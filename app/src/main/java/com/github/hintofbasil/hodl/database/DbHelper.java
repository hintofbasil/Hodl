package com.github.hintofbasil.hodl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.patches.AddTablePatch;
import com.github.hintofbasil.hodl.database.patches.Patch;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

import java.math.BigDecimal;

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

    public static final Patch[] PATCHES = new Patch[] {
            new AddTablePatch(CoinSummarySchema.SQL_CREATE_ENTRIES, CoinSummarySchema.SQL_DELETE_ENTRIES),
            new AddTablePatch(ExchangeRateSchema.SQL_CREATE_ENTRIES, ExchangeRateSchema.SQL_DELETE_ENTRIES) {
                @Override
                public void apply(SQLiteDatabase sqLiteDatabase) {
                    super.apply(sqLiteDatabase);
                    // Insert USD with exchange rate of 1
                    // USD is never returned from fixer.io as it is the base rate
                    ExchangeRate usdExchangeRate = new ExchangeRate("USD", new BigDecimal(1));
                    usdExchangeRate.addToDatabase(sqLiteDatabase);
                }
            }
    };

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        onUpgrade(sqLiteDatabase, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            PATCHES[i].apply(sqLiteDatabase);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        for (int i = oldVersion - 1; i >= newVersion; i--) {
            PATCHES[i].revert(sqLiteDatabase);
        }
    }
}
