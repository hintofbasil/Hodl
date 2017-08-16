package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummaryListAdapter;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseCoinSummaryList();
    }

    private void initialiseCoinSummaryList() {
        CoinSummary[] coinData = new CoinSummary[2];
        ListView coinSummaryList = (ListView)findViewById(R.id.coin_summary_list);
        CoinSummaryListAdapter coinSummaryListAdapter = new CoinSummaryListAdapter(
                this,
                R.layout.coin_summary_list_element,
                coinData);
        coinSummaryList.setAdapter(coinSummaryListAdapter);
    }
}
