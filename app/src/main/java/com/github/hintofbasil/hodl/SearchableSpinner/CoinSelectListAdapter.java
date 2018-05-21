package com.github.hintofbasil.hodl.SearchableSpinner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hintofbasil.hodl.GlideApp;
import com.github.hintofbasil.hodl.R;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by will on 8/16/17.
 */

public class CoinSelectListAdapter extends ArrayAdapter<CoinSummary> {

    int resource;
    List<CoinSummary> originalList;

    public CoinSelectListAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }

    public CoinSelectListAdapter(Context context, int resource, CoinSummary[] objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.originalList = Arrays.asList(objects);
    }

    public CoinSelectListAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.originalList = new ArrayList<>(objects);
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
        GlideApp.with(getContext())
                .load(summary.getImageURL(64))
                .error(R.drawable.unknown_coin_image)
                .into(coinImageView);

        TextView coinNameView = (TextView) v.findViewById(R.id.coin_name);
        coinNameView.setText(summary.getName());

        TextView coinTickerSymbolView = (TextView) v.findViewById(R.id.coin_ticker_symbol);
        String formattedSymbol = String.format("(%s)", summary.getSymbol());
        coinTickerSymbolView.setText(formattedSymbol);

        return v;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filterResults.values = CoinSelectListAdapter.this.originalList;
                    filterResults.count = CoinSelectListAdapter.this.originalList.size();
                } else {
                    ArrayList<CoinSummary> tempList = new ArrayList<>();
                    String lowerContraint = constraint.toString().toLowerCase();
                    for (CoinSummary summary : CoinSelectListAdapter.this.originalList) {
                        if (summary.getSymbol().toLowerCase().contains(lowerContraint)
                                || summary.getName().toLowerCase().contains(lowerContraint)) {
                            tempList.add(summary);
                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                CoinSelectListAdapter.this.clear();
                for (CoinSummary summary : ((List<CoinSummary>)results.values)) {
                    CoinSelectListAdapter.this.add(summary);
                }
                notifyDataSetChanged();
            }
        };
        //return super.getFilter();
    }

    public void resetFilter() {
        CoinSelectListAdapter.this.clear();
        for (CoinSummary summary : originalList) {
            CoinSelectListAdapter.this.add(summary);
        }
        notifyDataSetChanged();
    }
}
