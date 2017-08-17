package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.math.BigDecimal;

public class CoinDetailsActivity extends Activity {

    EditText quantityEditText;
    CoinSummary coinSummary;
    SharedPreferences coinSharedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);

        coinSummary = (CoinSummary) getIntent().getSerializableExtra("coinSummary");

        coinSharedData = getSharedPreferences("hintofbasil.github.com.coin_status", MODE_PRIVATE);

        ImageView coinImageView = (ImageView) findViewById(R.id.coin_image);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(coinSummary.getImageURL(128), coinImageView);

        TextView tickerSymbol = (TextView) findViewById(R.id.coin_ticker_symbol);
        tickerSymbol.setText(coinSummary.getSymbol());

        TextView price = (TextView)findViewById(R.id.coin_price_usd);
        if (price != null && !price.equals("")) {
            price.setText(String.format("$%s", coinSummary.getPriceUSD()));
        } else {
            String text = getString(R.string.price_missing);
            price.setText(text);
        }

        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        if (coinSummary.getQuantity() != null) {
            quantityEditText.setText(coinSummary.getQuantity().toString());
        } else {
            quantityEditText.setText("0");
        }
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
}
