package com.example.stegopaybeta;

public class UserProfileModel {

    private String userID;
    private String firstName;
    private String lastName;
    private String email;

    public UserProfileModel() {
        this.userID = "0";
        this.firstName = "N/A";
        this.lastName = "N/A";
        this.email = "N/A";
    }

    public UserProfileModel(String userID, String firstName, String lastName, String email) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
