package com.github.hintofbasil.hodl.database.schemas;

import android.provider.BaseColumns;

/**
 * Created by will on 9/7/17.
 */

public class ExchangeRateSchema {

        public class ExchangeRateEntry implements BaseColumns {

        public static final String TABLE_NAME = "exchange_rate";
        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_EXCHANGE_RATE = "exchange_rate";;

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ExchangeRateEntry.TABLE_NAME + " (" +
                    ExchangeRateEntry._ID + " INTEGER PRIMARY KEY, " +
                    ExchangeRateEntry.COLUMN_NAME_SYMBOL + " TEXT, " +
                    ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE + " TEXT)";

    public static final String[] allProjection = {
            ExchangeRateEntry._ID,
            ExchangeRateEntry.COLUMN_NAME_SYMBOL,
            ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE
    };

}
