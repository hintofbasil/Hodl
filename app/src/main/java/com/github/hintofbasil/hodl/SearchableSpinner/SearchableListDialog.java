package com.github.hintofbasil.hodl.SearchableSpinner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.github.hintofbasil.hodl.R;

import java.io.Serializable;
import java.util.List;

/**
 * Modified from https://www.simplifiedcoding.net/android-spinner-with-search/
 */

public class SearchableListDialog extends DialogFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String ITEMS = "items";

    private CoinSelectListAdapter listAdapter;
    private ListView listViewItems;
    private SearchableItem searchableItem;
    private SearchView searchView;

    private String currentSearchText;
    private boolean shouldReopen = false;

    public SearchableListDialog() {

    }

    public static SearchableListDialog newInstance(List items) {
        SearchableListDialog multiSelectExpandableFragment = new
                SearchableListDialog();

        Bundle args = new Bundle();
        args.putSerializable(ITEMS, (Serializable) items);

        multiSelectExpandableFragment.setArguments(args);

        return multiSelectExpandableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Crash on orientation change #7
        // Change Start
        // Description: As the instance was re initializing to null on rotating the device,
        // getting the instance from the saved instance
        if (null != savedInstanceState) {
            searchableItem = (SearchableItem) savedInstanceState.getSerializable("item");
        }
        // Change End

        View rootView = inflater.inflate(R.layout.searchable_list_dialog, null);
        setData(rootView);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setView(rootView);

        alertDialog.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shouldReopen = false;
            }
        });

        alertDialog.setTitle("Select Item");

        // Re-filter after minimize
        if (shouldReopen) {
            searchView.setQuery(currentSearchText, false);
        }

        shouldReopen = true;

        final AlertDialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    // Crash on orientation change #7
    // Change Start
    // Description: Saving the instance of searchable item instance.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("item", searchableItem);
        super.onSaveInstanceState(outState);
    }
    // Change End


    @Override
    public void onCancel(DialogInterface dialog) {
        shouldReopen = false;
        super.onCancel(dialog);
    }

    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
        this.searchableItem = searchableItem;
    }

    private void setData(View rootView) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
                .SEARCH_SERVICE);

        searchView = (SearchView) rootView.findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName
                ()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.clearFocus();
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        // Must reset the filter to stop the previous filtering
        if (shouldReopen) {
            listAdapter.resetFilter();
        }
        List items = (List) getArguments().getSerializable(ITEMS);

        listViewItems = (ListView) rootView.findViewById(R.id.listItems);

        //create the adapter by passing your ArrayList data
        listAdapter = new CoinSelectListAdapter(getActivity(), R.layout.coin_select_spinner_dropdown, items);        //attach the adapter to the list
        listViewItems.setAdapter(listAdapter);

        listViewItems.setTextFilterEnabled(true);

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
                getDialog().dismiss();
                shouldReopen = false;
            }
        });
    }

    @Override
    public boolean onClose() {
        shouldReopen = false;
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (TextUtils.isEmpty(s)) {
            ((CoinSelectListAdapter) listViewItems.getAdapter()).getFilter().filter(null);
            currentSearchText = "";
        } else {
            ((CoinSelectListAdapter) listViewItems.getAdapter()).getFilter().filter(s);
            currentSearchText = s;
        }
        return true;
    }

    public boolean shouldReopen() {
        return shouldReopen;
    }

    public interface SearchableItem<T> extends Serializable {
        void onSearchableItemClicked(T item, int position);
    }
}
