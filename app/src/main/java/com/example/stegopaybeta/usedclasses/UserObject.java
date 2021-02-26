package com.example.stegopaybeta.usedclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//for named JSON object "user"
public class UserObject {

    @SerializedName("user")
    @Expose
    private User userDetails;

    public User getUser() {
        return userDetails;
    }

    public void setUser(User userDetails) {
        this.userDetails = userDetails;
    }
}
