package com.github.hintofbasil.hodl;

import android.security.NetworkSecurityPolicy;

import com.github.hintofbasil.hodl.database.CoinMarketCapUpdaterService;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;

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

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation();

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

        CoinMarketCapUpdaterService.Implementation implementation = new CoinMarketCapUpdaterService().new Implementation();

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
