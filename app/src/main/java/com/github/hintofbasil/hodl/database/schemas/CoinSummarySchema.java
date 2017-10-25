package com.github.hintofbasil.hodl.database.schemas;

import android.provider.BaseColumns;

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
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_QUANTITY = "quantity";

        @Deprecated
        public static final String COLUMN_NAME_PRICE_VAL = "price_val";
        @Deprecated
        public static final String COLUMN_NAME_PRICE_SCALE = "price_scale";
        @Deprecated
        public static final String COLUMN_NAME_QUANTITY_VAL = "quantity_val";
        @Deprecated
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
                    CoinEntry.COLUMN_NAME_PRICE + " TEXT, " +
                    CoinEntry.COLUMN_NAME_QUANTITY + " TEXT)";

    @Deprecated
    public static final String SQL_CREATE_ENTRIES_V1 =
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

    public static final String[] allProjection = {
            CoinEntry._ID,
            CoinEntry.COLUMN_NAME_SYMBOL,
            CoinEntry.COLUMN_NAME_ID,
            CoinEntry.COLUMN_NAME_NAME,
            CoinEntry.COLUMN_NAME_RANK,
            CoinEntry.COLUMN_NAME_WATCHED,
            CoinEntry.COLUMN_NAME_PRICE,
            CoinEntry.COLUMN_NAME_QUANTITY
    };

    @Deprecated
    public static final String[] allProjectionV1 = {
            CoinEntry._ID,
            CoinEntry.COLUMN_NAME_SYMBOL,
            CoinEntry.COLUMN_NAME_ID,
            CoinEntry.COLUMN_NAME_NAME,
            CoinEntry.COLUMN_NAME_RANK,
            CoinEntry.COLUMN_NAME_WATCHED,
            CoinEntry.COLUMN_NAME_PRICE_VAL,
            CoinEntry.COLUMN_NAME_PRICE_SCALE,
            CoinEntry.COLUMN_NAME_QUANTITY_VAL,
            CoinEntry.COLUMN_NAME_QUANTITY_SCALE
    };
}
