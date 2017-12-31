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
public class CoinSummaryTest {

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

}
