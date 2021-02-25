package com.example.stegopaybeta;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Card {

    @SerializedName("_id")
    private String cardID;

    private String nickName;
    private String image;
    private String mappingKey;
    private String last4Digits;
    private HashMap<Integer, String> hashMap_1;
    private HashMap<String, String> hashMap_2;

    public Card(String nickName, String image, String mappingKey, HashMap<Integer, String> hashMap_1, String last4Digits) {
        this.nickName = nickName;
        this.image = image;
        this.mappingKey = mappingKey;
        this.last4Digits = last4Digits;
        this.hashMap_1 = hashMap_1;
    }

    public Card(String cardID, String nickName, String image, HashMap<Integer, String> hashMap_1, HashMap<String, String> hashMap_2, String last4Digits) {
        this.cardID = cardID;
        this.nickName = nickName;
        this.image = image;
        this.last4Digits = last4Digits;
        this.hashMap_1 = hashMap_1;
        this.hashMap_2 = hashMap_2;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getMappingKey() {
        return mappingKey;
    }

    public void setMappingKey(String mappingKey) {
        this.mappingKey = mappingKey;
    }

    public HashMap<Integer, String> getHashMap_1() {
        return hashMap_1;
    }

    public void setHashMap_1(HashMap<Integer, String> hashMap_1) {
        this.hashMap_1 = hashMap_1;
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

    public HashMap<String, String> getHashMap_2() {
        return hashMap_2;
    }

    public void setHashMap_2(HashMap<String, String> hashMap_2) {
        this.hashMap_2 = hashMap_2;
    }
}
