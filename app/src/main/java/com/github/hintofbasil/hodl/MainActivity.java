package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;
import com.github.hintofbasil.hodl.database.CoinSummaryDbHelper;
import com.github.hintofbasil.hodl.database.CoinSummarySchema;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String COIN_MARKET_CAP_API_URL = "https://api.coinmarketcap.com/v1/ticker/?limit=100";

    private SharedPreferences coinSharedData;
    private TextView totalCoinSummary;
    private SwipeRefreshLayout swipeRefreshLayout;

    CoinSummaryDbHelper dbHelper;
    SQLiteDatabase coinSummaryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader();
        setContentView(R.layout.activity_main);

        dbHelper = new CoinSummaryDbHelper(MainActivity.this);
        coinSummaryDatabase = dbHelper.getWritableDatabase();

        coinSharedData = getSharedPreferences("hintofbasil.github.com.coin_status", MODE_PRIVATE);
        totalCoinSummary = (TextView) findViewById(R.id.total_coin_summary);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.this.requestDataFromCoinMarketCap();
            }
        });

        coinSharedData.registerOnSharedPreferenceChangeListener(this);

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

        requestDataFromCoinMarketCap();
        initialiseCoinSummaryList();
    }

    @Override
    protected void onDestroy() {
        coinSharedData.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void requestDataFromCoinMarketCap() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(COIN_MARKET_CAP_API_URL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String data = new String(responseBody);
                JsonElement jsonElement = new JsonParser().parse(data);
                JsonArray baseArray = jsonElement.getAsJsonArray();

                for(JsonElement coinDataElement : baseArray) {

                    JsonObject coinData = coinDataElement.getAsJsonObject();
                    String symbol = coinData.get("symbol").getAsString();
                    String name = coinData.get("name").getAsString();
                    String id = coinData.get("id").getAsString();
                    int rank = coinData.get("rank").getAsInt();
                    String priceUSD = coinData.get("price_usd").getAsString();

                    CoinSummary coin = new CoinSummary(symbol, name, id);
                    coin.setPriceUSD(new BigDecimal(priceUSD));
                    coin.setRank(rank);
                    coin.addToDatabase(coinSummaryDatabase);
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getBaseContext(), getString(R.string.network_update_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialiseCoinSummaryList() {
        CoinSummary[] coinData = loadCachedCoinData();
        final ListView coinSummaryList = (ListView)findViewById(R.id.coin_summary_list);
        CoinSummaryListAdapter coinSummaryListAdapter = new CoinSummaryListAdapter(
                this,
                R.layout.coin_summary_list_element,
                coinData);
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
        String sortOrder = CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK + " DESC";

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
        while (cursor.moveToNext()) {
            CoinSummary summary = CoinSummary.buildFromCursor(cursor);
            coinData[id++] = summary;
            totalValue = totalValue.add(summary.getOwnedValue(false));
        }

        cursor = coinSummaryDatabase.query(CoinSummarySchema.CoinEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            FloatingActionButton addCoinButton = (FloatingActionButton) findViewById(R.id.add_coin_button);
            addCoinButton.setVisibility(View.VISIBLE);
        }

        totalCoinSummary.setText(String.format("$%s", totalValue.setScale(2, BigDecimal.ROUND_DOWN).toString()));
        return coinData;
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.defaultDisplayImageOptions(defaultOptions)
                .diskCacheFileCount(1200) // 1069 coins on market cap.
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.FIFO)
        ;

        ImageLoader.getInstance().init(config.build());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences == coinSharedData) {
            initialiseCoinSummaryList();
        }
    }

    public void onPlusButtonClicked(View view) {
        // Button is not visible when no data is available
        String summaryJSON = coinSharedData.getString("BTC", null);
        // Bitcoin data may be unavailable, if so load random coin
        // Uses for loop as can't get first value from set
        if (summaryJSON == null) {
            for (String key : coinSharedData.getAll().keySet()) {
                summaryJSON = coinSharedData.getString(key, "");
                break;
            }
        }
        Gson gson = new Gson();
        CoinSummary summary = gson.fromJson(summaryJSON, CoinSummary.class);
        Intent intent = new Intent(this, CoinDetailsActivity.class);
        intent.putExtra("coinSummary", summary);
        startActivity(intent);
    }
}
