package com.github.hintofbasil.hodl.database.schemas;

import android.provider.BaseColumns;

import com.github.hintofbasil.hodl.database.objects.ExchangeRate;

/**
 * Created by will on 9/7/17.
 */

public class ExchangeRateSchema {

    public class ExchangeRateEntry implements BaseColumns {

        public static final String TABLE_NAME = "exchange_rate";
        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_EXCHANGE_RATE = "exchange_rate";

        @Deprecated
        public static final String COLUMN_NAME_EXCHANGE_RATE_VAL = "exchange_rate_value";
        @Deprecated
        public static final String COLUMN_NAME_EXCHANGE_RATE_SCALE = "exchange_rate_scale";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ExchangeRateEntry.TABLE_NAME + " (" +
                    ExchangeRateEntry._ID + " INTEGER PRIMARY KEY, " +
                    ExchangeRateEntry.COLUMN_NAME_SYMBOL + " TEXT, " +
                    ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE + " TEXT)";

    @Deprecated
    public static final String SQL_CREATE_ENTRIES_V1 =
            "CREATE TABLE " + ExchangeRateEntry.TABLE_NAME + " (" +
                    ExchangeRateEntry._ID + " INTEGER PRIMARY KEY, " +
                    ExchangeRateEntry.COLUMN_NAME_SYMBOL + " TEXT, " +
                    ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL + " INTEGER, " +
                    ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ExchangeRateEntry.TABLE_NAME;

    public static final String[] allProjection = {
            ExchangeRateEntry._ID,
            ExchangeRateEntry.COLUMN_NAME_SYMBOL,
            ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE
    };

    @Deprecated
    public static final String[] allProjectionV1 = {
            ExchangeRateEntry._ID,
            ExchangeRateEntry.COLUMN_NAME_SYMBOL,
            ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL,
            ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE
    };

}
