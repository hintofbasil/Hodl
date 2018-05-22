package com.github.hintofbasil.hodl;

import android.security.NetworkSecurityPolicy;

import com.github.hintofbasil.hodl.database.FixerUpdaterService;
import com.github.hintofbasil.hodl.database.objects.ExchangeRate;

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
public class FixerUpdaterServiceTest {

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
        assertEquals(new BigDecimal("0.74185"), rate.getExchangeRate());
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
