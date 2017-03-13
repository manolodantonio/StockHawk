package com.udacity.stockhawk.data;

/**
 * Created by Manolo on 13/03/2017.
 */

public class QuoteObject {

    private String symbol;
    private String price;
    private String absoluteChange;
    private String percentageChange;
    private String history;

    public QuoteObject(String symbol, String price, String absoluteChange, String percentageChange, String history) {
        this.symbol = symbol;
        this.price = price;
        this.absoluteChange = absoluteChange;
        this.percentageChange = percentageChange;
        this.history = history;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAbsoluteChange() {
        return absoluteChange;
    }

    public void setAbsoluteChange(String absoluteChange) {
        this.absoluteChange = absoluteChange;
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(String percentageChange) {
        this.percentageChange = percentageChange;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
