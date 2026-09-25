package com.example.smartpantrymanager;

public class PantryItem {

    private int id;
    private String name;
    private String quantity;
    private String expiry;

    public PantryItem(int id, String name, String quantity, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expiry = expiry;
    }

    public PantryItem(String name, String quantity, String expiry) {
        this.name = name;
        this.quantity = quantity;
        this.expiry = expiry;
    }

    public int getId() {
        return id;
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

    public void setId(int id) {
        this.id = id;
    }
}