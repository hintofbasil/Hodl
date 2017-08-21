package com.github.hintofbasil.hodl.database;

import android.provider.BaseColumns;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;

/**
 * Created by will on 8/21/17.
 */

public class CoinSummarySchema {

    private CoinSummarySchema() {}

    public class CoinEntry implements BaseColumns {

        public static final String TABLE_NAME = "coin_summary";
        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_RANK = "rank";
        public static final String COLUMN_NAME_WATCHED = "watched";
        public static final String COLUMN_NAME_PRICE_VAL = "price_val";
        public static final String COLUMN_NAME_PRICE_SCALE = "price_scale";
        public static final String COLUMN_NAME_QUANTITY_VAL = "quantity_val";
        public static final String COLUMN_NAME_QUANTITY_SCALE = "quantity_scale";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CoinEntry.TABLE_NAME + " (" +
                    CoinEntry._ID + " INTEGER PRIMARY KEY, " +
                    CoinEntry.COLUMN_NAME_SYMBOL + " TEXT, " +
                    CoinEntry.COLUMN_NAME_ID + " TEXT, " +
                    CoinEntry.COLUMN_NAME_NAME + " TEXT, " +
                    CoinEntry.COLUMN_NAME_RANK + " INTEGER, " +
                    CoinEntry.COLUMN_NAME_WATCHED + " INTEGER, " +
                    CoinEntry.COLUMN_NAME_PRICE_VAL + " INTEGER, " +
                    CoinEntry.COLUMN_NAME_PRICE_SCALE + " INTEGER, " +
                    CoinEntry.COLUMN_NAME_QUANTITY_VAL + " INTEGER, " +
                    CoinEntry.COLUMN_NAME_QUANTITY_SCALE + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CoinEntry.TABLE_NAME;
}
