package com.github.hintofbasil.hodl.SearchableSpinner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hintofbasil.hodl.R;
import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by will on 8/16/17.
 */

public class CoinSelectListAdapter extends ArrayAdapter<CoinSummary> {

    int resource;
    ImageLoader imageLoader;

    public CoinSelectListAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
        imageLoader = ImageLoader.getInstance();
    }

    public CoinSelectListAdapter(Context context, int resource, CoinSummary[] objects) {
        super(context, resource, objects);
        this.resource = resource;
        imageLoader = ImageLoader.getInstance();
    }

    public CoinSelectListAdapter(Context context, int resource, List objects) {
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
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(summary.getImageURL(64), coinImageView);

        TextView coinNameView = (TextView) v.findViewById(R.id.coin_name);
        coinNameView.setText(summary.getName());

        TextView coinTickerSymbolView = (TextView) v.findViewById(R.id.coin_ticker_symbol);
        String formattedSymbol = String.format("(%s)", summary.getSymbol());
        coinTickerSymbolView.setText(formattedSymbol);

        return v;
    }
}
