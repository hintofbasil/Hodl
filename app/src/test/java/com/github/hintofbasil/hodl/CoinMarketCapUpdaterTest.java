package com.github.hintofbasil.hodl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.security.NetworkSecurityPolicy;

import com.github.hintofbasil.hodl.database.CoinMarketCapUpdaterService;
import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;
import com.github.hintofbasil.hodl.database.schemas.CoinSummarySchema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by will on 22/12/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {CoinMarketCapUpdaterTest.CustomNetworkSecurityPolicy.class})
public class CoinMarketCapUpdaterTest extends BaseTester {

    @Test
    public void testDownloadDataSingle() throws IOException {

        assertEquals(1, 1);
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        List<CoinSummary> lst = implementation.downloadData();
        CoinSummary summary = lst.get(0);

        assertEquals(1, lst.size());
        assertEquals("bitcoin", summary.getId());
        assertEquals("Bitcoin", summary.getName());
        assertEquals("BTC", summary.getSymbol());
        assertEquals(new BigDecimal("14860.2"), summary.getPriceUSD());
        assertEquals(1, summary.getRank());
    }

    @Test
    public void testDownloadDataSingleNullPrice() throws IOException {

        assertEquals(1, 1);
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc_null_price);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        List<CoinSummary> lst = implementation.downloadData();
        CoinSummary summary = lst.get(0);

        assertEquals(1, lst.size());
        assertEquals("bitcoin", summary.getId());
        assertEquals("Bitcoin", summary.getName());
        assertEquals("BTC", summary.getSymbol());
        assertEquals(null, summary.getPriceUSD());
        assertEquals(1, summary.getRank());
    }

    @Test
    public void testGetExistingCoinIds () {
        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        new CoinSummary(
                "BTC",
                "Bitcoin",
                "bitcoin"
        )
                .addToDatabase(db);

        new CoinSummary(
                "XMR",
                "Monero",
                "monero"
        )
                .addToDatabase(db);


        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        List<String> result = implementation.getExistingCoinIds();

        assertEquals(2, result.size());
        assertTrue(result.contains("bitcoin"));
        assertTrue(result.contains("monero"));
    }

    @Test
    public void testProcessAll_AllNew() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc_eth);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        List<CoinSummary> coins = getCoinDataFromDB();

        assertEquals(2, coins.size());

        CoinSummary summary = coins.get(0);

        assertEquals("bitcoin", summary.getId());
        assertEquals("Bitcoin", summary.getName());
        assertEquals("BTC", summary.getSymbol());
        assertEquals(new BigDecimal("14860.2"), summary.getPriceUSD());
        assertEquals(1, summary.getRank());

        summary = coins.get(1);

        assertEquals("ethereum", summary.getId());
        assertEquals("Ethereum", summary.getName());
        assertEquals("ETH", summary.getSymbol());
        assertEquals(new BigDecimal("1027.61"), summary.getPriceUSD());
        assertEquals(2, summary.getRank());
    }


    @Test
    public void testProcessAll_Edit() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc_eth);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        // Add updated data for BTC
        // name, symbol, rank and price_usd updated
        enqueueRawData(mockWebServer, R.raw.coin_json_btc_update);

        implementation.processAll();


        List<CoinSummary> coins = getCoinDataFromDB();

        assertEquals(2, coins.size());

        CoinSummary summary = coins.get(0);

        assertEquals("bitcoin", summary.getId());
        assertEquals("Bitcoin_", summary.getName());
        assertEquals("BTC_", summary.getSymbol());
        assertEquals(new BigDecimal("2"), summary.getPriceUSD());
        assertEquals(3, summary.getRank());
    }

    @Test
    public void testBroadcastsSent_Update() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_100);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        final List<Intent> receivedIntents = new ArrayList<>();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedIntents.add(intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_FAILURE);
        intentFilter.addAction(CoinMarketCapUpdaterService.UPDATE_PROGRESS);
        RuntimeEnvironment.application.registerReceiver(receiver, intentFilter);

        implementation.processAll();

        assertEquals(100, receivedIntents.size());
        for (int i = 0; i < receivedIntents.size(); i++) {
            Intent intent = receivedIntents.get(i);
            assertEquals(intent.getAction(), CoinMarketCapUpdaterService.UPDATE_PROGRESS);
            int progress = intent.getIntExtra(CoinMarketCapUpdaterService.INTENT_UPDATE_PROGRESS, 0);
            assertEquals(i, progress);
        }
    }

    @Test
    public void testBroadcastsSent_Complete() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        final List<Intent> receivedIntents = new ArrayList<>();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedIntents.add(intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_FAILURE);
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_COMPLETED);
        RuntimeEnvironment.application.registerReceiver(receiver, intentFilter);

        implementation.processAll();

        assertEquals(1, receivedIntents.size());
        Intent intent = receivedIntents.get(0);
        assertEquals(intent.getAction(), CoinMarketCapUpdaterService.STATUS_COMPLETED);
    }


    @Test
    public void testBroadcastsSent_Failed() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        final List<Intent> receivedIntents = new ArrayList<>();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedIntents.add(intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_FAILURE);
        intentFilter.addAction(CoinMarketCapUpdaterService.STATUS_COMPLETED);
        RuntimeEnvironment.application.registerReceiver(receiver, intentFilter);

        implementation.processAll();

        assertEquals(1, receivedIntents.size());
        Intent intent = receivedIntents.get(0);
        assertEquals(intent.getAction(), CoinMarketCapUpdaterService.STATUS_FAILURE);
    }

    @Test
    public void testUpdatePriceNull() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.coin_json_btc);
        enqueueRawData(mockWebServer, R.raw.coin_json_btc_null_price);

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();
        implementation.processAll();

        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

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

        assertEquals(new BigDecimal("14860.2"), loaded.getPriceUSD());
    }

    public void enqueueRawData(MockWebServer server, int resource) throws IOException {

        InputStream in = RuntimeEnvironment.application.getResources().openRawResource(resource);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        String data = new String(buffer);

        server.enqueue(new MockResponse().setResponseCode(200).setBody(data));

    }

    public List<CoinSummary> getCoinDataFromDB() {
        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        List<CoinSummary> lst = new ArrayList<>();

        while(cursor.moveToNext()) {
            lst.add(CoinSummary.buildFromCursor(cursor));
        }

        return lst;
    }

    // Allow testing of HTTP calls without throwing an exception
    @Implements(NetworkSecurityPolicy.class)
    public static class CustomNetworkSecurityPolicy {

        @Implementation
        public static NetworkSecurityPolicy getInstance() {
            try {
                Class<?> shadow = CustomNetworkSecurityPolicy.class.forName("android.security.NetworkSecurityPolicy");
                return (NetworkSecurityPolicy) shadow.newInstance();
            } catch (Exception e) {
                throw new AssertionError();
            }
        }

        @Implementation
        public boolean isCleartextTrafficPermitted(String host) {
            return true;
        }

    }

}
