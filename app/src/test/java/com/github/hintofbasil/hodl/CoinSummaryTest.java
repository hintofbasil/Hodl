package com.github.hintofbasil.hodl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Created by will on 22/12/17.
 */

@RunWith(RobolectricTestRunner.class)
public class CoinSummaryTest extends BaseTester  {

    @Test
    public void testSaveCoinSummary() throws IOException {
        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                new BigDecimal("14860.2"),
                new BigDecimal("1.23")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        Cursor cursor = db.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());

        // Must move to next before reading
        cursor.moveToNext();
        CoinSummary loaded = CoinSummary.buildFromCursor(cursor);

        assertEquals("bitcoin", loaded.getId());
        assertEquals("Bitcoin", loaded.getName());
        assertEquals("BTC", loaded.getSymbol());
        assertEquals(new BigDecimal("14860.2"), loaded.getPriceUSD());
        assertEquals(1, loaded.getRank());
        assertEquals(new BigDecimal("1.23"), loaded.getQuantity());
        assertEquals(false, loaded.isWatched());
    }

    @Test
    public void testCoinUpdate() {
        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                new BigDecimal("14860.2"),
                new BigDecimal("1.23")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        coin.setName("Bitcoin 2");
        coin.setRank(2);
        coin.setWatched(true);
        coin.setPriceUSD(new BigDecimal("2"));
        coin.setQuantity(new BigDecimal("3"));

        coin.updateDatabase(db, "name", "rank", "watched", "price", "quantity");

        Cursor cursor = db.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());

        cursor.moveToNext();
        CoinSummary loaded = CoinSummary.buildFromCursor(cursor);

        assertEquals("Bitcoin 2", loaded.getName());
        assertEquals(new BigDecimal("2"), loaded.getPriceUSD());
        assertEquals(2, loaded.getRank());
        assertEquals(new BigDecimal("3"), loaded.getQuantity());
        assertEquals(true, loaded.isWatched());
    }


    @Test
    public void testSaveCoinSummaryNullPrice() throws IOException {
        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                null,
                new BigDecimal("1.23")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        Cursor cursor = db.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());

        cursor.moveToNext();
        CoinSummary loaded = CoinSummary.buildFromCursor(cursor);

        assertEquals("bitcoin", loaded.getId());
        assertEquals("Bitcoin", loaded.getName());
        assertEquals("BTC", loaded.getSymbol());
        assertEquals(null, loaded.getPriceUSD());
        assertEquals(1, loaded.getRank());
        assertEquals(new BigDecimal("1.23"), loaded.getQuantity());
        assertEquals(false, loaded.isWatched());
    }

    @Test
    public void testCoinUpdateNullPrice() {
        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                new BigDecimal("14860.2"),
                new BigDecimal("1.23")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        coin.setName("Bitcoin 2");
        coin.setRank(2);
        coin.setWatched(true);
        coin.setPriceUSD(null);
        coin.setQuantity(new BigDecimal("3"));

        coin.updateDatabase(db, "name", "rank", "watched", "price", "quantity");

        Cursor cursor = db.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());

        cursor.moveToNext();
        CoinSummary loaded = CoinSummary.buildFromCursor(cursor);

        assertEquals(null, loaded.getPriceUSD());
    }

    @Test
    public void testCompare() {
        CoinSummary coinOne = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                new BigDecimal(1),
                new BigDecimal(0)
        );

        CoinSummary coinTwo = new CoinSummary(
                "ETH",
                "Ethereum",
                "ethereum",
                2,
                false,
                new BigDecimal(1),
                new BigDecimal(0)
        );

        assertTrue(coinOne.compareTo(coinTwo) < 0);

        coinOne.setQuantity(new BigDecimal(1));
        coinTwo.setQuantity(new BigDecimal(0));
        assertTrue(coinOne.compareTo(coinTwo) < 0);

        coinOne.setQuantity(new BigDecimal(0));
        coinTwo.setQuantity(new BigDecimal(1));
        assertTrue(coinOne.compareTo(coinTwo) > 0);

        coinOne.setQuantity(new BigDecimal(1));
        coinTwo.setQuantity(new BigDecimal(1));
        assertTrue(coinOne.compareTo(coinTwo) < 0);

        coinOne.setQuantity(new BigDecimal(1));
        coinTwo.setQuantity(new BigDecimal(1));
        coinOne.setPriceUSD(new BigDecimal(1));
        coinTwo.setPriceUSD(new BigDecimal(2));
        assertTrue(coinOne.compareTo(coinTwo) > 0);
    }

    @Test
    public void testCompareToNullPrice() {
        CoinSummary coinOne = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                false,
                null,
                new BigDecimal(1)
        );

        CoinSummary coinTwo = new CoinSummary(
                "ETH",
                "Ethereum",
                "ethereum",
                2,
                false,
                null,
                new BigDecimal(1)
        );

        assertTrue(coinOne.compareTo(coinTwo) < 0);

        coinOne.setPriceUSD(new BigDecimal(1));
        coinTwo.setPriceUSD(null);
        assertTrue(coinOne.compareTo(coinTwo) < 0);

        coinOne.setPriceUSD(null);
        coinTwo.setPriceUSD(new BigDecimal(1));
        assertTrue(coinOne.compareTo(coinTwo) > 0);
    }

}
