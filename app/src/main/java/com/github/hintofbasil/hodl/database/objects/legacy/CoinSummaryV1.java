package com.github.hintofbasil.hodl.database.objects.legacy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Created by will on 8/16/17.
 */

@Deprecated
public class CoinSummaryV1 extends CoinSummary {

    private CoinSummaryV1(){};

    private CoinSummaryV1(String symbol, String name, String id) {
        super(symbol, name, id);
    };

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

}
