package com.example.smartpantry;

import java.util.Locale;

/**
 * Helpers that make matching robust against everyday messiness:
 * different units (kg vs g) and singular/plural names (tomato vs tomatoes).
 */
public final class UnitUtil {

    public static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private UnitUtil() { }

    /** Groups units so we only ever compare like with like (mass with mass, and so on). */
    public static String dimension(String unit) {
        switch (unit) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            default:
                return "count";
        }
    }

    /** Converts a quantity to the base unit of its dimension (grams, millilitres or pieces). */
    public static double toBase(double qty, String unit) {
        switch (unit) {
            case "kg":   return qty * 1000;
            case "l":    return qty * 1000;
            case "tsp":  return qty * 5;
            case "tbsp": return qty * 15;
            case "cup":  return qty * 240;
            default:     return qty; // g, ml, pcs are already base units
        }
    }

    /** Lower-cases, strips punctuation and singularises the last word of an ingredient name. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", "").trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return s;
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace >= 0 ? s.substring(0, lastSpace + 1) : "";
        String last = lastSpace >= 0 ? s.substring(lastSpace + 1) : s;
        return head + singular(last);
    }

    private static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";   // cherries -> cherry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);         // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes")) {
            return w.substring(0, w.length() - 2);                            // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us")) return w;                   // hummus stays hummus
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);           // eggs -> egg
        return w;
    }

    public static String formatQty(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return String.format(Locale.US, "%.2f", q);
    }
}
