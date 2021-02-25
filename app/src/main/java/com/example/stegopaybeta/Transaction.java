package com.example.stegopaybeta;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("store")
    private String vendor;
    private double amount;
    private String cardID;

    public Transaction(String vendor, double amount, String cardID) {
        this.vendor = vendor;
        this.amount = amount;
        this.cardID = cardID;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }
}
