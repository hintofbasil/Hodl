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

        TextView coinNameView = (TextView) v.findViewById(R.id.coin_name);
        coinNameView.setText(summary.getName());

        TextView tickerSymbol = (TextView)v.findViewById(R.id.coin_ticker_symbol);
        tickerSymbol.setText(
                String.format("(%s)",
                        summary.getSymbol()
                )
        );

        TextView price = (TextView)v.findViewById(R.id.coin_price_usd);
        if (price != null && !price.equals("")) {
            price.setText(String.format("$%s", summary.getPriceUSD(true).toString()));
        } else {
            String text = getContext().getString(R.string.price_missing);
            price.setText(text);
        }

        if (summary.getQuantity().signum() == 1) {
            TextView quantityANndOwnedValueView = (TextView) v.findViewById(R.id.coin_quantity_and_owned_value);
            quantityANndOwnedValueView.setText(
                    String.format("%s ($%s)",
                            summary.getQuantity().toString(),
                            summary.getOwnedValue(true).toString()
                    )
            );
        }

        return v;
    }
}
