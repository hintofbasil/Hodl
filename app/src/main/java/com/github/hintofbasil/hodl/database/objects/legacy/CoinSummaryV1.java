package com.github.hintofbasil.hodl.database.objects.legacy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.objects.DbObject;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Created by will on 8/16/17.
 */

@Deprecated
public class CoinSummaryV1 extends CoinSummary implements DbObject {

    private CoinSummaryV1(){};

    public CoinSummaryV1(String symbol,
                       String name,
                       String id,
                       int rank,
                       boolean watched,
                       BigDecimal priceUSD,
                       BigDecimal quantity) {
        this.symbol = symbol;
        this.name = name;
        this.id = id;
        this.rank = rank;
        this.watched = watched;
        this.priceUSD = priceUSD;
        this.quantity = quantity;
    }

    @Deprecated
    @Override
    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL, this.getSymbol());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_ID, this.getId());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_NAME, this.getName());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED, this.isWatched());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK, this.getRank());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_VAL, this.getPriceUSD().unscaledValue().intValue());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_SCALE, this.getPriceUSD().scale());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_VAL, this.getQuantity().unscaledValue().intValue());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_SCALE, this.getQuantity().scale());

        return database.insert(CoinSummarySchema.CoinEntry.TABLE_NAME, null, values);
    }

    @Deprecated
    @Override
    public int updateDatabase(SQLiteDatabase database, String... toUpdate) {
        throw new UnsupportedOperationException("This method should never be invoked");
    }

    @Deprecated
    public static CoinSummaryV1 buildFromCursor(Cursor cursor) {

        CoinSummaryV1 summary = new CoinSummaryV1();

        summary.setSymbol(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL)
                )
        );

        summary.setName(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_NAME)
                )
        );

        summary.setId(
                cursor.getString(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_ID)
                )
        );

        summary.setWatched(
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED)
                ) != 0
        );

        summary.setRank(
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK)
                )
        );

        summary.setPriceUSD(
                new BigDecimal(
                        BigInteger.valueOf(cursor.getInt(
                                cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_VAL)
                        )),
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_SCALE)
                        )
                )
        );

        summary.setQuantity(
                new BigDecimal(
                        BigInteger.valueOf(cursor.getInt(
                                cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_VAL)
                        )),
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_SCALE)
                        )
                )
        );

        return summary;
    }

    @Override
    public DbObject upgrade() {
        return new CoinSummary(symbol,
                name,
                id,
                rank,
                watched,
                priceUSD,
                quantity);
    }

    @Override
    public DbObject downgrade() {
        throw new RuntimeException("Can not downgrade CoinSummaryV1");
    }
}
