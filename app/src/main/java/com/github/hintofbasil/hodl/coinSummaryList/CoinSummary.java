package com.github.hintofbasil.hodl.coinSummaryList;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable {

    public CoinSummary(String symbol) {
        this.symbol = symbol;
        this.priceUSD = new BigDecimal(0);
        this.quantity = new BigDecimal(0);
    }

    private String symbol;
    private BigDecimal priceUSD;
    private BigDecimal quantity;

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPriceUSD() {
        return priceUSD;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setPriceUSD(BigDecimal priceUSD) {
        this.priceUSD = priceUSD;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
