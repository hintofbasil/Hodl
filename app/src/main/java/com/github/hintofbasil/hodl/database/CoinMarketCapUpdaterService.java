package com.github.hintofbasil.hodl.database;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by will on 8/22/17.
 */

public class CoinMarketCapUpdaterService extends IntentService {

    public static final String BASE_URL = "https://api.coinmarketcap.com";
    public static final String COIN_MARKET_CAP_API_URL = "https://api.coinmarketcap.com/v1/ticker/?limit=0";

    public static final String STATUS_FAILURE = "COIN_MARKET_CAP_UPDATER_STATUS_FAILURE";
    public static final String STATUS_COMPLETED = "COIN_MARKET_CAP_STATUS_COMPLETED";
    public static final String UPDATE_PROGRESS = "COIN_MARKET_CAP_UPDATE_PROGRESS";
    public static final String INTENT_UPDATE_PROGRESS = "COIN_MARKET_CAP_INTENT_UPDATE_PROGRESS";



    public CoinMarketCapUpdaterService() {
        super("CoinMarketCapUpdaterService");
    }

    private Implementation implementation;

    @Override
    public void onCreate() {
        super.onCreate();
        implementation = new Implementation(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        implementation.processAll();
    }

    @Override
    public void onDestroy() {
        implementation.close();
        super.onDestroy();
    }

    public class Implementation {

        private String baseUrl = BASE_URL;
        private DbHelper dbHelper;
        private SQLiteDatabase coinSummaryDatabase;
        private Context context;

        public Implementation(Context context) {
            this.context = context;
            dbHelper = new DbHelper(context);
            coinSummaryDatabase = dbHelper.getWritableDatabase();
        }

        public List<CoinSummary> downloadData() throws IOException {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            API api = retrofit.create(API.class);
            Call<List<CoinSummaryGson>> request = api.getAll();
            Response<List<CoinSummaryGson>> response = request.execute();
            if (!response.isSuccessful()) {
                throw new IOException("Get was unsuccessful");
            }
            List<CoinSummaryGson> data = response.body();
            List<CoinSummary> lst = new ArrayList<>();
            for (CoinSummaryGson summary : data) {
                lst.add(summary.toCoinSummary());
            }
            return lst;
        }

        public List<String> getExistingCoinIds () {
            Cursor cursor = coinSummaryDatabase.query(
                    CoinSummarySchema.CoinEntry.TABLE_NAME,
                    new String[] {CoinSummarySchema.CoinEntry.COLUMN_NAME_ID},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<String> ids = new ArrayList<>();
            int columnId = cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_ID);

            while (cursor.moveToNext()) {
                String id = cursor.getString(columnId);
                ids.add(id);
            }

            return ids;
        }

        public void processAll() {
            try {
                List<String> knownCoins = getExistingCoinIds();
                List<CoinSummary> data = downloadData();
                int valuesCount = data.size();
                int progress = -1;

                for (int i = 0; i < valuesCount; i++) {
                    CoinSummary summary = data.get(i);
                    if (knownCoins.contains(summary.getId())) {
                        // If price is null, use old price
                        if (summary.getPriceUSD() == null) {
                            summary.updateDatabase(coinSummaryDatabase, "symbol", "name", "rank");
                        } else {
                            summary.updateDatabase(coinSummaryDatabase, "symbol", "name", "price", "rank");
                        }
                    } else {
                        summary.addToDatabase(coinSummaryDatabase);
                    }

                    // Add Bitcoin as a currency
                    if ("bitcoin".equals(summary.getId()) && summary.getPriceUSD() != null) {
                        ExchangeRate btcExchangeRate;

                        String selection = ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL + " = ?";
                        String[] selectionArgs = { "BTC" };
                        Cursor exchangeCursor = coinSummaryDatabase.query(
                                ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                                ExchangeRateSchema.allProjection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                null
                        );
                        BigDecimal btcExchangeValue = new BigDecimal("1").divide(summary.getPriceUSD(), 10, BigDecimal.ROUND_CEILING);
                        if (exchangeCursor.moveToNext()) {
                            btcExchangeRate = ExchangeRate.buildFromCursor(exchangeCursor);
                            btcExchangeRate.setExchangeRate(btcExchangeValue);
                            btcExchangeRate.updateDatabase(coinSummaryDatabase);
                        } else {
                            btcExchangeRate = new ExchangeRate("BTC", btcExchangeValue);
                            btcExchangeRate.addToDatabase(coinSummaryDatabase);
                        }
                    }

                    // Broadcast progress
                    int newProgress = i * 100 / valuesCount;
                    if (newProgress > progress) {
                        progress = newProgress;
                        Intent intent = new Intent(UPDATE_PROGRESS);
                        intent.putExtra(INTENT_UPDATE_PROGRESS, progress);
                        context.sendBroadcast(intent);
                    }
                }

                context.sendBroadcast(new Intent(STATUS_COMPLETED));
            } catch (IOException e) {
                context.sendBroadcast(new Intent(STATUS_FAILURE));
            }
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public void close() {
            dbHelper.close();
            coinSummaryDatabase.close();
        }
    }

    public interface API {
        @GET("/v1/ticker/?limit=0")
        Call<List<CoinSummaryGson>> getAll();
    }

    // Used to allow GSON to parse data
    // specific to this API
    public class CoinSummaryGson {
        public String symbol;
        public BigDecimal price_usd;
        public String id;
        public String name;
        public int rank;

        public CoinSummary toCoinSummary() {
            return new CoinSummary(
                    symbol,
                    name,
                    id,
                    rank,
                    price_usd
            );
        }
    }
}
