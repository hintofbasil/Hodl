package com.github.hintofbasil.hodl.coinSummaryList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hintofbasil.hodl.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.math.BigDecimal;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummaryListAdapter extends ArrayAdapter<CoinSummary> {

    int resource;
    ImageLoader imageLoader;

    public CoinSummaryListAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
        imageLoader = ImageLoader.getInstance();
    }

    public CoinSummaryListAdapter(Context context, int resource, CoinSummary[] objects) {
        super(context, resource, objects);
        this.resource = resource;
        imageLoader = ImageLoader.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CoinSummary summary = getItem(position);

        View v = convertView;
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(this.resource, null);
        }

        ImageView coinImageView = (ImageView) v.findViewById(R.id.coin_image);
        imageLoader.displayImage(summary.getImageURL(128), coinImageView);

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
