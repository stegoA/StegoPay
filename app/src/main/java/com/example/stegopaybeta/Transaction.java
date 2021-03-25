package com.example.stegopaybeta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Transaction   {

    private String vendor;
    private String _id;
    private String amount;
    @SerializedName("card")
    private String cardID;
    private String date;
    private String currency;

    @SerializedName("items")
    private ArrayList<Item> itemsList;


    public Transaction(String vendor, String amount, String cardID, String date, String currency, ArrayList<Item> itemsList) {
        this.vendor = vendor;
        this.amount = amount;
        this.cardID = cardID;
        this.date = date;
        this.currency = currency;
        this.itemsList = itemsList;
    }

    public Transaction(String vendor, String amount, String cardID, String date, String currency) {
        this.vendor = vendor;
        this.amount = amount;
        this.cardID = cardID;
        this.date = date;
        this.currency = currency;
    }


    protected Transaction(Parcel in) {
        vendor = in.readString();
        _id = in.readString();
        amount = in.readString();
        cardID = in.readString();
        date = in.readString();
        currency = in.readString();
    }


    public ArrayList<Item> getItemsList() {
        return itemsList;
    }

    public void setItemsList(ArrayList<Item> itemsList) {
        this.itemsList = itemsList;
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
