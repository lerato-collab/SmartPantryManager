# Smart Pantry Manager

A Java Android app that helps reduce food waste. You record the ingredients you already have at home, and the app suggests only the recipes you can cook right now, with no shopping trip needed.

## The strict-matching rule
A recipe is suggested only if every ingredient it needs is in your pantry in at least the required quantity. A recipe that is missing even one ingredient never appears in the main list. Matching copes with simple real-world messiness: singular and plural names (tomato and tomatoes) and unit differences (1 kg covers 200 g).

An optional "Almost There" list shows recipes missing exactly one ingredient. It is shown separately from the strict suggestions.

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list screen using a RecyclerView with a custom adapter
- 17 recipes pre-loaded into the database on first run
- Suggested Recipes screen using the strict-matching logic, with a friendly message when nothing matches
- Recipe Detail screen with the full ingredient list and method
- Settings screen: expiring-soon alerts toggle and default unit
- Input validation on the add/edit form
- Bottom navigation bar

No Google Maps, mapping SDKs or location features are used.

## Database choice: SQLite
I used SQLite through `SQLiteOpenHelper`. The app stores one user's pantry and a fixed set of recipes, so a local on-device database is the simplest fit. It needs no account, server or internet connection, and the data persists after the app is closed and reopened.

Tables: `pantry`, `recipes`, `recipe_ingredients`.

## Setup and run instructions
1. Install Android Studio and clone this repository: `git clone https://github.com/YOUR-USERNAME/SmartPantryManager.git`
2. Open the project folder in Android Studio and wait for Gradle sync to finish.
3. Start an emulator (API 24 or higher) or connect an Android phone with USB debugging on.
4. Press Run. The app opens on the Pantry List screen.
5. Add a few ingredients, then open the Recipes tab to see the suggestions.

## Tech
Java, Android Studio, SQLite, RecyclerView, Material Components, SharedPreferences.
