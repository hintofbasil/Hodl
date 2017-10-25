package com.github.hintofbasil.hodl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.objects.legacy.CoinSummaryV1;
import com.github.hintofbasil.hodl.database.objects.legacy.ExchangeRateV1;
import com.github.hintofbasil.hodl.database.patches.AddTablePatch;
import com.github.hintofbasil.hodl.database.patches.Patch;
import com.github.hintofbasil.hodl.database.patches.UpdateColumnsPatch;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

import java.math.BigDecimal;

/**
 * Created by will on 8/21/17.
 *
 *  Version 1: Create coin_summary table
 *  Version 2: Create exchange_rate table
 *  Version 3: Update CoinSummary columns to save BigDecimal as a String and migrate data
 *  Version 4: Updated ExchangeRate columns to save BigDecimal as a String and migrate data
 *
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "CoinSummary.db";

    public static final Patch[] PATCHES = new Patch[] {
            new AddTablePatch(CoinSummarySchema.SQL_CREATE_ENTRIES_V1, CoinSummarySchema.SQL_DELETE_ENTRIES),
            new AddTablePatch(ExchangeRateSchema.SQL_CREATE_ENTRIES_V1, ExchangeRateSchema.SQL_DELETE_ENTRIES) {
                @Override
                public void apply(SQLiteDatabase sqLiteDatabase) {
                    super.apply(sqLiteDatabase);
                    // Insert USD with exchange rate of 1
                    // USD is never returned from fixer.io as it is the base rate
                    ExchangeRateV1 usdExchangeRate = new ExchangeRateV1("USD", new BigDecimal(1));
                    usdExchangeRate.addToDatabase(sqLiteDatabase);
                }
            },
            new UpdateColumnsPatch(
                    CoinSummarySchema.CoinEntry.TABLE_NAME,
                    CoinSummarySchema.SQL_CREATE_ENTRIES_V1,
                    CoinSummarySchema.SQL_CREATE_ENTRIES,
                    CoinSummarySchema.allProjectionV1,
                    CoinSummarySchema.allProjection,
                    CoinSummaryV1.class,
                    CoinSummary.class
            ),
            new UpdateColumnsPatch(
                    ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                    ExchangeRateSchema.SQL_CREATE_ENTRIES_V1,
                    ExchangeRateSchema.SQL_CREATE_ENTRIES,
                    ExchangeRateSchema.allProjectionV1,
                    ExchangeRateSchema.allProjection,
                    ExchangeRateV1.class,
                    ExchangeRate.class
            )
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
        Log.d("DbHelper", String.format("Upgrading database %d -> %d", oldVersion, newVersion));
        try {
            for (int i = oldVersion; i < newVersion; i++) {
                PATCHES[i].apply(sqLiteDatabase);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("DbHelper", String.format("Downgrading database %d -> %d", oldVersion, newVersion));
        try {
            for (int i = oldVersion - 1; i >= newVersion; i--) {
                PATCHES[i].revert(sqLiteDatabase);
            }
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
