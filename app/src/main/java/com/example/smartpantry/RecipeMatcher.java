package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule (Section 2.3 of the brief).
 * A recipe is "suggested" only if EVERY ingredient is in the pantry in at least the required quantity.
 */
public class RecipeMatcher {

    public static class Result {
        public final List<Recipe> strict = new ArrayList<>();
        public final List<Recipe> almost = new ArrayList<>(); // bonus: missing exactly one ingredient
    }

    public static Result match(List<PantryItem> pantry, List<Recipe> recipes) {
        // Step 1: total up the pantry per (normalised name + unit dimension), in base units.
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            String key = key(p.name, p.unit);
            double base = UnitUtil.toBase(p.quantity, p.unit);
            Double existing = stock.get(key);
            stock.put(key, existing == null ? base : existing + base);
        }

        // Step 2: check every ingredient of every recipe against that stock.
        Result result = new Result();
        for (Recipe r : recipes) {
            int missing = 0;
            String missingName = null;
            for (RecipeIngredient ing : r.ingredients) {
                Double have = stock.get(key(ing.name, ing.unit));
                double need = UnitUtil.toBase(ing.quantity, ing.unit);
                if (have == null || have + 1e-9 < need) {
                    missing++;
                    missingName = ing.name;
                }
            }
            if (missing == 0) {
                r.missingNote = null;
                result.strict.add(r);
            } else if (missing == 1) {
                r.missingNote = missingName;
                result.almost.add(r);
            }
        }
        return result;
    }

    private static String key(String name, String unit) {
        return UnitUtil.normalizeName(name) + "|" + UnitUtil.dimension(unit);
    }
}
