package com.github.hintofbasil.hodl.database.objects.legacy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.DbObject;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by will on 9/7/17.
 */

@Deprecated
public class ExchangeRateV1 extends ExchangeRate implements DbObject {

    public ExchangeRateV1(String symbol, BigDecimal exchangeRate) {
        this.symbol = symbol;
        this.exchangeRate = exchangeRate;
    }

    public ExchangeRateV1(String symbol, int exchangeRateValue, int exchangeRateScale) {
        this.symbol = symbol;
        this.exchangeRate = new BigDecimal(BigInteger.valueOf(exchangeRateValue), exchangeRateScale);
    }

    @Override
    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL, this.symbol);
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL, this.getExchangeRate().unscaledValue().intValue());
        values.put(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE, this.getExchangeRate().scale());

        return database.insert(ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateDatabase(SQLiteDatabase database, String... toUpdate) {
        throw new UnsupportedOperationException("This method should never be invoked");
    }

    public static ExchangeRateV1 buildFromCursor(Cursor cursor) {

        String symbol = cursor.getString(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_SYMBOL)
        );

        int exchangeRateValue = cursor.getInt(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_VAL)
        );

        int exchangeRateScale = cursor.getInt(
                cursor.getColumnIndexOrThrow(ExchangeRateSchema.ExchangeRateEntry.COLUMN_NAME_EXCHANGE_RATE_SCALE)
        );

        return new ExchangeRateV1(symbol, exchangeRateValue, exchangeRateScale);
    }

    @Override
    public DbObject upgrade() {
        return new ExchangeRate(
                symbol,
                exchangeRate
        );
    }

    @Override
    public DbObject downgrade() {
        throw new RuntimeException("Can not downgrade ExchangeRateV1");
    }
}
