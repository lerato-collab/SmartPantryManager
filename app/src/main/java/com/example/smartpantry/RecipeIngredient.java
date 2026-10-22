package com.example.smartpantry;

/** One ingredient line inside a recipe. */
public class RecipeIngredient {
    public String name;
    public double quantity;
    public String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
}
