package com.github.hintofbasil.hodl.database.objects;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

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
    private String token;

    public ExchangeRate(String symbol, BigDecimal exchangeRate) {
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
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

    public String getToken() {
        if (token == null) {
            token = Currency.getInstance(symbol).getSymbol();
        }
        return token;
    }

    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL, this.symbol);
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE, this.getExchangeRate().toPlainString());

        return database.insert(ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME, null, values);
    }

    public int updateDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();

        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE, this.getExchangeRate().toPlainString());

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

        BigDecimal exchangeRate = new BigDecimal(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE)
                )
        );

        return new ExchangeRate(symbol, exchangeRate);
    }
}
