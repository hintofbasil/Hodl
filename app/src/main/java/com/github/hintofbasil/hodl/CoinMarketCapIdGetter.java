package com.github.hintofbasil.hodl;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CoinMarketCap images use an internal id in the URL now
 * This singleton looks up that id a list generated from
 * https://files.coinmarketcap.com/generated/search/quick_search.json
 * This API is likely change so better not to pull data from there each run
 */

public class CoinMarketCapIdGetter {

    public static CoinMarketCapIdGetter instance;

    public static CoinMarketCapIdGetter get() {
        if (instance == null) {
            instance = new CoinMarketCapIdGetter();
        }
        return instance;
    }

    private JSONObject loadedData;

    private CoinMarketCapIdGetter() {};

    public boolean isLoaded() {
        return loadedData != null;
    }

    public void init(Context context) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.coin_market_cap_id_map)));
        String temp;
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        loadedData = new JSONObject(sb.toString());
    }

    public int lookupCmcId(String id) {
        // Return fake value to cause image load to fail
        if (loadedData == null) {
            return -1;
        }
        try {
            return loadedData.getInt(id);
        } catch (JSONException e) {
            return -1;
        }
    }

}
