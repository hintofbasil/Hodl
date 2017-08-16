package com.github.hintofbasil.hodl.coinSummaryList;

import java.io.Serializable;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable {

    public CoinSummary(String symbol) {
        this.symbol = symbol;
    }

    private String symbol;
    private String priceUSD;

    public String getSymbol() {
        return symbol;
    }

    public String getPriceUSD() {
        return priceUSD;
    }

    public void setPriceUSD(String priceUSD) {
        this.priceUSD = priceUSD;
    }
}
