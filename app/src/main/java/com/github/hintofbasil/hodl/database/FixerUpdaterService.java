package com.github.hintofbasil.hodl.database;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;
import com.github.hintofbasil.hodl.helpers.SqlHelperSingleton;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by will on 8/22/17.
 */

public class FixerUpdaterService extends IntentService {

    public static final String BASE_URL = "https://onwik5cjk0.execute-api.eu-west-1.amazonaws.com/";

    public static final String STATUS_FAILURE = "FIXER_UPDATER_STATUS_FAILURE";
    public static final String STATUS_COMPLETED = "FIXER_STATUS_COMPLETED";
    public static final String UPDATE_PROGRESS = "FIXER_UPDATE_PROGRESS";
    public static final String INTENT_UPDATE_PROGRESS = "FIXER_INTENT_UPDATE_PROGRESS";

    private Implementation implementation;

    public FixerUpdaterService() {
        super("FixerUpdaterService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        implementation = new Implementation(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        implementation.processAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class Implementation {

        private String baseUrl = BASE_URL;
        private SQLiteDatabase database;
        private Context context;

        public Implementation(Context context) {
            this.context = context;
            database = SqlHelperSingleton.getDatabase(context);
        }

        public List<ExchangeRate> downloadData() throws IOException {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            API api = retrofit.create(API.class);
            Call<FixerUpdaterService.FixerGson> request = api.getAll();
            Response<FixerGson> response = request.execute();
            if (!response.isSuccessful()) {
                throw new IOException("Get was unsuccessful");
            }
            FixerGson data = response.body();
            List<ExchangeRate> lst = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : data.rates.entrySet()) {
                ExchangeRate rate = new ExchangeRate(entry.getKey(), entry.getValue());
                lst.add(rate);
            }
            return lst;
        }

        public List<String> getExistingExchangeRateIds () {
            Cursor cursor = database.query(
                    ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                    new String[] {ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<String> ids = new ArrayList<>();
            int columnId = cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL);

            while (cursor.moveToNext()) {
                String id = cursor.getString(columnId);
                ids.add(id);
            }

            return ids;
        }

        public void processAll() {
            try {
                List<String> knownCoins = getExistingExchangeRateIds();
                List<ExchangeRate> data = downloadData();
                int valuesCount = data.size();
                int progress = -1;

                for (int i = 0; i < valuesCount; i++) {
                    ExchangeRate summary = data.get(i);
                    if (knownCoins.contains(summary.getSymbol())) {
                        summary.updateDatabase(database);
                    } else {
                        summary.addToDatabase(database);
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
    }

    public interface API {
        @GET("/Testing/fixer-io-cache")
        Call<FixerUpdaterService.FixerGson> getAll();
    }

    // Used to allow GSON to parse data
    // specific to this API
    public class FixerGson {
        public boolean success;
        public long timestamp;
        public String base;
        public String date;
        public Map<String, BigDecimal> rates;
    }
}
