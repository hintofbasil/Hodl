package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.SearchableSpinner.CoinSelectListAdapter;
import com.github.hintofbasil.hodl.SearchableSpinner.SearchableSpinner;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;
import com.github.hintofbasil.hodl.helpers.SqlHelperSingleton;
import com.github.hintofbasil.hodl.settings.SettingsActivity;

import java.math.BigDecimal;

public class CoinDetailsActivity extends Activity {

    EditText quantityEditText;
    ImageView coinImageView;
    TextView price;
    TextView ownedValue;
    SearchableSpinner coinSearchBox;
    Switch watchSwitch;

    CoinSummary coinSummary;

    FloatingActionButton saveButton;

    boolean trackAutoEnabledOnce;

    SQLiteDatabase coinSummaryDatabase;

    private ExchangeRate activeExchangeRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);

        coinSummaryDatabase = SqlHelperSingleton.getDatabase(getApplicationContext());

        trackAutoEnabledOnce = false;

        coinSummary = (CoinSummary) getIntent().getSerializableExtra("coinSummary");

        coinImageView = (ImageView) findViewById(R.id.coin_image);
        price = (TextView)findViewById(R.id.coin_price_usd);
        ownedValue = (TextView) findViewById(R.id.coin_owned_value);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        coinSearchBox = (SearchableSpinner) findViewById(R.id.coin_search_box);
        watchSwitch = (Switch) findViewById(R.id.coin_watch_switch);
        saveButton = (FloatingActionButton) findViewById(R.id.save);

        String sortOrder = CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK + " ASC";
        Cursor cursor = coinSummaryDatabase.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        int coinNumber = cursor.getCount();
        CoinSummary[] coinNames = new CoinSummary[coinNumber];
        int i = 0;
        while (cursor.moveToNext()) {
            CoinSummary summary = CoinSummary.buildFromCursor(cursor);
            coinNames[i++] = summary;
        }
        cursor.close();

        int toShow = 0;
        for (i=0; i<coinNames.length; i++) {
            CoinSummary summary = coinNames[i];
            if (summary.getSymbol().equals(coinSummary.getSymbol())) {
                toShow = i;
            }
        }

        CoinSelectListAdapter coinSearchBoxAdapter = new CoinSelectListAdapter(
                this,
                R.layout.coin_select_spinner_dropdown_no_image,
                coinNames);
        coinSearchBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ownedValue.setVisibility(View.GONE);
                CoinSummary newCoinSummary = (CoinSummary) coinSearchBox.getItemAtPosition(position);
                CoinDetailsActivity.this.coinSummary = newCoinSummary;
                CoinDetailsActivity.this.setCoinData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        coinSearchBox.setAdapter(coinSearchBoxAdapter);
        coinSearchBox.setSelection(toShow);

        setCoinData();
    }

    @Override
    protected void onPause() {
        coinSearchBox.minimize();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinSearchBox.maximize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ExchangeRate getActiveExchangeRate() {
        if (activeExchangeRate == null) {
            SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
            String currencySymbol = preferenceManager.getString(SettingsActivity.DISPLAY_CURRENCY, "USD");

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

    private void setCoinData() {

        quantityEditText.removeTextChangedListener(textWatcher);

        GlideApp.with(this)
                .load(coinSummary.getImageURL(128))
                .error(R.drawable.unknown_coin_image)
                .into(coinImageView);

        if (coinSummary.getPriceUSD() != null) {
            price.setText(
                    String.format(
                            "%s%s",
                            getActiveExchangeRate().getToken(),
                            coinSummary.getPriceUSD()
                                .multiply(getActiveExchangeRate().getExchangeRate())
                                .setScale(2, BigDecimal.ROUND_DOWN)
                    )
            );
        } else {
            String text = getString(R.string.price_missing);
            price.setText(text);
        }

        BigDecimal owned = coinSummary.getOwnedValue();
        if (owned != null && owned.signum() == 1) {
            ownedValue.setText(
                    String.format(
                            "(%s%s)",
                            getActiveExchangeRate().getToken(),
                            coinSummary.getOwnedValue()
                                    .multiply(getActiveExchangeRate().getExchangeRate())
                                    .setScale(2, BigDecimal.ROUND_DOWN)
                    )
            );
            ownedValue.setVisibility(View.VISIBLE);
        }

        if (coinSummary.getQuantity() != null) {
            quantityEditText.setText(coinSummary.getQuantity().toString());
        } else {
            quantityEditText.setText("0");
        }

        watchSwitch.setChecked(coinSummary.isWatched());

        quantityEditText.addTextChangedListener(textWatcher);
    }

    public void onSubmit(View view) {
        String quantityString = quantityEditText.getText().toString();
        try {
            BigDecimal quantity = new BigDecimal(quantityString);
            coinSummary.setQuantity(quantity);
            coinSummary.setWatched(watchSwitch.isChecked());
            coinSummary.updateDatabase(coinSummaryDatabase, "quantity", "watched");
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    public void onWatchToggled(View view) {
        saveButton.setVisibility(View.VISIBLE);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            CoinDetailsActivity.this.saveButton.setVisibility(View.VISIBLE);
            CoinDetailsActivity.this.ownedValue.setVisibility(View.VISIBLE);

            if (count > before && !trackAutoEnabledOnce) {
                trackAutoEnabledOnce = true;
                watchSwitch.setChecked(true);
            }
            try {
                CoinDetailsActivity.this.coinSummary.setQuantity(new BigDecimal(s.toString()));
                BigDecimal owned = coinSummary.getOwnedValue();
                if (owned != null && owned.signum() == 1) {
                    ownedValue.setText(
                            String.format(
                                    "(%s%s)",
                                    getActiveExchangeRate().getToken(),
                                    coinSummary.getOwnedValue()
                                        .multiply(getActiveExchangeRate().getExchangeRate())
                                        .setScale(2, BigDecimal.ROUND_DOWN)
                            )
                    );
                    ownedValue.setVisibility(View.VISIBLE);
                } else {
                    ownedValue.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                ownedValue.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
