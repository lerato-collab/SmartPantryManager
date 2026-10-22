package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite storage for the pantry (full CRUD) and the pre-seeded recipe collection. */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db); // runs once, the first time the database is created
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------------- Pantry CRUD ----------------

    public long insertItem(PantryItem item) {                     // Create
        return getWritableDatabase().insert("pantry", null, toValues(item));
    }

    public List<PantryItem> getAllItems() {                       // Read (list)
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public PantryItem getItem(long id) {                          // Read (single)
        Cursor c = getReadableDatabase().query("pantry", null, "id=?", new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return item;
    }

    public int updateItem(PantryItem item) {                      // Update
        return getWritableDatabase().update("pantry", toValues(item), "id=?",
                new String[]{String.valueOf(item.id)});
    }

    public int deleteItem(long id) {                              // Delete
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.name);
        v.put("quantity", item.quantity);
        v.put("unit", item.unit);
        v.put("expiry", item.expiry);
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ---------------- Recipes ----------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, null, null, null, null, "name");
        while (c.moveToNext()) {
            Recipe r = new Recipe(c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, r);
            list.add(r);
        }
        c.close();
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(id, c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, r);
        }
        c.close();
        return r;
    }

    private void loadIngredients(SQLiteDatabase db, Recipe r) {
        Cursor c = db.query("recipe_ingredients", null, "recipe_id=?", new String[]{String.valueOf(r.id)},
                null, null, "id");
        while (c.moveToNext()) {
            r.ingredients.add(new RecipeIngredient(c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))));
        }
        c.close();
    }

    // ---------------- Seed data ----------------

    /** Each ingredient is written as "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs and melt the butter in a pan.\n3. Stir the eggs gently until just set.\n4. Pile onto the toast.",
                "egg|3|pcs", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until tender.\n2. Fry chopped onion and garlic in oil.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Toss with the pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|2|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion in oil.\n2. Push aside, scramble the eggs.\n3. Add cooked rice and soy sauce.\n4. Stir fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "cooking oil|1|tbsp");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter in a pan and pour in the eggs.\n3. Add grated cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Pancakes",
                "1. Mix flour, milk, egg and sugar into a smooth batter.\n2. Fry ladles of batter in a hot pan.\n3. Flip when bubbles appear.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|2|tbsp");
        addRecipe(db, "Potato Soup",
                "1. Soften chopped onion in butter.\n2. Add diced potatoes and milk.\n3. Simmer until soft, then mash or blend.",
                "potato|4|pcs", "onion|1|pcs", "butter|20|g", "milk|250|ml");
        addRecipe(db, "Chicken Stir Fry",
                "1. Slice the chicken and vegetables.\n2. Fry the chicken in oil until golden.\n3. Add vegetables and soy sauce.\n4. Cook 5 minutes.",
                "chicken breast|300|g", "carrot|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "cooking oil|1|tbsp");
        addRecipe(db, "Garlic Bread",
                "1. Mash crushed garlic into soft butter.\n2. Spread on the bread.\n3. Bake at 200C for 10 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|3|pcs");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Fill with cheese.\n3. Toast in a pan until golden on both sides.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");
        addRecipe(db, "Banana Smoothie",
                "1. Peel the bananas.\n2. Blend with milk and honey until smooth.",
                "banana|2|pcs", "milk|300|ml", "honey|1|tbsp");
        addRecipe(db, "Vegetable Curry",
                "1. Fry onion in oil, add curry powder.\n2. Add diced potato, carrot and tomato.\n3. Add a splash of water and simmer 20 minutes.",
                "potato|2|pcs", "carrot|2|pcs", "onion|1|pcs", "tomato|2|pcs", "curry powder|2|tsp", "cooking oil|1|tbsp");
        addRecipe(db, "Beans on Toast",
                "1. Warm the beans in a pot.\n2. Toast and butter the bread.\n3. Spoon the beans on top.",
                "baked beans|400|g", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Tuna Mayo Sandwich",
                "1. Drain the tuna and mix with mayonnaise.\n2. Spread on bread and close the sandwich.",
                "tuna|1|pcs", "mayonnaise|2|tbsp", "bread|2|pcs");
        addRecipe(db, "Mashed Potatoes",
                "1. Boil peeled potatoes until soft.\n2. Drain and mash with butter and milk.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml");
        addRecipe(db, "Spaghetti Bolognese",
                "1. Brown the mince with onion and garlic.\n2. Add chopped tomatoes and simmer 20 minutes.\n3. Serve over cooked spaghetti.",
                "spaghetti|200|g", "mince|300|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs");
        addRecipe(db, "Oats Porridge",
                "1. Combine oats and milk in a pot.\n2. Stir over medium heat for 5 minutes.\n3. Drizzle with honey.",
                "oat|50|g", "milk|250|ml", "honey|1|tbsp");
        addRecipe(db, "Simple Greek Salad",
                "1. Chop cucumber and tomatoes.\n2. Crumble in the feta.\n3. Dress with olive oil.",
                "cucumber|1|pcs", "tomato|2|pcs", "feta cheese|100|g", "olive oil|1|tbsp");
    }
}
