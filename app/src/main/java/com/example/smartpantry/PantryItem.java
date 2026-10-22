package com.example.smartpantry;

/** One ingredient the user currently has at home. */
public class PantryItem {
    public long id;
    public String name;
    public double quantity;
    public String unit;
    public String expiry; // yyyy-MM-dd, or empty when not set

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }
}
