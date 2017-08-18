package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.SearchableSpinner.CoinSelectListAdapter;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.math.BigDecimal;

public class CoinDetailsActivity extends Activity {

    EditText quantityEditText;
    ImageView coinImageView;
    TextView tickerSymbol;
    TextView price;
    Spinner coinSearchBox;
    Switch watchSwitch;

    CoinSummary coinSummary;

    SharedPreferences coinSharedData;
    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);

        coinSummary = (CoinSummary) getIntent().getSerializableExtra("coinSummary");

        coinSharedData = getSharedPreferences("hintofbasil.github.com.coin_status", MODE_PRIVATE);

        imageLoader = ImageLoader.getInstance();
        coinImageView = (ImageView) findViewById(R.id.coin_image);
        tickerSymbol = (TextView) findViewById(R.id.coin_ticker_symbol);
        price = (TextView)findViewById(R.id.coin_price_usd);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        coinSearchBox = (Spinner) findViewById(R.id.coin_search_box);
        watchSwitch = (Switch) findViewById(R.id.coin_watch_switch);

        int coinNumber = coinSharedData.getAll().size();
        CoinSummary[] coinNames = new CoinSummary[coinNumber];
        int i = 0;
        int toShow = 0;
        Gson gson = new Gson();
        for (String key : coinSharedData.getAll().keySet()) {
            String json = coinSharedData.getString(key, null);
            CoinSummary summary = gson.fromJson(json, CoinSummary.class);
            if (summary.getSymbol().equals(coinSummary.getSymbol())) {
                toShow = i;
            }
            coinNames[i++] = summary;
        }

        CoinSelectListAdapter coinSearchBoxAdapter = new CoinSelectListAdapter(
                this,
                R.layout.coin_select_spinner_dropdown_no_image,
                coinNames);
        coinSearchBox.setSelection(toShow);
        coinSearchBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CoinSummary newCoinSummary = (CoinSummary) coinSearchBox.getItemAtPosition(position);
                CoinDetailsActivity.this.coinSummary = newCoinSummary;
                CoinDetailsActivity.this.setCoinData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        coinSearchBox.setAdapter(coinSearchBoxAdapter);

        setCoinData();
    }

    private void setCoinData() {

        imageLoader.displayImage(coinSummary.getImageURL(128), coinImageView);

        tickerSymbol.setText(coinSummary.getSymbol());

        if (coinSummary.getPriceUSD() != null) {
            price.setText(String.format("$%s", coinSummary.getPriceUSD()));
        } else {
            String text = getString(R.string.price_missing);
            price.setText(text);
        }

        if (coinSummary.getQuantity() != null) {
            quantityEditText.setText(coinSummary.getQuantity().toString());
        } else {
            quantityEditText.setText("0");
        }

        watchSwitch.setChecked(coinSummary.isWatched());
    }

    public void onSubmit(View view) {
        String quantityString = quantityEditText.getText().toString();
        try {
            BigDecimal quantity = new BigDecimal(quantityString);
            coinSummary.setQuantity(quantity);
            Gson gson = new Gson();
            coinSharedData.edit().putString(coinSummary.getSymbol(), gson.toJson(coinSummary)).apply();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    public void onWatchChanged(View view) {
        Switch swtch = (Switch) view;
        coinSummary.setWatched(swtch.isChecked());
        Gson gson = new Gson();
        String json = gson.toJson(coinSummary);
        coinSharedData.edit().putString(coinSummary.getSymbol(), json).apply();
    }
}
