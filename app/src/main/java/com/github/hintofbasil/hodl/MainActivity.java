package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {

    public static final String COIN_MARKET_CAP_API_URL = "https://api.coinmarketcap.com/v1/ticker/?limit=10";

    SharedPreferences coinSharedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coinSharedData = getSharedPreferences("hintofbasil.github.com.coin_status", MODE_PRIVATE);

        requestDataFromCoinMarketCap();
        initialiseCoinSummaryList();
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
                    String priceUSD = coinData.get("price_usd").getAsString();
                    String previousCoin = coinSharedData.getString(symbol, null);
                    CoinSummary coin;
                    if (previousCoin != null) {
                        coin = gson.fromJson(previousCoin, CoinSummary.class);
                    } else {
                        coin = new CoinSummary(symbol);
                    }
                    coin.setPriceUSD(priceUSD);
                    coinSharedData.edit().putString(symbol, gson.toJson(coin)).apply();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void initialiseCoinSummaryList() {
        CoinSummary[] coinData = loadCachedCoinData();
        ListView coinSummaryList = (ListView)findViewById(R.id.coin_summary_list);
        CoinSummaryListAdapter coinSummaryListAdapter = new CoinSummaryListAdapter(
                this,
                R.layout.coin_summary_list_element,
                coinData);
        coinSummaryList.setAdapter(coinSummaryListAdapter);
    }

    private CoinSummary[] loadCachedCoinData() {

        Map<String, String> cachedCoinData = (Map<String, String>) coinSharedData.getAll();
        CoinSummary[] coinData = new CoinSummary[cachedCoinData.size()];
        int id = 0;
        Gson gson = new Gson();
        for(String data : cachedCoinData.values()) {
            CoinSummary summary = gson.fromJson(data, CoinSummary.class);
            coinData[id] = summary;
            id++;
        }
        return coinData;
    }
}
