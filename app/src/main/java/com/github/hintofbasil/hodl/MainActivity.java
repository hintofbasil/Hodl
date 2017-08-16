package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;
import com.google.gson.Gson;

import java.util.Map;

public class MainActivity extends Activity {

    SharedPreferences coinSharedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coinSharedData = getSharedPreferences("hintofbasil.github.com.coin_status", MODE_PRIVATE);

        initialiseCoinSummaryList();
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
