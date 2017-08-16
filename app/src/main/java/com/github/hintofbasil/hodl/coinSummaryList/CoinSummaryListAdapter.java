package com.github.hintofbasil.hodl.coinSummaryList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.hintofbasil.hodl.R;

import java.math.BigDecimal;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummaryListAdapter extends ArrayAdapter<CoinSummary> {

    int resource;

    public CoinSummaryListAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }

    public CoinSummaryListAdapter(Context context, int resource, CoinSummary[] objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CoinSummary summary = getItem(position);

        View v = LayoutInflater.from(getContext()).inflate(this.resource, null);

        TextView tickerSymbol = (TextView)v.findViewById(R.id.coin_ticker_symbol);
        tickerSymbol.setText(summary.getSymbol());

        TextView price = (TextView)v.findViewById(R.id.coin_price_usd);
        if (price != null && !price.equals("")) {
            price.setText(String.format("$%s", summary.getPriceUSD().toString()));
        } else {
            String text = getContext().getString(R.string.price_missing);
            price.setText(text);
        }

        TextView quantityView = (TextView) v.findViewById(R.id.coin_quantity);
        BigDecimal quantity = summary.getQuantity();
        if (quantity != null) {
            quantityView.setText(quantity.toString());
        } else {
            quantityView.setText("0");
        }

        return v;
    }
}
