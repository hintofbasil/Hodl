package com.github.hintofbasil.hodl.database;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.github.hintofbasil.hodl.MainActivity;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.math.BigDecimal;

import cz.msebera.android.httpclient.Header;

/**
 * Created by will on 8/22/17.
 */

public class CoinMarketCapUpdaterService extends IntentService {

    public static final String COIN_MARKET_CAP_API_URL = "https://api.coinmarketcap.com/v1/ticker/";

    public static final String STATUS_FAILURE = "UPDATER_STATUS_FAILURE";

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
                Intent intent = new Intent(MainActivity.MAIN_ACTIVITY_UPDATE_PROGRESS);
                intent.putExtra(MainActivity.MAIN_ACTIVITY_INTENT_UPDATE_PROGRESS, 0);
                sendBroadcast(intent);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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
                        summary.setPriceUSD(new BigDecimal(priceUSD));
                        summary.setRank(rank);
                        summary.updateDatabase(coinSummaryDatabase, "price", "rank");
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
                        Intent intent = new Intent(MainActivity.MAIN_ACTIVITY_UPDATE_PROGRESS);
                        intent.putExtra(MainActivity.MAIN_ACTIVITY_INTENT_UPDATE_PROGRESS, progress);
                        sendBroadcast(intent);
                    }
                }
                sendBroadcast(new Intent(MainActivity.MAIN_ACTIVITY_REFRESH));
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
}
