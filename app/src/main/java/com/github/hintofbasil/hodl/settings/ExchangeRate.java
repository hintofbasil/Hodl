package com.github.hintofbasil.hodl.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.github.hintofbasil.hodl.database.ExchangeRateSchema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;

/**
 * Created by will on 9/7/17.
 */

public class ExchangeRate {

    private String symbol;
    private BigDecimal exchangeRate;
    private String name;

    public ExchangeRate(String symbol, BigDecimal exchangeRate) {
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
    }

    public ExchangeRate(String symbol, int exchangeRateValue, int exchangeRateScale) {
        this.symbol = symbol;
        this.exchangeRate = new BigDecimal(BigInteger.valueOf(exchangeRateValue), exchangeRateScale);
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public String getName() {
        if (name == null) {
            if (Build.VERSION.SDK_INT >= 19) {
                this.name = Currency.getInstance(this.symbol).getDisplayName();
            } else {
                this.name = "";
            }
        }
        return name;
    }

    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL, this.symbol);
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL, this.getExchangeRate().unscaledValue().intValue());
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE, this.getExchangeRate().scale());

        return database.insert(ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME, null, values);
    }

    public int updateDatabase(SQLiteDatabase database, String... toUpdate) {
        ContentValues values = new ContentValues();

        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL, this.getExchangeRate().unscaledValue().intValue());
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE, this.getExchangeRate().scale());

        String selection = ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL + " = ?";
        String[] selectionArgs = { this.symbol };

        return database.update(
                ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public static ExchangeRate buildFromCursor(Cursor cursor) {

        String symbol = cursor.getString(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL)
        );

        int exchangeRateValue = cursor.getInt(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL)
        );

        int exchangeRateScale = cursor.getInt(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE)
        );

        return new ExchangeRate(symbol, exchangeRateValue, exchangeRateScale);
    }
}
