package com.github.hintofbasil.hodl;

import com.github.hintofbasil.hodl.database.CoinMarketCapUpdaterService;
import com.github.hintofbasil.hodl.database.objects.CoinSummary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.*;

/**
 * Created by will on 22/12/17.
 */

@RunWith(MockitoJUnitRunner.class)
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

        List<CoinSummary> lst = implementation.downloadData(url.toString());
        CoinSummary summary = lst.get(0);

        assertEquals(1, lst.size());
        assertEquals("bitcoin", summary.getId());
        assertEquals("Bitcoin", summary.getName());
        assertEquals("BTC", summary.getSymbol());
        assertEquals(new BigDecimal("14860.2"), summary.getPriceUSD());
        assertEquals(1, summary.getRank());
    }

}
