package com.example.stegopaybeta;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class StegoCard {

    private Bitmap coverImage;
    private String mappingKey;
    private HashMap<Integer, String> validPixelLocation;
    private int lastFourDigits;
    private String cardNickName;

    public StegoCard(Bitmap coverImage, String mappingKey, HashMap<Integer, String> validPixelLocation, int lastFourDigits, String cardNickName) {
        this.coverImage = coverImage;
        this.mappingKey = mappingKey;
        this.validPixelLocation = validPixelLocation;
        this.lastFourDigits = lastFourDigits;
        this.cardNickName = cardNickName;
    }

    public Bitmap getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Bitmap coverImage) {
        this.coverImage = coverImage;
    }

    public String getMappingKey() {
        return mappingKey;
    }

    public void setMappingKey(String mappingKey) {
        this.mappingKey = mappingKey;
    }

    public HashMap<Integer, String> getValidPixelLocation() {
        return validPixelLocation;
    }

    public void setValidPixelLocation(HashMap<Integer, String> validPixelLocation) {
        this.validPixelLocation = validPixelLocation;
    }

    public int getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(int lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getCardNickName() {
        return cardNickName;
    }

    public void setCardNickName(String cardNickName) {
        this.cardNickName = cardNickName;
    }
}
