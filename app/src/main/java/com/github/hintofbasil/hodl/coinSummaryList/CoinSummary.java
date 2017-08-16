package com.github.hintofbasil.hodl.coinSummaryList;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable {

    public CoinSummary(String symbol) {
        this.symbol = symbol;
    }

    private String symbol;
    private String priceUSD;
    private BigDecimal quantity;

    public String getSymbol() {
        return symbol;
    }

    public String getPriceUSD() {
        return priceUSD;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setPriceUSD(String priceUSD) {
        this.priceUSD = priceUSD;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
