package com.github.hintofbasil.hodl.coinSummaryList;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by will on 8/16/17.
 */

public class CoinSummary implements Serializable {

    public static final String COIN_MARKET_CAP_IMAGE_URL = "https://files.coinmarketcap.com/static/img/coins/%dx%d/%s.png";

    public CoinSummary(String symbol, String id) {
        this.symbol = symbol;
        this.id = id;
        this.priceUSD = new BigDecimal(0);
        this.quantity = new BigDecimal(0);
    }

    private String symbol;
    private BigDecimal priceUSD;
    private BigDecimal quantity;
    private String imageURL;
    private String id;

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPriceUSD() {
        return priceUSD;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getImageURL(int size) {
        return String.format(COIN_MARKET_CAP_IMAGE_URL, size, size, this.id);
    }

    public void setPriceUSD(BigDecimal priceUSD) {
        this.priceUSD = priceUSD;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
