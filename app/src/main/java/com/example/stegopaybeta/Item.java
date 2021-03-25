package com.example.stegopaybeta;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable  {

    private String _id;
    private String product_id;
    private String name;
    private String quantity;
    private String product_price;

    public Item(String _id, String product_id, String name, String quantity, String product_price) {
        this._id = _id;
        this.product_id = product_id;
        this.name = name;
        this.quantity = quantity;
        this.product_price = product_price;
    }


    protected Item(Parcel in) {
        _id = in.readString();
        product_id = in.readString();
        name = in.readString();
        quantity = in.readString();
        product_price = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(product_id);
        dest.writeString(name);
        dest.writeString(quantity);
        dest.writeString(product_price);
    }
}
