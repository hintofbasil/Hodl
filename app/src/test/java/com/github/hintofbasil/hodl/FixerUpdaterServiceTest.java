package com.github.hintofbasil.hodl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.security.NetworkSecurityPolicy;

import com.github.hintofbasil.hodl.database.DbHelper;
import com.github.hintofbasil.hodl.database.FixerUpdaterService;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;
import com.github.hintofbasil.hodl.database.schemas.ExchangeRateSchema;

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

/**
 * Created by will on 22/12/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {CoinMarketCapUpdaterTest.CustomNetworkSecurityPolicy.class})
public class FixerUpdaterServiceTest extends BaseTester  {

    @Test
    public void testDownloadDataSingle() throws IOException {

        assertEquals(1, 1);
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.fixer_json_gbp);

        HttpUrl url = mockWebServer.url("latest?base=USD");

        FixerUpdaterService.Implementation implementation = new FixerUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        List<ExchangeRate> lst = implementation.downloadData();
        ExchangeRate rate = lst.get(0);

        assertEquals(1, lst.size());
        assertEquals("GBP", rate.getSymbol());
        assertEquals("British Pound Sterling", rate.getName());
        assertEquals(new BigDecimal("0.75268"), rate.getExchangeRate());
    }

    @Test
    public void testProcessAll_AllNew() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.fixer_all);

        HttpUrl url = mockWebServer.url("latest?base=USD");

        FixerUpdaterService.Implementation implementation = new FixerUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        List<ExchangeRate> rates = getExchangeRatesFromDB();

        assertEquals(168, rates.size());

        // Get rate 1 as 0 is USD
        ExchangeRate rate = rates.get(1);

        assertEquals("AED", rate.getSymbol());
        assertEquals("United Arab Emirates Dirham", rate.getName());
        assertEquals(new BigDecimal("3.672704"), rate.getExchangeRate());
    }


    @Test
    public void testProcessAll_Edit() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.fixer_all);

        HttpUrl url = mockWebServer.url("latest?base=USD");

        FixerUpdaterService.Implementation implementation = new FixerUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        enqueueRawData(mockWebServer, R.raw.fixer_all_update);

        implementation.processAll();

        List<ExchangeRate> rates = getExchangeRatesFromDB();

        assertEquals(168, rates.size());

        // Get rate 1 as 0 is USD
        ExchangeRate rate = rates.get(1);

        assertEquals("AED", rate.getSymbol());
        assertEquals("United Arab Emirates Dirham", rate.getName());
        assertEquals(new BigDecimal("4"), rate.getExchangeRate());
    }

    @Test
    public void coinGetNameAPI21() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        enqueueRawData(mockWebServer, R.raw.fixer_all);

        HttpUrl url = mockWebServer.url("latest?base=USD");

        FixerUpdaterService.Implementation implementation = new FixerUpdaterService().new Implementation(RuntimeEnvironment.application);

        URI uri = url.uri();
        String baseUrl = String.format("http://%s", uri.getAuthority());
        implementation.setBaseUrl(baseUrl);

        implementation.processAll();

        enqueueRawData(mockWebServer, R.raw.fixer_all_update);

        implementation.processAll();

        List<ExchangeRate> rates = getExchangeRatesFromDB();

        for (ExchangeRate rate : rates) {
            String name = rate.getName();
        }
    }


    public List<ExchangeRate> getExchangeRatesFromDB() {
        DbHelper dbHelper = new DbHelper(RuntimeEnvironment.application);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                ExchangeRateSchema.ExchangeRateEntry.TABLE_NAME,
                ExchangeRateSchema.allProjection,
                null,
                null,
                null,
                null,
                null
        );

        List<ExchangeRate> lst = new ArrayList<>();

        while(cursor.moveToNext()) {
            lst.add(ExchangeRate.buildFromCursor(cursor));
        }

        return lst;
    }

    public void enqueueRawData(MockWebServer server, int resource) throws IOException {

        InputStream in = RuntimeEnvironment.application.getResources().openRawResource(resource);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        String data = new String(buffer);

        server.enqueue(new MockResponse().setResponseCode(200).setBody(data));

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
