package com.crypto.trader.models;

public class TradeAction{


    private String action;
    private Double price;
    private boolean executed=false;
    public static TradeAction Builder(){return new TradeAction();}

    public TradeAction setAction(String action) {
        this.action = action;
        return this;
    }

    public TradeAction setPrice(Double price) {
        this.price = price;
        return this;
    }

    @Override
    public String toString() {
        return "Action : "+action+" price:" + price;
    }
    public String getAction() {
        return action;
    }

    public Double getPrice() {
        return price;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}

