package com.github.hintofbasil.hodl.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.github.hintofbasil.hodl.R;
import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.FixerUpdaterService;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 9/6/17.
 */

public class SettingsFragment extends PreferenceFragment {

    private DbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FixerUpdaterService.STATUS_COMPLETED);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        initialiseCurrency();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void initialiseCurrency() {

        if (dbHelper == null) {
            dbHelper = new DbHelper(getActivity());
            database = dbHelper.getWritableDatabase();
        }

        String sortOrder = ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL + " ASC";
        Cursor cursor = database.query(
                ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                ExchangeRateSchema.allProjection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        ListPreference currencies = (ListPreference) findPreference("preferences_currency");

        // No additional data has been downloaded
        if (cursor.getCount() <= 1) {
            currencies.setEnabled(false);
            currencies.setSummary(R.string.preferences_currency_summary_disabled);
            return;
        } else {
            currencies.setEnabled(true);
            currencies.setSummary(R.string.preferences_currency_summary);
        }

        List<String> currenciesValues = new ArrayList<>();
        List<String> currenciesEntries = new ArrayList<>();

        while(cursor.moveToNext()) {
            ExchangeRate exchangeRate = ExchangeRate.buildFromCursor(cursor);
            String name = String.format(
                    "%s (%s)",
                    exchangeRate.getName(),
                    exchangeRate.getSymbol()
            );
            currenciesValues.add(exchangeRate.getSymbol());
            currenciesEntries.add(name);
        }

        String[] currenciesValuesArray = new String[currenciesValues.size()];
        currenciesValues.toArray(currenciesValuesArray);
        currencies.setEntryValues(currenciesValuesArray);


        String[] currenciesEntriesArray = new String[currenciesEntries.size()];
        currenciesEntries.toArray(currenciesEntriesArray);
        currencies.setEntries(currenciesEntriesArray);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FixerUpdaterService.STATUS_COMPLETED:
                    initialiseCurrency();
                    break;
            }
        }
    };
}
