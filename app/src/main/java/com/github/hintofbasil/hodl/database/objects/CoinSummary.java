package com.github.hintofbasil.hodl.database.objects;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.objects.legacy.CoinSummaryV1;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable, Comparable<CoinSummary>, DbObject {

    public static final String COIN_MARKET_CAP_IMAGE_URL = "https://files.coinmarketcap.com/static/img/coins/%dx%d/%s.png";

    protected String symbol;
    protected BigDecimal priceUSD;
    protected BigDecimal quantity;
    protected String id;
    protected String name;
    protected boolean watched;
    protected int rank;

    protected CoinSummary() {}

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

    public CoinSummary(String symbol,
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

    public String getSymbol() {
        return symbol;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPriceUSD() {
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

    public BigDecimal getOwnedValue() {
        return this.priceUSD.multiply(this.quantity);
    }

    public int getRank() {
        return rank;
    }



    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setId(String id) {
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
    public int compareTo(CoinSummary o) {
        // Complex sorting algorithm
        // May be too slow with all coins tracked
        int thisPositive = this.getOwnedValue().signum();
        int thatPositive = o.getOwnedValue().signum();
        if(thisPositive > 0 && thatPositive <= 0) {
            return -1;
        } else if (thisPositive <= 0 && thatPositive > 0) {
            return 1;
        } else if(thisPositive > 0 && thatPositive > 0) {
            return o.getOwnedValue().subtract(this.getOwnedValue()).toBigInteger().intValue();
        }
        return this.getRank() - o.getRank();
    }

    @Override
    public long addToDatabase(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL, this.symbol);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_ID, this.id);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_NAME, this.name);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED, this.watched);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK, this.rank);
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE, this.priceUSD.toPlainString());
        values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY, this.quantity.toPlainString());

        return database.insert(CoinSummarySchema.CoinEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateDatabase(SQLiteDatabase database, String... toUpdate) {
        ContentValues values = new ContentValues();
        List<String> toUpdateList = Arrays.asList(toUpdate);
        if(toUpdateList.contains("symbol")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_SYMBOL, this.symbol);
        }
        if(toUpdateList.contains("name")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_NAME, this.name);
        }
        if(toUpdateList.contains("price")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE, this.priceUSD.toPlainString());
        }
        if(toUpdateList.contains("quantity")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY, this.quantity.toPlainString());
        }
        if(toUpdateList.contains("rank")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK, this.rank);
        }
        if(toUpdateList.contains("watched")) {
            values.put(CoinSummarySchema.CoinEntry.COLUMN_NAME_WATCHED, this.watched);
        }

        String selection = CoinSummarySchema.CoinEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { this.id };

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
                cursor.getString(
                        cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_PRICE)
                )
            )
        );

        summary.setQuantity(
                new BigDecimal(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(CoinSummarySchema.CoinEntry.COLUMN_NAME_QUANTITY)
                        )
                )
        );

        return summary;
    }

    @Override
    public DbObject upgrade() {
        throw new RuntimeException("Can not upgrade CoinSummary");
    }

    @Override
    public DbObject downgrade() {
        return new CoinSummaryV1(symbol,
                name,
                id,
                rank,
                watched,
                priceUSD,
                quantity);
    }
}
