package com.github.hintofbasil.hodl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

/**
 * Created by will on 8/21/17.
 *
 *  Version 1: Create coin_summary table
 *  Version 2: Create exchange_rate table
 *
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "CoinSummary.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CoinSummarySchema.SQL_CREATE_ENTRIES);
        sqLiteDatabase.execSQL(ExchangeRateSchema.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(ExchangeRateSchema.SQL_CREATE_ENTRIES);
        }
    }
}
