package com.github.hintofbasil.hodl;

import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by will on 07/01/18.
 */


@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void testBasicLoad() {

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start();
        MainActivity activity = controller.get();

        assertNotEquals(activity.findViewById(R.id.total_coin_summary), null);
    }

    @Test
    public void testLoadWithOneCoin() {

        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                true,
                new BigDecimal("14860.2"),
                new BigDecimal("1.5")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start();
        MainActivity activity = controller.get();
        activity.onResume();

        ListView coinListView = activity.findViewById(R.id.coin_summary_list);
        assertNotEquals(coinListView, null);
        assertEquals(coinListView.getAdapter().getCount(), 1);

        assertEquals(
                View.GONE,
                activity.findViewById(R.id.homepage_summary_toolbar).findViewById(R.id.coin_summary_price_missing).getVisibility()
        );

        View firstCoinView = coinListView.getAdapter().getView(0, null, null);
        assertNotEquals(firstCoinView, null);
        assertEquals("Bitcoin", ((TextView)firstCoinView.findViewById(R.id.coin_name)).getText());
        assertEquals("(BTC)", ((TextView)firstCoinView.findViewById(R.id.coin_ticker_symbol)).getText());
        assertEquals("$14860.20", ((TextView)firstCoinView.findViewById(R.id.coin_price_usd)).getText());
        assertEquals("1.5 ($22290.30)", ((TextView)firstCoinView.findViewById(R.id.coin_quantity_and_owned_value)).getText());
    }

    @Test
    public void testLoadWithOneCoin_NullPrice() {

        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                true,
                null,
                new BigDecimal("1.5")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start();
        MainActivity activity = controller.get();
        activity.onResume();

        ListView coinListView = activity.findViewById(R.id.coin_summary_list);
        assertNotEquals(coinListView, null);
        assertEquals(coinListView.getAdapter().getCount(), 1);

        assertEquals(
                View.VISIBLE,
                activity.findViewById(R.id.homepage_summary_toolbar).findViewById(R.id.coin_summary_price_missing).getVisibility()
        );

        View firstCoinView = coinListView.getAdapter().getView(0, null, null);
        assertNotEquals(firstCoinView, null);
        assertEquals("Unknown", ((TextView)firstCoinView.findViewById(R.id.coin_price_usd)).getText());
    }

    @Test
    public void testLoadWithOneCoin_NullPrice_NoQuantity() {

        CoinSummary coin = new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin",
                1,
                true,
                null,
                new BigDecimal("0")
        );

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        coin.addToDatabase(db);

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start();
        MainActivity activity = controller.get();
        activity.onResume();

        assertEquals(
                View.GONE,
                activity.findViewById(R.id.homepage_summary_toolbar).findViewById(R.id.coin_summary_price_missing).getVisibility()
        );
    }

}
