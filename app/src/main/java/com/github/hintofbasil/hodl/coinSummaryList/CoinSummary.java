package com.github.hintofbasil.hodl.coinSummaryList;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary {

    public CoinSummary(String symbol) {
        this.symbol = symbol;
    }

    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
