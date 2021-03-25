package com.example.stegopaybeta;

import com.google.gson.annotations.SerializedName;

public class UserStats {

    private String _id;
    @SerializedName("vendor")
    private String vendorName;
    @SerializedName("total")
    private String totalSpending;
    @SerializedName("number_of_transactions")
    private String totalPurchases;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public UserStats(String _id, String vendorName, String totalSpending, String totalPurchases) {
        this._id = _id;
        this.vendorName = vendorName;
        this.totalSpending = totalSpending;
        this.totalPurchases = totalPurchases;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(String totalSpending) {
        this.totalSpending = totalSpending;
    }

    public String getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(String totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
}
