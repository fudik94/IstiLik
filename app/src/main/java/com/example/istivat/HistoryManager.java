package com.example.istivat;

import android.content.SharedPreferences;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class HistoryManager {

    static final String KEY_HISTORY = "history_entries";
    private static final String DELIMITER = "|";

    private final SharedPreferences prefs;

    public HistoryManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    /** Add a new calculation entry at the top of history. */
    public void addEntry(String name, HeatingCalculator.Input input, HeatingCalculator.Result result) {
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date());
        String area = String.format(Locale.getDefault(), "%.1f", input.area);
        String boiler = String.valueOf(result.recommendedBoilerKw);
        String newEntry = name + DELIMITER + date + DELIMITER + area + DELIMITER + boiler;

        String existing = prefs.getString(KEY_HISTORY, "");
        String updated = (existing == null || existing.trim().isEmpty())
                ? newEntry
                : newEntry + "\n" + existing;
        prefs.edit().putString(KEY_HISTORY, updated).apply();
    }

    /** Return all history entries as [date, area, boilerKw] arrays. */
    public List<String[]> getEntries() {
        return parseHistory(prefs.getString(KEY_HISTORY, ""));
    }

    /** Delete the entry at the given index and persist. */
    public void deleteEntryAt(int index) {
        List<String[]> entries = getEntries();
        if (index < 0 || index >= entries.size()) return;
        deleteEntryAt(entries, index);
        prefs.edit().putString(KEY_HISTORY, serializeHistory(entries)).apply();
    }

    /** Clear all history. */
    public void clearAll() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    // ── Static helpers (testable without Android) ──────────────────────────

    /** Parse a raw newline-separated history string into a list of [name, date, area, boilerKw]. */
    public static List<String[]> parseHistory(String raw) {
        List<String[]> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return result;
        for (String line : raw.split("\n")) {
            String[] parts = line.split(Pattern.quote(DELIMITER));
            if (parts.length == 3) {
                // Old format: date|area|boiler → add empty name
                result.add(new String[]{"", parts[0], parts[1], parts[2]});
            } else if (parts.length >= 4) {
                // New format: name|date|area|boiler
                result.add(new String[]{parts[0], parts[1], parts[2], parts[3]});
            }
        }
        return result;
    }

    /** Serialize a list of [name, date, area, boilerKw] back to the raw string format. */
    public static String serializeHistory(List<String[]> entries) {
        if (entries.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            String[] e = entries.get(i);
            sb.append(e[0]).append(DELIMITER).append(e[1]).append(DELIMITER).append(e[2]).append(DELIMITER).append(e[3]);
            if (i < entries.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /** Remove entry at index from the given list (mutates). */
    public static void deleteEntryAt(List<String[]> entries, int index) {
        entries.remove(index);
    }
}
