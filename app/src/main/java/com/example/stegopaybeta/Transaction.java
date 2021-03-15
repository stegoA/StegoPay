package com.example.stegopaybeta;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    private String vendor;
    private String _id;
    private String amount;
    @SerializedName("card")
    private String cardID;
    private String date;
    private String currency;

    public Transaction(String vendor, String amount, String cardID, String date, String currency) {
        this.vendor = vendor;
        this.amount = amount;
        this.cardID = cardID;
        this.date = date;
        this.currency = currency;
    }




    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }
}
