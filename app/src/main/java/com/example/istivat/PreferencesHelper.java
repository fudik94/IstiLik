package com.example.istivat;

import android.content.SharedPreferences;

public final class PreferencesHelper {

    public static final String PREFS_NAME = "heating_prefs";

    // Keys
    static final String KEY_AREA = "area";
    static final String KEY_HEIGHT = "height";
    static final String KEY_RADIATOR_POWER = "radiator_power";
    static final String KEY_FLOOR_AREA = "floor_area";
    static final String KEY_HOUSING = "housing";
    static final String KEY_WALLS = "walls";
    static final String KEY_INSULATION = "insulation";
    static final String KEY_FLOOR = "floor";
    static final String KEY_SYSTEM = "system";
    static final String KEY_DHW = "dhw_points";
    static final String KEY_RESULT_TEXT = "result_text";
    static final String KEY_LANGUAGE = "language";
    static final String KEY_ADVANCED_EXPANDED = "advanced_expanded";

    private static final String DEFAULT_RADIATOR_POWER = "180";
    static final String[] LANGUAGE_CODES = {"az", "ru", "en"};

    private final SharedPreferences prefs;

    public PreferencesHelper(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void save(FormState state) {
        prefs.edit()
                .putString(KEY_AREA, state.area)
                .putString(KEY_HEIGHT, state.height)
                .putString(KEY_RADIATOR_POWER, state.radiatorPower)
                .putString(KEY_FLOOR_AREA, state.floorArea)
                .putInt(KEY_HOUSING, state.housingPosition)
                .putInt(KEY_WALLS, state.wallsPosition)
                .putInt(KEY_INSULATION, state.insulationPosition)
                .putInt(KEY_FLOOR, state.floorPosition)
                .putInt(KEY_SYSTEM, state.systemPosition)
                .putInt(KEY_DHW, state.dhwPosition)
                .putBoolean(KEY_ADVANCED_EXPANDED, state.advancedExpanded)
                .apply();
    }

    public void saveResultText(String text) {
        if (text == null) {
            prefs.edit().remove(KEY_RESULT_TEXT).apply();
        } else {
            prefs.edit().putString(KEY_RESULT_TEXT, text).apply();
        }
    }

    public FormState load() {
        String radiatorPower = prefs.getString(KEY_RADIATOR_POWER, null);
        if (radiatorPower == null || radiatorPower.isEmpty()) {
            radiatorPower = DEFAULT_RADIATOR_POWER;
        }
        return new FormState(
                prefs.getString(KEY_AREA, ""),
                prefs.getString(KEY_HEIGHT, ""),
                radiatorPower,
                prefs.getString(KEY_FLOOR_AREA, ""),
                prefs.getInt(KEY_HOUSING, 0),
                prefs.getInt(KEY_WALLS, 0),
                prefs.getInt(KEY_INSULATION, 0),
                prefs.getInt(KEY_FLOOR, 0),
                prefs.getInt(KEY_SYSTEM, 0),
                prefs.getInt(KEY_DHW, 0),
                prefs.getBoolean(KEY_ADVANCED_EXPANDED, false),
                prefs.getString(KEY_RESULT_TEXT, null)
        );
    }

    public void saveLanguage(String code) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply();
    }

    public String loadLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_CODES[0]);
    }

    // ── FormState ────────────────────────────────────────────────────────────

    public static final class FormState {
        public final String area;
        public final String height;
        public final String radiatorPower;
        public final String floorArea;
        public final int housingPosition;    // 0=apartment, 1=house
        public final int wallsPosition;      // 0-3 → 1-4 walls
        public final int insulationPosition; // 0=good, 1=medium, 2=poor
        public final int floorPosition;      // 0=standard, 1=attic
        public final int systemPosition;     // 0=radiators, 1=underfloor, 2=mixed
        public final int dhwPosition;        // 0-8 → 1-9 points
        public final boolean advancedExpanded;
        public final String resultText;      // null if no result yet

        public FormState(String area, String height, String radiatorPower, String floorArea,
                         int housingPosition, int wallsPosition, int insulationPosition,
                         int floorPosition, int systemPosition, int dhwPosition,
                         boolean advancedExpanded, String resultText) {
            this.area = area;
            this.height = height;
            this.radiatorPower = radiatorPower;
            this.floorArea = floorArea;
            this.housingPosition = housingPosition;
            this.wallsPosition = wallsPosition;
            this.insulationPosition = insulationPosition;
            this.floorPosition = floorPosition;
            this.systemPosition = systemPosition;
            this.dhwPosition = dhwPosition;
            this.advancedExpanded = advancedExpanded;
            this.resultText = resultText;
        }

        public static FormState defaults() {
            return new FormState("", "", "180", "",
                    0, 0, 0, 0, 0, 0, false, null);
        }
    }
}
