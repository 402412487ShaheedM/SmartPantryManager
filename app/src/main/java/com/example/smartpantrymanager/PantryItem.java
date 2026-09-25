package com.example.smartpantrymanager;

public class PantryItem {

    private String name;
    private String quantity;
    private String expiry;

    public PantryItem(String name, String quantity, String expiry) {
        this.name = name;
        this.quantity = quantity;
        this.expiry = expiry;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}