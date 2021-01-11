package com.example.stegopaybeta;

public class Activity {

    private String service;
    private double amount;

    public Activity(String service, double amount) {
        this.service = service;
        this.amount = amount;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
