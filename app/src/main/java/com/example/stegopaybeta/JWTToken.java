package com.example.stegopaybeta;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class JWTToken {

    @SerializedName("token")
    public String jwtToken;


    public JWTToken() {
    }

    public String getJWTToken() {
        return jwtToken;
    }

    public void setJWTToken(String authToken) {
        this.jwtToken = authToken;
    }

    @NonNull
    @Override
    public String toString() {
        return getJWTToken();
    }
}
