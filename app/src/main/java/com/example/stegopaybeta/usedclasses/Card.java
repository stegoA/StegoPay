package com.example.stegopaybeta.usedclasses;

import java.util.HashMap;

public class Card {
    private String _id;
    private String nickName;
    private String image;
    private String last4Digits;
    private HashMap<Integer, String> hashMap_1;
    private HashMap<String, String> hashMap_2;

    //Constructor for making call to the Server
    public Card(String nickName, String image, String last4Digits, HashMap<Integer, String> hashMap_1) {
        this.nickName = nickName;
        this.image = image;
        this.last4Digits = last4Digits;
        this.hashMap_1 = hashMap_1;
    }

    public Card(String _id, String nickName, String image, String last4Digits, HashMap<Integer, String> hashMap_1, HashMap<String, String> hashMap_2) {
        this._id = _id;
        this.nickName = nickName;
        this.image = image;
        this.last4Digits = last4Digits;
        this.hashMap_1 = hashMap_1;
        this.hashMap_2 = hashMap_2;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public HashMap<Integer, String> getHashMap_1() {
        return hashMap_1;
    }

    public void setHashMap_1(HashMap<Integer, String> hashMap_1) {
        this.hashMap_1 = hashMap_1;
    }

    public HashMap<String, String> getHashMap_2() {
        return hashMap_2;
    }

    public void setHashMap_2(HashMap<String, String> hashMap_2) {
        this.hashMap_2 = hashMap_2;
    }

    //
    //@Override
    //public String toString() {
    //return "card [image = " + image + ", nickName = " + nickName + ", _id = " + _id + ", last4Digits = " + last4Digits + "]";
    //}
}

