package com.github.hintofbasil.hodl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;
import com.github.hintofbasil.hodl.database.CoinMarketCapUpdaterService;
import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.FixerUpdaterService;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;
import com.github.hintofbasil.hodl.settings.SettingsActivity;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private TextView totalCoinSummary;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar updateProgressBar;

    DbHelper dbHelper;
    SQLiteDatabase coinSummaryDatabase;

    private int coinMarketCapUpdaterProgress = 0;
    private int fixerUpdaterProgress = 0;
    private boolean isCoinMarketCapUpdaterCompleted = false;
    private boolean isFixerUpdaterCompleted = false;
    private boolean updateErrorReported = false;

    private ExchangeRate activeExchangeRate;

    private int coinMarketCapUpdateProgressRatio = 90;
    private int fixerUpdateProgressRatio = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.homepage_summary_toolbar);
        setSupportActionBar(myToolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        migratePreferences();

        // Initialise CoinMarketCapIdGetter
        if (!CoinMarketCapIdGetter.get().isLoaded()) {
            try {
                CoinMarketCapIdGetter.get().init(this);
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.coin_market_cap_id_getter_init_failed), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(this, getString(R.string.coin_market_cap_id_getter_init_failed), Toast.LENGTH_LONG).show();
            }
        }

        dbHelper = new DbHelper(MainActivity.this);
        coinSummaryDatabase = dbHelper.getWritableDatabase();

        totalCoinSummary = (TextView) findViewById(R.id.total_coin_summary);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        updateProgressBar = (ProgressBar) findViewById(R.id.update_progress_bar);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                coinMarketCapUpdaterProgress = 0;
                fixerUpdaterProgress = 0;
                isCoinMarketCapUpdaterCompleted = false;
                isFixerUpdaterCompleted = false;
                updateErrorReported = false;
                MainActivity.this.refreshDataFromSources();
            }
        });

        // https://stackoverflow.com/questions/27041416/cant-scroll-in-a-listview-in-a-swiperefreshlayout
        final ListView coinSummaryListView = (ListView) findViewById(R.id.coin_summary_list);
        coinSummaryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (coinSummaryListView == null || coinSummaryListView.getChildCount() == 0) ?
                        0 : coinSummaryListView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_COMPLETED);
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_FAILURE);
        intentFilter.addAction(CoinMarketCapUpdaterService.UPDATE_PROGRESS);

        intentFilter.addAction(FixerUpdaterService.STATUS_COMPLETED);
        intentFilter.addAction(FixerUpdaterService.STATUS_FAILURE);
        intentFilter.addAction(FixerUpdaterService.UPDATE_PROGRESS);
        registerReceiver(broadcastReceiver, intentFilter);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        refreshDataFromSources();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseCoinSummaryList();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        coinSummaryDatabase.close();
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_menu_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void migratePreferences() {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);

        String currencySymbol = preferenceManager.getString(SettingsActivity.DISPLAY_CURRENCY, null);
        if (currencySymbol == null) {
            preferenceManager.edit()
                    .putString(SettingsActivity.DISPLAY_CURRENCY,
                            getString(R.string.preferences_currency_default))
                    .apply();
        }
    }

    private ExchangeRate getActiveExchangeRate() {
        if (activeExchangeRate == null) {
            SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
            String currencySymbol = preferenceManager.getString(SettingsActivity.DISPLAY_CURRENCY, "");

            String selection = ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL + " = ?";
            String selectionArgs[] = { currencySymbol };
            Cursor cursor = coinSummaryDatabase.query(
                    ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                    ExchangeRateSchema.allProjection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            cursor.moveToNext();
            activeExchangeRate = ExchangeRate.buildFromCursor(cursor);
        }
        return activeExchangeRate;
    }

    private void refreshDataFromSources() {
        requestDataFromCoinMarketCap();
        requestDataFromFixer();
    }

    private void requestDataFromCoinMarketCap() {
        Intent intent = new Intent(this, CoinMarketCapUpdaterService.class);
        startService(intent);
    }

    private void requestDataFromFixer() {
        Intent intent = new Intent(this, FixerUpdaterService.class);
        startService(intent);
    }

    private void initialiseCoinSummaryList() {
        CoinSummary[] coinData = loadCachedCoinData();
        final ListView coinSummaryList = (ListView)findViewById(R.id.coin_summary_list);
        CoinSummaryListAdapter coinSummaryListAdapter = new CoinSummaryListAdapter(
                this,
                R.layout.coin_summary_list_element,
                coinData,
                coinSummaryList,
                getActiveExchangeRate());
        coinSummaryList.setAdapter(coinSummaryListAdapter);
        coinSummaryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, CoinDetailsActivity.class);
                CoinSummary summary = (CoinSummary) coinSummaryList.getItemAtPosition(position);
                intent.putExtra("coinSummary", summary);
                startActivity(intent);
            }
        });
    }

    private CoinSummary[] loadCachedCoinData() {

        String selection = CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED + " = ?";
        String selectionArgs[] = { "1" };
        String sortOrder = CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK + " ASC";

        Cursor cursor = coinSummaryDatabase.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        int count = cursor.getCount();

        CoinSummary[] coinData = new CoinSummary[count];
        int id = 0;
        BigDecimal totalValue = new BigDecimal(0);

        findViewById(R.id.coin_summary_price_missing).setVisibility(View.GONE);
        while (cursor.moveToNext()) {
            CoinSummary summary = CoinSummary.buildFromCursor(cursor);
            coinData[id++] = summary;
            BigDecimal ownedValue = summary.getOwnedValue();
            if (ownedValue != null) {
                totalValue = totalValue.add(summary.getOwnedValue());
            } else {
                // Only show warning is coins are owned
                if (summary.getQuantity().signum() > 0) {
                    findViewById(R.id.coin_summary_price_missing).setVisibility(View.VISIBLE);
                }
            }
        }
        cursor.close();

        cursor = coinSummaryDatabase.query(CoinSummarySchema.CoinEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            FloatingActionButton addCoinButton = (FloatingActionButton) findViewById(R.id.add_coin_button);
            addCoinButton.setVisibility(View.VISIBLE);
        }
        cursor.close();

        ExchangeRate exchangeRate = getActiveExchangeRate();
        totalCoinSummary.setText(
                String.format(
                        "%s%s",
                        exchangeRate.getToken(),
                        totalValue
                                .multiply(exchangeRate.getExchangeRate())
                                .setScale(2, BigDecimal.ROUND_DOWN)
                                .toString()
                )
        );

        Arrays.sort(coinData);
        return coinData;
    }

    public void onPlusButtonClicked(View view) {
        // Button is not visible when no data is available
        String selection = CoinSummarySchema.CoinEntry.COLUMN_NAME_ID + " = ?";
        String selectionArgs[] = { "bitcoin" };

        Cursor cursor = coinSummaryDatabase.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Bitcoin data may be unavailable, if so load random coin
        if (cursor.getCount() == 0) {
            cursor.close();
            cursor = coinSummaryDatabase.query(
                    CoinSummarySchema.CoinEntry.TABLE_NAME,
                    CoinSummarySchema.allProjection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        cursor.moveToNext();
        CoinSummary summary = CoinSummary.buildFromCursor(cursor);
        cursor.close();
        Intent intent = new Intent(this, CoinDetailsActivity.class);
        intent.putExtra("coinSummary", summary);
        startActivity(intent);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress;
            switch (intent.getAction()) {
                case CoinMarketCapUpdaterService.STATUS_FAILURE:
                    if (!updateErrorReported) {
                        updateErrorReported = true;
                        Toast.makeText(getBaseContext(), getString(R.string.network_update_failed), Toast.LENGTH_SHORT).show();
                    }
                    coinMarketCapUpdaterProgress = 100;
                    // Deliberate missing break
                case CoinMarketCapUpdaterService.STATUS_COMPLETED:
                    isCoinMarketCapUpdaterCompleted = true;
                    if (isFixerUpdaterCompleted) {
                        MainActivity.this.initialiseCoinSummaryList();
                        swipeRefreshLayout.setRefreshing(false);
                        updateProgressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case CoinMarketCapUpdaterService.UPDATE_PROGRESS:
                    progress = intent.getIntExtra(CoinMarketCapUpdaterService.INTENT_UPDATE_PROGRESS, 0);
                    coinMarketCapUpdaterProgress = progress;
                    updateProgressBar.setProgress(
                            (coinMarketCapUpdaterProgress * coinMarketCapUpdateProgressRatio / 100)
                                    + (fixerUpdaterProgress * fixerUpdateProgressRatio / 100)
                    );
                    updateProgressBar.setVisibility(View.VISIBLE);
                    break;


                case FixerUpdaterService.STATUS_FAILURE:
                    if (!updateErrorReported) {
                        updateErrorReported = true;
                        Toast.makeText(getBaseContext(), getString(R.string.network_update_failed), Toast.LENGTH_SHORT).show();
                    }
                    fixerUpdaterProgress = 100;
                    // Deliberate missing break
                case FixerUpdaterService.STATUS_COMPLETED:
                    isFixerUpdaterCompleted = true;
                    if (isCoinMarketCapUpdaterCompleted) {
                        MainActivity.this.initialiseCoinSummaryList();
                        swipeRefreshLayout.setRefreshing(false);
                        updateProgressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case FixerUpdaterService.UPDATE_PROGRESS:
                    progress = intent.getIntExtra(FixerUpdaterService.INTENT_UPDATE_PROGRESS, 0);
                    fixerUpdaterProgress = progress;
                    updateProgressBar.setProgress(
                            (coinMarketCapUpdaterProgress * coinMarketCapUpdateProgressRatio / 100)
                                    + (fixerUpdaterProgress * fixerUpdateProgressRatio / 100)
                    );
                    updateProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (SettingsActivity.DISPLAY_CURRENCY.equals(key)) {
                        MainActivity.this.activeExchangeRate = null;
                        MainActivity.this.loadCachedCoinData();
                    }
                }
            };
}
