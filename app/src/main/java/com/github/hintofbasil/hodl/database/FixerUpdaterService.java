package com.github.hintofbasil.hodl.database;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import java.math.BigDecimal;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by will on 8/22/17.
 */

public class FixerUpdaterService extends IntentService {

    public static final String FIXER_API_URL = "http://api.fixer.io/latest?base=USD";

    public static final String STATUS_FAILURE = "FIXER_UPDATER_STATUS_FAILURE";
    public static final String STATUS_COMPLETED = "FIXER_STATUS_COMPLETED";
    public static final String UPDATE_PROGRESS = "FIXER_UPDATE_PROGRESS";
    public static final String INTENT_UPDATE_PROGRESS = "FIXER_INTENT_UPDATE_PROGRESS";

    private DbHelper dbHelper;
    private SQLiteDatabase database;

    public FixerUpdaterService() {
        super("FixerUpdaterService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DbHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SyncHttpClient client = new SyncHttpClient();
        client.get(FIXER_API_URL, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Intent intent = new Intent(UPDATE_PROGRESS);
                intent.putExtra(INTENT_UPDATE_PROGRESS, 0);
                sendBroadcast(intent);
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String data = new String(responseBody);
                JsonElement jsonElement = new JsonParser().parse(data);
                JsonObject baseObject = jsonElement.getAsJsonObject();
                JsonObject currencyData = baseObject.getAsJsonObject("rates");

                int valuesCount = currencyData.size();
                int progress = -1;
                int i = 0;

                for (Map.Entry<String, JsonElement> currency : currencyData.entrySet()) {
                    String symbol = currency.getKey();
                    BigDecimal value = currency.getValue().getAsBigDecimal();

                    // Query existing data
                    String selection = ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL + " = ?";
                    String selectionArgs[] = { symbol };
                    Cursor cursor = database.query(
                            ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                            ExchangeRateSchema.allProjection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );

                    if (cursor.moveToNext()) {
                        ExchangeRate exchangeRate = ExchangeRate.buildFromCursor(cursor);
                        exchangeRate.setExchangeRate(value);
                        exchangeRate.updateDatabase(database);
                    } else {
                        ExchangeRate exchangeRate = new ExchangeRate(symbol, value);
                        exchangeRate.addToDatabase(database);
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
                    i++;

                }
                sendBroadcast(new Intent(STATUS_COMPLETED));
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
        database.close();
        super.onDestroy();
    }
}
