package com.github.hintofbasil.hodl.database;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
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

    private DbHelper dbHelper;
    private SQLiteDatabase coinSummaryDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DbHelper(this);
        coinSummaryDatabase = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        SyncHttpClient client = new SyncHttpClient();
        client.get(COIN_MARKET_CAP_API_URL, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Intent intent = new Intent(UPDATE_PROGRESS);
                intent.putExtra(INTENT_UPDATE_PROGRESS, 0);
                sendBroadcast(intent);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                long updateStartTime = System.currentTimeMillis();
                String data = new String(responseBody);
                JsonElement jsonElement = new JsonParser().parse(data);
                JsonArray baseArray = jsonElement.getAsJsonArray();

                int valuesCount = baseArray.size();
                int progress = -1;

                for (int i=0; i<valuesCount;i++) {

                    JsonElement coinDataElement = baseArray.get(i);

                    JsonObject coinData = coinDataElement.getAsJsonObject();
                    String symbol = coinData.get("symbol").getAsString();
                    String name = coinData.get("name").getAsString();
                    String id = coinData.get("id").getAsString();
                    int rank = coinData.get("rank").getAsInt();
                    String priceUSD = coinData.get("price_usd").getAsString();

                    // Query existing data
                    String selection = CoinSummarySchema.CoinEntry.COLUMN_NAME_ID + " = ?";
                    String selectionArgs[] = { id };
                    Cursor cursor = coinSummaryDatabase.query(
                            CoinSummarySchema.CoinEntry.TABLE_NAME,
                            CoinSummarySchema.allProjection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );

                    if (cursor.moveToNext()) {
                        CoinSummary summary = CoinSummary.buildFromCursor(cursor);
                        summary.setSymbol(symbol);
                        summary.setName(name);
                        summary.setPriceUSD(new BigDecimal(priceUSD));
                        summary.setRank(rank);
                        summary.updateDatabase(coinSummaryDatabase, "symbol", "name", "price", "rank");
                    } else {
                        CoinSummary summary = new CoinSummary(symbol, name, id);
                        summary.setPriceUSD(new BigDecimal(priceUSD));
                        summary.setRank(rank);
                        summary.addToDatabase(coinSummaryDatabase);
                    }
                    cursor.close();

                    // Broadcast progress
                    int newProgress = i * 100 / valuesCount;
                    if (newProgress > progress) {
                        progress = newProgress;
                        Intent intent = new Intent(UPDATE_PROGRESS);
                        intent.putExtra(INTENT_UPDATE_PROGRESS, progress);
                        sendBroadcast(intent);
                    }
                }
                sendBroadcast(new Intent(STATUS_COMPLETED));
                Log.d("CoinMarketCapUpdaterSer",
                        String.format("Updates processed in %dms",
                                System.currentTimeMillis() - updateStartTime
                        )
                );
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                sendBroadcast(new Intent(STATUS_FAILURE));
            }
        });
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        coinSummaryDatabase.close();
        super.onDestroy();
    }

    public class Implementation {

        private String baseUrl = BASE_URL;
        private DbHelper dbHelper;
        private SQLiteDatabase coinSummaryDatabase;

        public Implementation(Context context) {
            dbHelper = new DbHelper(context);
            coinSummaryDatabase = dbHelper.getWritableDatabase();
        }

        public List<CoinSummary> downloadData() throws IOException {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            API api = retrofit.create(API.class);
            Call<List<CoinSummaryGson>> a = api.getAll();
            List<CoinSummaryGson> b = a.execute().body();
            List<CoinSummary> lst = new ArrayList<>();
            for (CoinSummaryGson z : b) {
                lst.add(z.toCoinSummary());
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

        public void processAll() throws IOException {
            for (CoinSummary summary : downloadData()) {
                summary.addToDatabase(coinSummaryDatabase);
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
