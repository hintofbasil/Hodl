package com.github.hintofbasil.hodl.coinSummaryList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.CoinSummarySchema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable, Comparable {

    public static final String COIN_MARKET_CAP_IMAGE_URL = "https://files.coinmarketcap.com/static/img/coins/%dx%d/%s.png";

    private CoinSummary() {}

    public CoinSummary(String symbol, String name, String id) {
        this.symbol = symbol;
        this.name = name;
        this.id = id;
        this.priceUSD = new BigDecimal(0);
        this.quantity = new BigDecimal(0);
        // Always show Bitcoin on first launch
        if (symbol.equals("BTC")) {
            watched = true;
        } else {
            watched = false;
        }
    }

    private String symbol;
    private BigDecimal priceUSD;
    private BigDecimal quantity;
    private String id;
    private String name;
    private boolean watched;
    private int rank;

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPriceUSD(boolean round) {
        if (round) {
            return priceUSD.setScale(2, BigDecimal.ROUND_DOWN);
        }
        return priceUSD;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getImageURL(int size) {
        return String.format(COIN_MARKET_CAP_IMAGE_URL, size, size, this.id);
    }

    public String getName() {
        return name;
    }

    public boolean isWatched() {
        return watched;
    }

    public BigDecimal getOwnedValue(boolean round) {
        if (round) {
            return this.priceUSD.multiply(this.quantity).setScale(2, BigDecimal.ROUND_DOWN);
        }
        return this.priceUSD.multiply(this.quantity);
    }

    public int getRank() {
        return rank;
    }



    private void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setId(String id) {
        this.id = id;
    }

    public void setPriceUSD(BigDecimal priceUSD) {
        this.priceUSD = priceUSD;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof  CoinSummary) {
            return this.getRank() - ((CoinSummary) o).getRank();
        }
        return 0;
    }

    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL, this.symbol);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_ID, this.id);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_NAME, this.name);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED, this.watched);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK, this.rank);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_VAL, this.priceUSD.unscaledValue().intValue());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_SCALE, this.priceUSD.scale());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_VAL, this.quantity.unscaledValue().intValue());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_SCALE, this.quantity.scale());

        return database.insert(CoinSummarySchema.CoinEntry.TABLE_NAME, null, values);
    }

    public int updateDatabase(SQLiteDatabase database, String... toUpdate) {
        ContentValues values = new ContentValues();
        List<String> toUpdateList = Arrays.asList(toUpdate);
        if(toUpdateList.contains("price")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_VAL, this.priceUSD.unscaledValue().intValue());
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE_SCALE, this.priceUSD.scale());
        }
        if(toUpdateList.contains("quantity")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_VAL, this.quantity.unscaledValue().intValue());
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY_SCALE, this.quantity.scale());
        }
        if(toUpdateList.contains("rank")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK, this.rank);
        }

        String selection = CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL + " LIKE ?";
        String[] selectionArgs = { this.symbol };

        return database.update(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public static CoinSummary buildFromCursor(Cursor cursor) {

        CoinSummary summary = new CoinSummary();

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
