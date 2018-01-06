package com.github.hintofbasil.hodl;

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
public class CoinMarketCapUpdaterTest {

    @Test
    public void testDownloadDataSingle() throws IOException {

        assertEquals(1, 1);
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setBody(
                "[\n" +
                "    {\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"rank\": \"1\", \n" +
                "        \"price_usd\": \"14860.2\", \n" +
                "        \"price_btc\": \"1.0\", \n" +
                "        \"24h_volume_usd\": \"23315000000.0\", \n" +
                "        \"market_cap_usd\": \"249019801500\", \n" +
                "        \"available_supply\": \"16757500.0\", \n" +
                "        \"total_supply\": \"16757500.0\", \n" +
                "        \"max_supply\": \"21000000.0\", \n" +
                "        \"percent_change_1h\": \"3.24\", \n" +
                "        \"percent_change_24h\": \"-5.87\", \n" +
                "        \"percent_change_7d\": \"-16.2\", \n" +
                "        \"last_updated\": \"1513983258\"\n" +
                "    }\n" +
                "]"
        ));

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
        mockWebServer.enqueue(new MockResponse().setBody(
                "[\n" +
                        "    {\n" +
                        "        \"id\": \"bitcoin\", \n" +
                        "        \"name\": \"Bitcoin\", \n" +
                        "        \"symbol\": \"BTC\", \n" +
                        "        \"rank\": \"1\", \n" +
                        "        \"price_usd\": null, \n" +
                        "        \"price_btc\": \"1.0\", \n" +
                        "        \"24h_volume_usd\": \"23315000000.0\", \n" +
                        "        \"market_cap_usd\": \"249019801500\", \n" +
                        "        \"available_supply\": \"16757500.0\", \n" +
                        "        \"total_supply\": \"16757500.0\", \n" +
                        "        \"max_supply\": \"21000000.0\", \n" +
                        "        \"percent_change_1h\": \"3.24\", \n" +
                        "        \"percent_change_24h\": \"-5.87\", \n" +
                        "        \"percent_change_7d\": \"-16.2\", \n" +
                        "        \"last_updated\": \"1513983258\"\n" +
                        "    }\n" +
                        "]"
        ));

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
        mockWebServer.enqueue(new MockResponse().setBody(
                "[\n" +
                        "    {\n" +
                        "        \"id\": \"bitcoin\", \n" +
                        "        \"name\": \"Bitcoin\", \n" +
                        "        \"symbol\": \"BTC\", \n" +
                        "        \"rank\": \"1\", \n" +
                        "        \"price_usd\": \"14860.2\", \n" +
                        "        \"price_btc\": \"1.0\", \n" +
                        "        \"24h_volume_usd\": \"23315000000.0\", \n" +
                        "        \"market_cap_usd\": \"249019801500\", \n" +
                        "        \"available_supply\": \"16757500.0\", \n" +
                        "        \"total_supply\": \"16757500.0\", \n" +
                        "        \"max_supply\": \"21000000.0\", \n" +
                        "        \"percent_change_1h\": \"3.24\", \n" +
                        "        \"percent_change_24h\": \"-5.87\", \n" +
                        "        \"percent_change_7d\": \"-16.2\", \n" +
                        "        \"last_updated\": \"1513983258\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": \"ethereum\", \n" +
                        "        \"name\": \"Ethereum\", \n" +
                        "        \"symbol\": \"ETH\", \n" +
                        "        \"rank\": \"2\", \n" +
                        "        \"price_usd\": \"1027.61\", \n" +
                        "        \"price_btc\": \"0.0687759\", \n" +
                        "        \"24h_volume_usd\": \"6954780000.0\", \n" +
                        "        \"market_cap_usd\": \"99441354962.0\", \n" +
                        "        \"available_supply\": \"96769548.0\", \n" +
                        "        \"total_supply\": \"96769548.0\", \n" +
                        "        \"max_supply\": null, \n" +
                        "        \"percent_change_1h\": \"-0.78\", \n" +
                        "        \"percent_change_24h\": \"12.65\", \n" +
                        "        \"percent_change_7d\": \"41.14\", \n" +
                        "        \"last_updated\": \"1515090249\"\n" +
                        "    }" +
                        "]"
        ));

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
        mockWebServer.enqueue(new MockResponse().setBody(
                "[\n" +
                        "    {\n" +
                        "        \"id\": \"bitcoin\", \n" +
                        "        \"name\": \"Bitcoin\", \n" +
                        "        \"symbol\": \"BTC\", \n" +
                        "        \"rank\": \"1\", \n" +
                        "        \"price_usd\": \"14860.2\", \n" +
                        "        \"price_btc\": \"1.0\", \n" +
                        "        \"24h_volume_usd\": \"23315000000.0\", \n" +
                        "        \"market_cap_usd\": \"249019801500\", \n" +
                        "        \"available_supply\": \"16757500.0\", \n" +
                        "        \"total_supply\": \"16757500.0\", \n" +
                        "        \"max_supply\": \"21000000.0\", \n" +
                        "        \"percent_change_1h\": \"3.24\", \n" +
                        "        \"percent_change_24h\": \"-5.87\", \n" +
                        "        \"percent_change_7d\": \"-16.2\", \n" +
                        "        \"last_updated\": \"1513983258\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": \"ethereum\", \n" +
                        "        \"name\": \"Ethereum\", \n" +
                        "        \"symbol\": \"ETH\", \n" +
                        "        \"rank\": \"2\", \n" +
                        "        \"price_usd\": \"1027.61\", \n" +
                        "        \"price_btc\": \"0.0687759\", \n" +
                        "        \"24h_volume_usd\": \"6954780000.0\", \n" +
                        "        \"market_cap_usd\": \"99441354962.0\", \n" +
                        "        \"available_supply\": \"96769548.0\", \n" +
                        "        \"total_supply\": \"96769548.0\", \n" +
                        "        \"max_supply\": null, \n" +
                        "        \"percent_change_1h\": \"-0.78\", \n" +
                        "        \"percent_change_24h\": \"12.65\", \n" +
                        "        \"percent_change_7d\": \"41.14\", \n" +
                        "        \"last_updated\": \"1515090249\"\n" +
                        "    }" +
                        "]"
        ));

        HttpUrl url = mockWebServer.url("/v1/ticker/?limit=0");

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        // Add updated data for BTC
        // name, symbol, rank and price_usd updated
        mockWebServer.enqueue(new MockResponse().setBody(
                "[\n" +
                        "    {\n" +
                        "        \"id\": \"bitcoin\", \n" +
                        "        \"name\": \"Bitcoin_\", \n" +
                        "        \"symbol\": \"BTC_\", \n" +
                        "        \"rank\": \"3\", \n" +
                        "        \"price_usd\": \"2\", \n" +
                        "        \"price_btc\": \"1.0\", \n" +
                        "        \"24h_volume_usd\": \"23315000000.0\", \n" +
                        "        \"market_cap_usd\": \"249019801500\", \n" +
                        "        \"available_supply\": \"16757500.0\", \n" +
                        "        \"total_supply\": \"16757500.0\", \n" +
                        "        \"max_supply\": \"21000000.0\", \n" +
                        "        \"percent_change_1h\": \"3.24\", \n" +
                        "        \"percent_change_24h\": \"-5.87\", \n" +
                        "        \"percent_change_7d\": \"-16.2\", \n" +
                        "        \"last_updated\": \"1513983258\"\n" +
                        "    }" +
                        "]"
        ));

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
