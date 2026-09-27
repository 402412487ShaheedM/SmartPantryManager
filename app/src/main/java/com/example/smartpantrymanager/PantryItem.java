
package com.example.smartpantrymanager;

public class PantryItem {

    private int id;
    private String name;
    private String quantity;
    private String expiry;
    private String category;

    public PantryItem() {
    }

    public PantryItem(int id, String name, String quantity, String expiry, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expiry = expiry;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}