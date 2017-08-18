package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String COIN_MARKET_CAP_API_URL = "https://api.coinmarketcap.com/v1/ticker/?limit=10";

    private SharedPreferences coinSharedData;
    private TextView totalCoinSummary;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader();
        setContentView(R.layout.activity_main);
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
                Gson gson = new Gson();
                for(JsonElement coinDataElement : baseArray) {
                    JsonObject coinData = coinDataElement.getAsJsonObject();
                    String symbol = coinData.get("symbol").getAsString();
                    String name = coinData.get("name").getAsString();
                    String id = coinData.get("id").getAsString();
                    String priceUSD = coinData.get("price_usd").getAsString();
                    String previousCoin = coinSharedData.getString(symbol, null);
                    CoinSummary coin;
                    if (previousCoin != null) {
                        coin = gson.fromJson(previousCoin, CoinSummary.class);
                    } else {
                        coin = new CoinSummary(symbol, name, id);
                    }
                    coin.setPriceUSD(new BigDecimal(priceUSD));
                    coinSharedData.edit().putString(symbol, gson.toJson(coin)).apply();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                swipeRefreshLayout.setRefreshing(false);
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

        Map<String, String> cachedCoinData = (Map<String, String>) coinSharedData.getAll();
        CoinSummary[] coinData = new CoinSummary[cachedCoinData.size()];
        int id = 0;
        BigDecimal totalValue = new BigDecimal(0);
        Gson gson = new Gson();
        for(String data : cachedCoinData.values()) {
            CoinSummary summary = gson.fromJson(data, CoinSummary.class);
            if (summary.isWatched()) {
                coinData[id++] = summary;
            }
            totalValue = totalValue.add(summary.getOwnedValue());
        }
        totalCoinSummary.setText(String.format("$%s", totalValue.toString()));
        coinData = Arrays.copyOfRange(coinData, 0, id);
        return coinData;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.diskCacheFileCount(200)
                .threadPriority(Thread.NORM_PRIORITY - 2)
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
}
