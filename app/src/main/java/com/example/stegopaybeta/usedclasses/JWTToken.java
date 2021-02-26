package com.example.stegopaybeta.usedclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//this class is used to retrieve the token so it can be saved as a String in shared preferences when a user logs in
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


}

