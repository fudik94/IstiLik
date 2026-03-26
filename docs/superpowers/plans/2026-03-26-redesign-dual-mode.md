# IstiLik Redesign & Dual Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign IstiLik with a dark theme, card-based layout, and simple/advanced dual-mode input backed by extracted helper classes.

**Architecture:** MainActivity becomes a thin Fragment host with BottomNavigationView (2 tabs). CalculatorFragment owns all form logic with live results and an expandable advanced section. HistoryFragment shows a RecyclerView with swipe-to-delete. HistoryManager and PreferencesHelper extract logic from the 730-line MainActivity.

**Tech Stack:** Java, Material Design 3 (already in project), AndroidX Fragments, RecyclerView (transitive via appcompat), no new Gradle dependencies.

---

## File Map

| Action | File |
|--------|------|
| Modify | `app/src/main/res/values/colors.xml` |
| Modify | `app/src/main/res/values/themes.xml` |
| Modify | `app/src/main/res/values/bools.xml` |
| Modify | `app/src/main/res/values/strings.xml` |
| Modify | `app/src/main/res/values-en/strings.xml` |
| Modify | `app/src/main/res/values-ru/strings.xml` |
| Create | `app/src/main/res/menu/bottom_nav_menu.xml` |
| Create | `app/src/main/res/layout/item_history_card.xml` |
| Create | `app/src/main/res/layout/fragment_history.xml` |
| Create | `app/src/main/res/layout/fragment_calculator.xml` |
| Modify | `app/src/main/res/layout/activity_main.xml` |
| Create | `app/src/main/java/com/example/istivat/HistoryManager.java` |
| Create | `app/src/main/java/com/example/istivat/PreferencesHelper.java` |
| Create | `app/src/main/java/com/example/istivat/HistoryAdapter.java` |
| Create | `app/src/main/java/com/example/istivat/HistoryFragment.java` |
| Create | `app/src/main/java/com/example/istivat/CalculatorFragment.java` |
| Modify | `app/src/main/java/com/example/istivat/MainActivity.java` |
| Create | `app/src/test/java/com/example/istivat/HistoryManagerTest.java` |
| Create | `app/src/test/java/com/example/istivat/PreferencesHelperTest.java` |

---

## Task 1: Dark Theme — Colors and Theme

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values/bools.xml`

- [ ] **Step 1: Replace colors.xml**

```xml
<!-- app/src/main/res/values/colors.xml -->
<resources>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>

    <!-- Dark theme palette -->
    <color name="bg_app">#0F0F14</color>
    <color name="bg_card">#1A1A24</color>
    <color name="accent_primary">#FF6B35</color>
    <color name="accent_secondary">#FFB347</color>
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#8A8A9A</color>
    <color name="divider">#2A2A38</color>

    <!-- Material theme roles mapped to dark palette -->
    <color name="primary">#FF6B35</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="secondary">#FFB347</color>
    <color name="on_secondary">#1A1A24</color>
    <color name="surface">#1A1A24</color>
    <color name="on_surface">#FFFFFF</color>
    <color name="background">#0F0F14</color>
    <color name="on_background">#FFFFFF</color>
    <color name="surface_variant">#252535</color>
    <color name="on_surface_variant">#8A8A9A</color>
    <color name="outline">#3A3A4A</color>
</resources>
```

- [ ] **Step 2: Replace themes.xml**

```xml
<!-- app/src/main/res/values/themes.xml -->
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.IstiVat" parent="Theme.Material3.Dark.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        <item name="colorSecondary">@color/secondary</item>
        <item name="colorOnSecondary">@color/on_secondary</item>
        <item name="colorSurface">@color/surface</item>
        <item name="colorOnSurface">@color/on_surface</item>
        <item name="colorSurfaceVariant">@color/surface_variant</item>
        <item name="colorOnSurfaceVariant">@color/on_surface_variant</item>
        <item name="colorOutline">@color/outline</item>
        <item name="android:colorBackground">@color/background</item>
        <item name="colorOnBackground">@color/on_background</item>
        <item name="android:statusBarColor">@color/bg_app</item>
        <item name="android:navigationBarColor">@color/bg_card</item>
        <item name="android:windowLightStatusBar" tools:targetApi="23">false</item>
        <item name="android:windowLightNavigationBar" tools:targetApi="27">false</item>
    </style>
</resources>
```

- [ ] **Step 3: Update bools.xml (light bars → false for dark theme)**

```xml
<!-- app/src/main/res/values/bools.xml -->
<resources>
    <bool name="use_light_system_bars">false</bool>
</resources>
```

- [ ] **Step 4: Delete values-night/ folder contents (theme is now always dark)**

The `res/values-night/` folder overrides are no longer needed since the theme is permanently dark. Delete any files inside `app/src/main/res/values-night/` if they exist, or leave empty.

- [ ] **Step 5: Build and check theme compiles**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. Install on device/emulator to verify the app is dark.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/values/colors.xml \
        app/src/main/res/values/themes.xml \
        app/src/main/res/values/bools.xml
git commit -m "feat: apply dark theme with orange accent palette"
```

---

## Task 2: New Strings for All 3 Languages

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values-ru/strings.xml`

- [ ] **Step 1: Add new strings to values/strings.xml (Azerbaijani)**

Add these entries inside `<resources>` at the end of the file, before `</resources>`:

```xml
    <!-- Dual mode -->
    <string name="advanced_params_show">Əlavə parametrlər ▼</string>
    <string name="advanced_params_hide">Əlavə parametrləri gizlət ▲</string>
    <string name="save_to_history">Tarixçəyə əlavə et</string>

    <!-- Housing toggle labels -->
    <string name="housing_apartment">Mənzil</string>
    <string name="housing_house">Həyət evi</string>

    <!-- System toggle labels -->
    <string name="system_radiators">Radiatorlar</string>
    <string name="system_underfloor">İsti döşəmə</string>
    <string name="system_mixed">Qarışıq</string>

    <!-- Bottom navigation -->
    <string name="nav_calculator">Hesablama</string>
    <string name="nav_history">Tarixçə</string>

    <!-- Result card labels -->
    <string name="result_label_power">Lazımi güc</string>
    <string name="result_label_sections">Radiator seksiyaları</string>
    <string name="result_label_boiler">Tövsiyə olunan qazan</string>
    <string name="result_dash">—</string>
    <string name="result_unit_kw">kW</string>
    <string name="result_unit_sections">ədəd</string>
```

- [ ] **Step 2: Add new strings to values-en/strings.xml (English)**

```xml
    <!-- Dual mode -->
    <string name="advanced_params_show">Advanced parameters ▼</string>
    <string name="advanced_params_hide">Hide parameters ▲</string>
    <string name="save_to_history">Save to history</string>

    <!-- Housing toggle labels -->
    <string name="housing_apartment">Apartment</string>
    <string name="housing_house">House</string>

    <!-- System toggle labels -->
    <string name="system_radiators">Radiators</string>
    <string name="system_underfloor">Underfloor</string>
    <string name="system_mixed">Mixed</string>

    <!-- Bottom navigation -->
    <string name="nav_calculator">Calculator</string>
    <string name="nav_history">History</string>

    <!-- Result card labels -->
    <string name="result_label_power">Required power</string>
    <string name="result_label_sections">Radiator sections</string>
    <string name="result_label_boiler">Recommended boiler</string>
    <string name="result_dash">—</string>
    <string name="result_unit_kw">kW</string>
    <string name="result_unit_sections">pcs</string>
```

- [ ] **Step 3: Add new strings to values-ru/strings.xml (Russian)**

```xml
    <!-- Dual mode -->
    <string name="advanced_params_show">Расширенные параметры ▼</string>
    <string name="advanced_params_hide">Скрыть параметры ▲</string>
    <string name="save_to_history">Сохранить в историю</string>

    <!-- Housing toggle labels -->
    <string name="housing_apartment">Квартира</string>
    <string name="housing_house">Дом</string>

    <!-- System toggle labels -->
    <string name="system_radiators">Радиаторы</string>
    <string name="system_underfloor">Тёплый пол</string>
    <string name="system_mixed">Смешанный</string>

    <!-- Bottom navigation -->
    <string name="nav_calculator">Расчёт</string>
    <string name="nav_history">История</string>

    <!-- Result card labels -->
    <string name="result_label_power">Требуемая мощность</string>
    <string name="result_label_sections">Секций радиатора</string>
    <string name="result_label_boiler">Рекомендуемый котёл</string>
    <string name="result_dash">—</string>
    <string name="result_unit_kw">кВт</string>
    <string name="result_unit_sections">шт</string>
```

- [ ] **Step 4: Build to verify no missing string references**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values/strings.xml \
        app/src/main/res/values-en/strings.xml \
        app/src/main/res/values-ru/strings.xml
git commit -m "feat: add dual-mode and nav strings for all 3 languages"
```

---

## Task 3: HistoryManager

**Files:**
- Create: `app/src/main/java/com/example/istivat/HistoryManager.java`
- Create: `app/src/test/java/com/example/istivat/HistoryManagerTest.java`

- [ ] **Step 1: Write the failing tests first**

Create `app/src/test/java/com/example/istivat/HistoryManagerTest.java`:

```java
package com.example.istivat;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class HistoryManagerTest {

    @Test
    public void parseHistory_emptyString_returnsEmptyList() {
        List<String[]> result = HistoryManager.parseHistory("");
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseHistory_nullString_returnsEmptyList() {
        List<String[]> result = HistoryManager.parseHistory(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseHistory_singleEntry_returnsOneParsedEntry() {
        String raw = "2026-03-26|50.0|24";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(1, result.size());
        assertEquals("2026-03-26", result.get(0)[0]);
        assertEquals("50.0", result.get(0)[1]);
        assertEquals("24", result.get(0)[2]);
    }

    @Test
    public void parseHistory_twoEntries_returnsTwoEntries() {
        String raw = "2026-03-26|50.0|24\n2026-03-25|30.0|28";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(2, result.size());
    }

    @Test
    public void parseHistory_malformedEntry_skipsIt() {
        String raw = "bad_entry\n2026-03-26|50.0|24";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(1, result.size());
    }

    @Test
    public void serializeHistory_roundTrip() {
        String original = "2026-03-26|50.0|24\n2026-03-25|30.0|28";
        List<String[]> parsed = HistoryManager.parseHistory(original);
        String serialized = HistoryManager.serializeHistory(parsed);
        assertEquals(original, serialized);
    }

    @Test
    public void serializeHistory_emptyList_returnsEmptyString() {
        List<String[]> empty = HistoryManager.parseHistory("");
        assertEquals("", HistoryManager.serializeHistory(empty));
    }

    @Test
    public void deleteEntryAt_removesCorrectEntry() {
        String raw = "2026-03-26|50.0|24\n2026-03-25|30.0|28\n2026-03-24|20.0|32";
        List<String[]> entries = HistoryManager.parseHistory(raw);
        HistoryManager.deleteEntryAt(entries, 1);
        assertEquals(2, entries.size());
        assertEquals("50.0", entries.get(0)[1]);
        assertEquals("20.0", entries.get(1)[1]);
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew test --tests "com.example.istivat.HistoryManagerTest"
```
Expected: FAIL — `HistoryManager` class does not exist yet.

- [ ] **Step 3: Create HistoryManager.java**

Create `app/src/main/java/com/example/istivat/HistoryManager.java`:

```java
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
    public void addEntry(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date());
        String area = String.format(Locale.getDefault(), "%.1f", input.area);
        String boiler = String.valueOf(result.recommendedBoilerKw);
        String newEntry = date + DELIMITER + area + DELIMITER + boiler;

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

    /** Parse a raw newline-separated history string into a list of [date, area, boilerKw]. */
    public static List<String[]> parseHistory(String raw) {
        List<String[]> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return result;
        for (String line : raw.split("\n")) {
            String[] parts = line.split(Pattern.quote(DELIMITER));
            if (parts.length < 3) continue;
            result.add(new String[]{parts[0], parts[1], parts[2]});
        }
        return result;
    }

    /** Serialize a list of [date, area, boilerKw] back to the raw string format. */
    public static String serializeHistory(List<String[]> entries) {
        if (entries.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            String[] e = entries.get(i);
            sb.append(e[0]).append(DELIMITER).append(e[1]).append(DELIMITER).append(e[2]);
            if (i < entries.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /** Remove entry at index from the given list (mutates). */
    public static void deleteEntryAt(List<String[]> entries, int index) {
        entries.remove(index);
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew test --tests "com.example.istivat.HistoryManagerTest"
```
Expected: `BUILD SUCCESSFUL`, all 8 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/istivat/HistoryManager.java \
        app/src/test/java/com/example/istivat/HistoryManagerTest.java
git commit -m "feat: extract HistoryManager with unit tests"
```

---

## Task 4: PreferencesHelper

**Files:**
- Create: `app/src/main/java/com/example/istivat/PreferencesHelper.java`
- Create: `app/src/test/java/com/example/istivat/PreferencesHelperTest.java`

- [ ] **Step 1: Write the failing tests first**

Create `app/src/test/java/com/example/istivat/PreferencesHelperTest.java`:

```java
package com.example.istivat;

import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesHelperTest {

    @Test
    public void formState_defaultValues() {
        PreferencesHelper.FormState state = PreferencesHelper.FormState.defaults();
        assertEquals("", state.area);
        assertEquals("", state.height);
        assertEquals("180", state.radiatorPower);
        assertEquals("", state.floorArea);
        assertEquals(0, state.housingPosition);
        assertEquals(0, state.wallsPosition);
        assertEquals(0, state.insulationPosition);
        assertEquals(0, state.floorPosition);
        assertEquals(0, state.systemPosition);
        assertEquals(0, state.dhwPosition);
        assertFalse(state.advancedExpanded);
        assertNull(state.resultText);
    }

    @Test
    public void formState_constructor_storesAllFields() {
        PreferencesHelper.FormState state = new PreferencesHelper.FormState(
                "50", "2.7", "180", "10",
                1, 2, 1, 0, 2, 3,
                true, "Result text"
        );
        assertEquals("50", state.area);
        assertEquals("2.7", state.height);
        assertEquals("180", state.radiatorPower);
        assertEquals("10", state.floorArea);
        assertEquals(1, state.housingPosition);
        assertEquals(2, state.wallsPosition);
        assertEquals(1, state.insulationPosition);
        assertEquals(0, state.floorPosition);
        assertEquals(2, state.systemPosition);
        assertEquals(3, state.dhwPosition);
        assertTrue(state.advancedExpanded);
        assertEquals("Result text", state.resultText);
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew test --tests "com.example.istivat.PreferencesHelperTest"
```
Expected: FAIL — `PreferencesHelper` does not exist yet.

- [ ] **Step 3: Create PreferencesHelper.java**

Create `app/src/main/java/com/example/istivat/PreferencesHelper.java`:

```java
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
        public final int housingPosition;   // 0=apartment, 1=house
        public final int wallsPosition;     // 0-3 → 1-4 walls
        public final int insulationPosition;// 0=good, 1=medium, 2=poor
        public final int floorPosition;     // 0=standard, 1=attic
        public final int systemPosition;    // 0=radiators, 1=underfloor, 2=mixed
        public final int dhwPosition;       // 0-8 → 1-9 points
        public final boolean advancedExpanded;
        public final String resultText;     // null if no result yet

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
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew test --tests "com.example.istivat.PreferencesHelperTest"
```
Expected: `BUILD SUCCESSFUL`, all 2 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/istivat/PreferencesHelper.java \
        app/src/test/java/com/example/istivat/PreferencesHelperTest.java
git commit -m "feat: extract PreferencesHelper with FormState and unit tests"
```

---

## Task 5: Navigation Structure — bottom_nav_menu + activity_main.xml

**Files:**
- Create: `app/src/main/res/menu/bottom_nav_menu.xml`
- Modify: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Create bottom_nav_menu.xml**

Create `app/src/main/res/menu/bottom_nav_menu.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:id="@+id/nav_calculator"
        android:icon="@drawable/ic_calculate"
        android:title="@string/nav_calculator" />

    <item
        android:id="@+id/nav_history"
        android:icon="@drawable/ic_history"
        android:title="@string/nav_history" />

</menu>
```

Note: `ic_history` drawable must exist. If it doesn't, use `ic_calculate` temporarily or add a vector drawable — see Step 2.

- [ ] **Step 2: Add ic_history drawable if missing**

Check if `app/src/main/res/drawable/ic_history.xml` exists. If not, create it:

```xml
<!-- app/src/main/res/drawable/ic_history.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M13,3C8.03,3 4,7.03 4,12H1L4.89,15.89L4.96,16.03L9,12H6C6,8.13 9.13,5 13,5C16.87,5 20,8.13 20,12C20,15.87 16.87,19 13,19C11.07,19 9.32,18.21 8.06,16.94L6.64,18.36C8.27,19.99 10.51,21 13,21C17.97,21 22,16.97 22,12C22,7.03 17.97,3 13,3ZM12,8V13L16.28,15.54L17,14.33L13.5,12.25V8H12Z" />
</vector>
```

- [ ] **Step 3: Replace activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/bg_app">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/bg_card"
        app:title="@string/app_name"
        app:titleTextColor="@color/text_primary"
        app:subtitleTextColor="@color/text_secondary" />

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragmentContainer"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottomNav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/bg_card"
        app:menu="@menu/bottom_nav_menu"
        app:itemIconTint="@color/bottom_nav_selector"
        app:itemTextColor="@color/bottom_nav_selector" />

</LinearLayout>
```

- [ ] **Step 4: Create bottom_nav_selector color state list**

Create `app/src/main/res/color/bottom_nav_selector.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/accent_primary" android:state_checked="true" />
    <item android:color="@color/text_secondary" />
</selector>
```

- [ ] **Step 5: Build to check layout compiles**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/menu/bottom_nav_menu.xml \
        app/src/main/res/layout/activity_main.xml \
        app/src/main/res/drawable/ic_history.xml \
        app/src/main/res/color/bottom_nav_selector.xml
git commit -m "feat: add bottom navigation structure and new activity layout"
```

---

## Task 6: History Item Card + HistoryAdapter

**Files:**
- Create: `app/src/main/res/layout/item_history_card.xml`
- Create: `app/src/main/java/com/example/istivat/HistoryAdapter.java`

- [ ] **Step 1: Create item_history_card.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="6dp"
    app:cardBackgroundColor="@color/bg_card"
    app:cardCornerRadius="12dp"
    app:cardElevation="0dp"
    app:strokeColor="@color/divider"
    app:strokeWidth="1dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center_vertical">

        <!-- Flame icon -->
        <ImageView
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:src="@drawable/ic_calculate"
            android:tint="@color/accent_primary"
            android:contentDescription="@null" />

        <!-- Text info -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:layout_marginStart="12dp">

            <TextView
                android:id="@+id/textHistoryDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/text_secondary"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/textHistoryArea"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="2dp"
                android:textColor="@color/text_primary"
                android:textSize="15sp"
                android:textStyle="bold" />

        </LinearLayout>

        <!-- Boiler power badge -->
        <TextView
            android:id="@+id/textHistoryBoiler"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@drawable/badge_accent"
            android:paddingHorizontal="10dp"
            android:paddingVertical="4dp"
            android:textColor="@color/white"
            android:textSize="14sp"
            android:textStyle="bold" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 2: Create badge_accent drawable**

Create `app/src/main/res/drawable/badge_accent.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/accent_primary" />
    <corners android:radius="8dp" />
</shape>
```

- [ ] **Step 3: Create HistoryAdapter.java**

Create `app/src/main/java/com/example/istivat/HistoryAdapter.java`:

```java
package com.example.istivat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String[] entry);
    }

    private final List<String[]> entries;
    private final OnItemClickListener listener;

    public HistoryAdapter(List<String[]> entries, OnItemClickListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] entry = entries.get(position);
        holder.textDate.setText(entry[0]);
        holder.textArea.setText(entry[1] + " m²");
        holder.textBoiler.setText(entry[2] + " kW");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void removeAt(int position) {
        entries.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textArea;
        TextView textBoiler;

        ViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textHistoryDate);
            textArea = itemView.findViewById(R.id.textHistoryArea);
            textBoiler = itemView.findViewById(R.id.textHistoryBoiler);
        }
    }
}
```

- [ ] **Step 4: Build to check adapter compiles**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/item_history_card.xml \
        app/src/main/res/drawable/badge_accent.xml \
        app/src/main/java/com/example/istivat/HistoryAdapter.java
git commit -m "feat: add history item card layout and RecyclerView adapter"
```

---

## Task 7: History Screen — fragment_history.xml + HistoryFragment

**Files:**
- Create: `app/src/main/res/layout/fragment_history.xml`
- Create: `app/src/main/java/com/example/istivat/HistoryFragment.java`

- [ ] **Step 1: Create fragment_history.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/bg_app">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerHistory"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingTop="8dp"
        android:paddingBottom="8dp"
        android:clipToPadding="false" />

    <LinearLayout
        android:id="@+id/layoutEmpty"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:visibility="gone">

        <ImageView
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:src="@drawable/ic_history"
            android:tint="@color/text_secondary"
            android:contentDescription="@null" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/history_empty"
            android:textColor="@color/text_secondary"
            android:textSize="16sp" />

    </LinearLayout>

</FrameLayout>
```

- [ ] **Step 2: Create HistoryFragment.java**

Create `app/src/main/java/com/example/istivat/HistoryFragment.java`:

```java
package com.example.istivat;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryManager historyManager;
    private PreferencesHelper prefsHelper;
    private HistoryAdapter adapter;
    private List<String[]> entries;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PreferencesHelper.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        historyManager = new HistoryManager(prefs);
        prefsHelper = new PreferencesHelper(prefs);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        entries = historyManager.getEntries();
        adapter = new HistoryAdapter(entries, this::onEntryClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        attachSwipeToDelete();
        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh when switching back to this tab
        entries.clear();
        entries.addAll(historyManager.getEntries());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void onEntryClicked(String[] entry) {
        // Save the entry's area to prefs so CalculatorFragment can restore it,
        // then switch to calculator tab
        // We only restore the area value — the rest stays as-is
        requireActivity().getSharedPreferences(PreferencesHelper.PREFS_NAME,
                android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(PreferencesHelper.KEY_AREA, entry[1])
                .apply();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).switchToCalculator();
        }
    }

    private void attachSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                historyManager.deleteEntryAt(position);
                adapter.removeAt(position);
                updateEmptyState();
                Toast.makeText(requireContext(),
                        getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void updateEmptyState() {
        boolean isEmpty = entries.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
```

- [ ] **Step 3: Build to check Fragment compiles**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_history.xml \
        app/src/main/java/com/example/istivat/HistoryFragment.java
git commit -m "feat: add HistoryFragment with RecyclerView and swipe-to-delete"
```

---

## Task 8: Calculator Layout — fragment_calculator.xml

**Files:**
- Create: `app/src/main/res/layout/fragment_calculator.xml`

- [ ] **Step 1: Create fragment_calculator.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/bg_app">

    <!-- Scrollable form area -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Card: Room dimensions -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardBackgroundColor="@color/bg_card"
                app:cardCornerRadius="12dp"
                app:cardElevation="0dp"
                app:strokeColor="@color/divider"
                app:strokeWidth="1dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutArea"
                        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="@string/area"
                        app:startIconDrawable="@drawable/ic_area"
                        app:suffixText="@string/unit_square_meter">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editArea"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="numberDecimal"
                            tools:ignore="TouchTargetSizeCheck" />
                    </com.google.android.material.textfield.TextInputLayout>

                    <com.google.android.material.textfield.TextInputLayout
                        android:id="@+id/layoutHeight"
                        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="12dp"
                        android:hint="@string/height"
                        app:startIconDrawable="@drawable/ic_height"
                        app:suffixText="@string/unit_meter">

                        <com.google.android.material.textfield.TextInputEditText
                            android:id="@+id/editHeight"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:inputType="numberDecimal"
                            tools:ignore="TouchTargetSizeCheck" />
                    </com.google.android.material.textfield.TextInputLayout>

                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Card: Main parameters (housing + system toggles) -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                app:cardBackgroundColor="@color/bg_card"
                app:cardCornerRadius="12dp"
                app:cardElevation="0dp"
                app:strokeColor="@color/divider"
                app:strokeWidth="1dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <!-- Housing type label -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/housing_type"
                        android:textColor="@color/text_secondary"
                        android:textSize="12sp" />

                    <!-- Housing toggle: Apartment / House -->
                    <com.google.android.material.button.MaterialButtonToggleGroup
                        android:id="@+id/toggleHousing"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="6dp"
                        app:singleSelection="true"
                        app:selectionRequired="true">

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnApartment"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="@string/housing_apartment" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnHouse"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="@string/housing_house" />

                    </com.google.android.material.button.MaterialButtonToggleGroup>

                    <!-- System type label -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="16dp"
                        android:text="@string/system_type"
                        android:textColor="@color/text_secondary"
                        android:textSize="12sp" />

                    <!-- System toggle: Radiators / Underfloor / Mixed -->
                    <com.google.android.material.button.MaterialButtonToggleGroup
                        android:id="@+id/toggleSystem"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="6dp"
                        app:singleSelection="true"
                        app:selectionRequired="true">

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnRadiators"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="@string/system_radiators" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnUnderfloor"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="@string/system_underfloor" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/btnMixed"
                            style="@style/Widget.Material3.Button.OutlinedButton"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="@string/system_mixed" />

                    </com.google.android.material.button.MaterialButtonToggleGroup>

                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Advanced parameters toggle button -->
            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnAdvancedToggle"
                style="@style/Widget.Material3.Button.TextButton"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="@string/advanced_params_show"
                android:textColor="@color/accent_secondary" />

            <!-- Advanced section (hidden by default) -->
            <LinearLayout
                android:id="@+id/advancedSection"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:visibility="gone">

                <!-- Card: Construction -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    app:cardBackgroundColor="@color/bg_card"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="0dp"
                    app:strokeColor="@color/divider"
                    app:strokeWidth="1dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutWalls"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:hint="@string/walls"
                            app:endIconMode="dropdown_menu"
                            app:startIconDrawable="@drawable/ic_wall">

                            <com.google.android.material.textfield.MaterialAutoCompleteTextView
                                android:id="@+id/autoWalls"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="none"
                                android:focusable="false"
                                android:cursorVisible="false"
                                android:importantForAutofill="no"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutInsulation"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:hint="@string/insulation"
                            app:endIconMode="dropdown_menu"
                            app:startIconDrawable="@drawable/ic_insulation">

                            <com.google.android.material.textfield.MaterialAutoCompleteTextView
                                android:id="@+id/autoInsulation"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="none"
                                android:focusable="false"
                                android:cursorVisible="false"
                                android:importantForAutofill="no"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutFloor"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:hint="@string/floor_type"
                            app:endIconMode="dropdown_menu"
                            app:startIconDrawable="@drawable/ic_floor">

                            <com.google.android.material.textfield.MaterialAutoCompleteTextView
                                android:id="@+id/autoFloor"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="none"
                                android:focusable="false"
                                android:cursorVisible="false"
                                android:importantForAutofill="no"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <!-- Card: Radiator & DHW -->
                <com.google.android.material.card.MaterialCardView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    app:cardBackgroundColor="@color/bg_card"
                    app:cardCornerRadius="12dp"
                    app:cardElevation="0dp"
                    app:strokeColor="@color/divider"
                    app:strokeWidth="1dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutRadiatorPower"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:hint="@string/radiator_power"
                            app:startIconDrawable="@drawable/ic_radiator"
                            app:suffixText="@string/unit_watt">

                            <com.google.android.material.textfield.TextInputEditText
                                android:id="@+id/editRadiatorPower"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="numberDecimal"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutFloorArea"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:hint="@string/floor_area"
                            app:startIconDrawable="@drawable/ic_floor_heating"
                            app:suffixText="@string/unit_square_meter">

                            <com.google.android.material.textfield.TextInputEditText
                                android:id="@+id/editFloorArea"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="numberDecimal"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                        <com.google.android.material.textfield.TextInputLayout
                            android:id="@+id/layoutDhwPoints"
                            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:hint="@string/dhw_points"
                            app:endIconMode="dropdown_menu"
                            app:startIconDrawable="@drawable/ic_radiator">

                            <com.google.android.material.textfield.MaterialAutoCompleteTextView
                                android:id="@+id/autoDhwPoints"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:inputType="none"
                                android:focusable="false"
                                android:cursorVisible="false"
                                android:importantForAutofill="no"
                                tools:ignore="TouchTargetSizeCheck" />
                        </com.google.android.material.textfield.TextInputLayout>

                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

            </LinearLayout>
            <!-- end advancedSection -->

        </LinearLayout>
    </ScrollView>

    <!-- Result card — always visible at bottom -->
    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="8dp"
        app:cardBackgroundColor="@color/bg_card"
        app:cardCornerRadius="12dp"
        app:cardElevation="0dp"
        app:strokeColor="@color/accent_primary"
        app:strokeWidth="1dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp">

            <!-- Power -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_label_power"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

                <TextView
                    android:id="@+id/textResultPower"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_dash"
                    android:textColor="@color/accent_primary"
                    android:textSize="22sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_unit_kw"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

            </LinearLayout>

            <!-- Divider -->
            <View
                android:layout_width="1dp"
                android:layout_height="match_parent"
                android:background="@color/divider" />

            <!-- Sections -->
            <LinearLayout
                android:id="@+id/layoutResultSections"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_label_sections"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

                <TextView
                    android:id="@+id/textResultSections"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_dash"
                    android:textColor="@color/accent_secondary"
                    android:textSize="22sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_unit_sections"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

            </LinearLayout>

            <!-- Divider -->
            <View
                android:layout_width="1dp"
                android:layout_height="match_parent"
                android:background="@color/divider" />

            <!-- Boiler -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_label_boiler"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

                <TextView
                    android:id="@+id/textResultBoiler"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_dash"
                    android:textColor="@color/accent_primary"
                    android:textSize="22sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/result_unit_kw"
                    android:textColor="@color/text_secondary"
                    android:textSize="11sp" />

            </LinearLayout>

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Action buttons -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:paddingTop="8dp">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSaveHistory"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/save_to_history"
            android:enabled="false" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnReset"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="@string/reset" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnShare"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:enabled="false"
            app:icon="@drawable/ic_share"
            app:iconPadding="0dp" />

    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 2: Build to check layout inflates**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/fragment_calculator.xml
git commit -m "feat: add calculator fragment layout with cards and dual-mode toggle"
```

---

## Task 9: CalculatorFragment

**Files:**
- Create: `app/src/main/java/com/example/istivat/CalculatorFragment.java`

- [ ] **Step 1: Create CalculatorFragment.java**

Create `app/src/main/java/com/example/istivat/CalculatorFragment.java`:

```java
package com.example.istivat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class CalculatorFragment extends Fragment {

    private static final String TAG = "CalculatorFragment";
    private static final double MIN_AREA = 5.0, MAX_AREA = 1000.0;
    private static final double MIN_HEIGHT = 2.0, MAX_HEIGHT = 6.0;
    private static final double MIN_RADIATOR_POWER = 50.0, MAX_RADIATOR_POWER = 500.0;
    private static final double DEFAULT_RADIATOR_POWER = 180.0;
    private static final int MAX_DHW_POINTS = 9;
    private static final int PDF_PAGE_WIDTH = 595, PDF_PAGE_HEIGHT = 842;
    private static final int PDF_MARGIN_X = 40, PDF_START_Y = 60, PDF_LINE_HEIGHT = 22;
    private static final float PDF_TEXT_SIZE = 14f;

    // Views
    private TextInputLayout layoutArea, layoutHeight, layoutRadiatorPower, layoutFloorArea;
    private TextInputEditText editArea, editHeight, editRadiatorPower, editFloorArea;
    private MaterialAutoCompleteTextView autoWalls, autoInsulation, autoFloor, autoDhwPoints;
    private MaterialButtonToggleGroup toggleHousing, toggleSystem;
    private MaterialButton btnSaveHistory, btnReset, btnShare, btnAdvancedToggle;
    private LinearLayout advancedSection;
    private TextView textResultPower, textResultSections, textResultBoiler;
    private LinearLayout layoutResultSections;

    // State
    private int wallsPosition = 0, insulationPosition = 0, floorPosition = 0, dhwPosition = 0;
    private HeatingCalculator.Result lastResult;
    private HeatingCalculator.Input lastInput;
    private PreferencesHelper prefsHelper;
    private HistoryManager historyManager;
    private boolean isRestoring = false;
    private boolean advancedExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PreferencesHelper.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefsHelper = new PreferencesHelper(prefs);
        historyManager = new HistoryManager(prefs);

        bindViews(view);
        setupDropdowns();
        setupTextWatchers();
        setupToggles();
        setupButtons();
        restoreState();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    // ── View binding ────────────────────────────────────────────────────────

    private void bindViews(View view) {
        layoutArea = view.findViewById(R.id.layoutArea);
        layoutHeight = view.findViewById(R.id.layoutHeight);
        layoutRadiatorPower = view.findViewById(R.id.layoutRadiatorPower);
        layoutFloorArea = view.findViewById(R.id.layoutFloorArea);

        editArea = view.findViewById(R.id.editArea);
        editHeight = view.findViewById(R.id.editHeight);
        editRadiatorPower = view.findViewById(R.id.editRadiatorPower);
        editFloorArea = view.findViewById(R.id.editFloorArea);

        autoWalls = view.findViewById(R.id.autoWalls);
        autoInsulation = view.findViewById(R.id.autoInsulation);
        autoFloor = view.findViewById(R.id.autoFloor);
        autoDhwPoints = view.findViewById(R.id.autoDhwPoints);

        toggleHousing = view.findViewById(R.id.toggleHousing);
        toggleSystem = view.findViewById(R.id.toggleSystem);

        btnSaveHistory = view.findViewById(R.id.btnSaveHistory);
        btnReset = view.findViewById(R.id.btnReset);
        btnShare = view.findViewById(R.id.btnShare);
        btnAdvancedToggle = view.findViewById(R.id.btnAdvancedToggle);
        advancedSection = view.findViewById(R.id.advancedSection);

        textResultPower = view.findViewById(R.id.textResultPower);
        textResultSections = view.findViewById(R.id.textResultSections);
        textResultBoiler = view.findViewById(R.id.textResultBoiler);
        layoutResultSections = view.findViewById(R.id.layoutResultSections);
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private void setupDropdowns() {
        setupDropdown(autoWalls, R.array.walls_array, pos -> wallsPosition = pos);
        setupDropdown(autoInsulation, R.array.insulation_array, pos -> insulationPosition = pos);
        setupDropdown(autoFloor, R.array.floor_array, pos -> floorPosition = pos);
        setupDropdown(autoDhwPoints, R.array.dhw_points_array, pos -> dhwPosition = pos);
    }

    private void setupDropdown(MaterialAutoCompleteTextView view, int arrayResId,
                                PositionListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, getResources().getStringArray(arrayResId));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        view.setAdapter(adapter);
        view.setOnItemClickListener((parent, v, position, id) -> {
            listener.onPosition(position);
            recalculate();
        });
        listener.onPosition(0);
        view.setText(adapter.getItem(0), false);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isRestoring) recalculate();
            }
        };
        editArea.addTextChangedListener(watcher);
        editHeight.addTextChangedListener(watcher);
        editRadiatorPower.addTextChangedListener(watcher);
        editFloorArea.addTextChangedListener(watcher);
    }

    private void setupToggles() {
        toggleHousing.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !isRestoring) recalculate();
        });
        toggleSystem.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !isRestoring) {
                updateSystemMode();
                recalculate();
            }
        });
        // Select defaults
        toggleHousing.check(R.id.btnApartment);
        toggleSystem.check(R.id.btnRadiators);
    }

    private void setupButtons() {
        btnAdvancedToggle.setOnClickListener(v -> toggleAdvancedSection());
        btnSaveHistory.setOnClickListener(v -> saveToHistory());
        btnReset.setOnClickListener(v -> resetForm());
        btnShare.setOnClickListener(v -> shareReport());
    }

    // ── Advanced section ─────────────────────────────────────────────────────

    private void toggleAdvancedSection() {
        advancedExpanded = !advancedExpanded;
        TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new AutoTransition());
        advancedSection.setVisibility(advancedExpanded ? View.VISIBLE : View.GONE);
        btnAdvancedToggle.setText(advancedExpanded
                ? R.string.advanced_params_hide
                : R.string.advanced_params_show);
    }

    private void setAdvancedExpanded(boolean expanded) {
        advancedExpanded = expanded;
        advancedSection.setVisibility(expanded ? View.VISIBLE : View.GONE);
        btnAdvancedToggle.setText(expanded
                ? R.string.advanced_params_hide
                : R.string.advanced_params_show);
    }

    // ── System mode ──────────────────────────────────────────────────────────

    private void updateSystemMode() {
        int checkedId = toggleSystem.getCheckedButtonId();
        boolean underfloorOnly = checkedId == R.id.btnUnderfloor;
        boolean radiatorsOnly = checkedId == R.id.btnRadiators;

        layoutRadiatorPower.setVisibility(underfloorOnly ? View.GONE : View.VISIBLE);
        layoutFloorArea.setVisibility(radiatorsOnly ? View.GONE : View.VISIBLE);
        layoutResultSections.setVisibility(underfloorOnly ? View.GONE : View.VISIBLE);

        if (underfloorOnly) layoutRadiatorPower.setError(null);
    }

    // ── Calculation ──────────────────────────────────────────────────────────

    private void recalculate() {
        if (!validateForm()) {
            showDashResults();
            return;
        }
        HeatingCalculator.Input input = buildInput();
        if (input == null) {
            showDashResults();
            return;
        }
        lastInput = input;
        lastResult = HeatingCalculator.calculate(input);
        showResults(lastResult, input);
        btnSaveHistory.setEnabled(true);
        btnShare.setEnabled(true);
    }

    private void showResults(HeatingCalculator.Result result, HeatingCalculator.Input input) {
        textResultPower.setText(String.format(Locale.getDefault(), "%.1f", result.requiredPowerKw));
        textResultSections.setText(String.valueOf(result.radiatorSections));
        textResultBoiler.setText(String.valueOf(result.recommendedBoilerKw));
        // Persist result text for restore
        prefsHelper.saveResultText(buildResultText(input, result));
    }

    private void showDashResults() {
        String dash = getString(R.string.result_dash);
        textResultPower.setText(dash);
        textResultSections.setText(dash);
        textResultBoiler.setText(dash);
        lastResult = null;
        lastInput = null;
        btnSaveHistory.setEnabled(false);
        btnShare.setEnabled(false);
        prefsHelper.saveResultText(null);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private boolean validateForm() {
        boolean valid = true;
        boolean underfloorOnly = toggleSystem.getCheckedButtonId() == R.id.btnUnderfloor;
        boolean radiatorsOnly = toggleSystem.getCheckedButtonId() == R.id.btnRadiators;

        Double area = parseDouble(editArea);
        if (area == null || area < MIN_AREA || area > MAX_AREA) {
            if (!isRestoring && area != null) layoutArea.setError(getString(R.string.error_invalid_area));
            valid = false;
        } else {
            layoutArea.setError(null);
        }

        Double height = parseDouble(editHeight);
        if (height == null || height < MIN_HEIGHT || height > MAX_HEIGHT) {
            if (!isRestoring && height != null) layoutHeight.setError(getString(R.string.error_invalid_height));
            valid = false;
        } else {
            layoutHeight.setError(null);
        }

        if (!underfloorOnly) {
            Double rp = parseDouble(editRadiatorPower);
            if (rp == null || rp < MIN_RADIATOR_POWER || rp > MAX_RADIATOR_POWER) {
                if (!isRestoring && rp != null) layoutRadiatorPower.setError(getString(R.string.error_invalid_radiator));
                valid = false;
            } else {
                layoutRadiatorPower.setError(null);
            }
        }

        if (!radiatorsOnly) {
            Double floorArea = parseDouble(editFloorArea);
            double maxFloor = area != null ? area : MAX_AREA;
            if (floorArea == null || floorArea < 0 || (area != null && floorArea > area)) {
                if (!isRestoring && floorArea != null)
                    layoutFloorArea.setError(getString(R.string.error_invalid_floor_area, maxFloor));
                valid = false;
            } else {
                layoutFloorArea.setError(null);
            }
        }

        return valid;
    }

    // ── Input building ───────────────────────────────────────────────────────

    private HeatingCalculator.Input buildInput() {
        Double area = parseDouble(editArea);
        Double height = parseDouble(editHeight);
        if (area == null || height == null) return null;

        int checkedSystemId = toggleSystem.getCheckedButtonId();
        boolean underfloorOnly = checkedSystemId == R.id.btnUnderfloor;
        boolean radiatorsOnly = checkedSystemId == R.id.btnRadiators;

        double radiatorPower = DEFAULT_RADIATOR_POWER;
        if (!underfloorOnly) {
            Double rp = parseDouble(editRadiatorPower);
            if (rp == null) return null;
            radiatorPower = rp;
        }

        double floorArea = 0.0;
        if (!radiatorsOnly) {
            Double fa = parseDouble(editFloorArea);
            if (fa == null) return null;
            floorArea = fa;
        }

        boolean isHouse = toggleHousing.getCheckedButtonId() == R.id.btnHouse;
        int wallCount = wallsPosition + 1; // positions 0-3 → walls 1-4
        int dhwPoints = Math.min(Math.max(dhwPosition, 0), MAX_DHW_POINTS - 1) + 1;

        return new HeatingCalculator.Input(
                area, height, wallCount, radiatorPower, floorArea,
                isHouse ? HeatingCalculator.HousingType.HOUSE : HeatingCalculator.HousingType.APARTMENT,
                mapInsulation(insulationPosition),
                floorPosition == 1 ? HeatingCalculator.FloorType.ATTIC : HeatingCalculator.FloorType.STANDARD,
                underfloorOnly ? HeatingCalculator.SystemType.UNDERFLOOR_ONLY : HeatingCalculator.SystemType.MIXED,
                dhwPoints
        );
    }

    private HeatingCalculator.InsulationLevel mapInsulation(int position) {
        if (position == 0) return HeatingCalculator.InsulationLevel.GOOD;
        if (position == 2) return HeatingCalculator.InsulationLevel.POOR;
        return HeatingCalculator.InsulationLevel.MEDIUM;
    }

    // ── History ──────────────────────────────────────────────────────────────

    private void saveToHistory() {
        if (lastInput == null || lastResult == null) return;
        historyManager.addEntry(lastInput, lastResult);
        Toast.makeText(requireContext(), getString(R.string.history_cleared).replace(
                getString(R.string.history_cleared), "✓ Saved"), Toast.LENGTH_SHORT).show();
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    private void resetForm() {
        isRestoring = true;
        editArea.setText("");
        editHeight.setText(String.valueOf((int) DEFAULT_RADIATOR_POWER));
        editRadiatorPower.setText(String.valueOf((int) DEFAULT_RADIATOR_POWER));
        editFloorArea.setText("");
        toggleHousing.check(R.id.btnApartment);
        toggleSystem.check(R.id.btnRadiators);
        setDropdownPosition(autoWalls, R.array.walls_array, 0, pos -> wallsPosition = pos);
        setDropdownPosition(autoInsulation, R.array.insulation_array, 0, pos -> insulationPosition = pos);
        setDropdownPosition(autoFloor, R.array.floor_array, 0, pos -> floorPosition = pos);
        setDropdownPosition(autoDhwPoints, R.array.dhw_points_array, 0, pos -> dhwPosition = pos);
        layoutArea.setError(null);
        layoutHeight.setError(null);
        layoutRadiatorPower.setError(null);
        layoutFloorArea.setError(null);
        showDashResults();
        updateSystemMode();
        saveState();
        isRestoring = false;
    }

    // ── State save/restore ───────────────────────────────────────────────────

    private void saveState() {
        int housingPos = toggleHousing.getCheckedButtonId() == R.id.btnHouse ? 1 : 0;
        int systemPos = systemToggleToPosition();
        prefsHelper.save(new PreferencesHelper.FormState(
                valueOrEmpty(editArea), valueOrEmpty(editHeight),
                valueOrEmpty(editRadiatorPower), valueOrEmpty(editFloorArea),
                housingPos, wallsPosition, insulationPosition, floorPosition,
                systemPos, dhwPosition, advancedExpanded, null
        ));
    }

    private void restoreState() {
        isRestoring = true;
        PreferencesHelper.FormState state = prefsHelper.load();

        editArea.setText(state.area);
        editHeight.setText(state.height);
        editRadiatorPower.setText(state.radiatorPower);
        editFloorArea.setText(state.floorArea);

        toggleHousing.check(state.housingPosition == 1 ? R.id.btnHouse : R.id.btnApartment);
        toggleSystem.check(systemPositionToButtonId(state.systemPosition));

        setDropdownPosition(autoWalls, R.array.walls_array, state.wallsPosition, pos -> wallsPosition = pos);
        setDropdownPosition(autoInsulation, R.array.insulation_array, state.insulationPosition, pos -> insulationPosition = pos);
        setDropdownPosition(autoFloor, R.array.floor_array, state.floorPosition, pos -> floorPosition = pos);
        setDropdownPosition(autoDhwPoints, R.array.dhw_points_array, state.dhwPosition, pos -> dhwPosition = pos);

        setAdvancedExpanded(state.advancedExpanded);
        updateSystemMode();
        isRestoring = false;
        recalculate();
    }

    private int systemToggleToPosition() {
        int id = toggleSystem.getCheckedButtonId();
        if (id == R.id.btnUnderfloor) return 1;
        if (id == R.id.btnMixed) return 2;
        return 0; // btnRadiators
    }

    private int systemPositionToButtonId(int pos) {
        if (pos == 1) return R.id.btnUnderfloor;
        if (pos == 2) return R.id.btnMixed;
        return R.id.btnRadiators;
    }

    // ── Share / PDF ──────────────────────────────────────────────────────────

    private void shareReport() {
        if (lastInput == null || lastResult == null) return;
        try {
            File pdfFile = createPdfReport(buildReportText(lastInput, lastResult));
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_share_message));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.report_share_title)));
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "Failed to share PDF", e);
            Toast.makeText(requireContext(), getString(R.string.report_share_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildResultText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder sb = new StringBuilder(getString(R.string.result_power, result.requiredPowerKw));
        if (!underfloorOnly) sb.append("\n").append(getString(R.string.result_sections, result.radiatorSections));
        sb.append("\n").append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return sb.toString();
    }

    private String buildReportText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.report_title)).append("\n\n");
        sb.append(getString(R.string.area)).append(": ").append(String.format(Locale.getDefault(), "%.1f m²\n", input.area));
        sb.append(getString(R.string.height)).append(": ").append(String.format(Locale.getDefault(), "%.1f m\n", input.height));
        if (!underfloorOnly) sb.append(getString(R.string.radiator_power)).append(": ").append(String.format(Locale.getDefault(), "%.0f W\n", input.radiatorPower));
        sb.append(getString(R.string.floor_area)).append(": ").append(String.format(Locale.getDefault(), "%.1f m²\n\n", input.floorArea));
        sb.append(getString(R.string.result_power, result.requiredPowerKw)).append("\n");
        if (!underfloorOnly) sb.append(getString(R.string.result_sections, result.radiatorSections)).append("\n");
        sb.append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return sb.toString();
    }

    private File createPdfReport(String text) throws IOException {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(PDF_TEXT_SIZE);
        paint.setAntiAlias(true);
        int y = PDF_START_Y;
        for (String line : text.split("\n")) {
            canvas.drawText(line, PDF_MARGIN_X, y, paint);
            y += PDF_LINE_HEIGHT;
        }
        doc.finishPage(page);
        File file = new File(requireContext().getCacheDir(), "heating_report.pdf");
        try (FileOutputStream out = new FileOutputStream(file)) {
            doc.writeTo(out);
        } finally {
            doc.close();
        }
        return file;
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private void setDropdownPosition(MaterialAutoCompleteTextView view, int arrayResId,
                                     int position, PositionListener listener) {
        String[] entries = getResources().getStringArray(arrayResId);
        int safe = Math.min(Math.max(position, 0), entries.length - 1);
        listener.onPosition(safe);
        view.setText(entries[safe], false);
    }

    private Double parseDouble(TextInputEditText editText) {
        String val = valueOrEmpty(editText);
        if (val.isEmpty()) return null;
        try { return Double.parseDouble(val.replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }

    private String valueOrEmpty(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private interface PositionListener {
        void onPosition(int position);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
```

- [ ] **Step 2: Build to check Fragment compiles**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/istivat/CalculatorFragment.java
git commit -m "feat: add CalculatorFragment with live results and dual-mode"
```

---

## Task 10: Update MainActivity — Host Fragments

**Files:**
- Modify: `app/src/main/java/com/example/istivat/MainActivity.java`

- [ ] **Step 1: Replace MainActivity.java**

Replace the entire file with this slimmed-down host:

```java
package com.example.istivat;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PreferencesHelper prefsHelper;
    private CalculatorFragment calculatorFragment;
    private HistoryFragment historyFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PreferencesHelper.PREFS_NAME, MODE_PRIVATE);
        prefsHelper = new PreferencesHelper(prefs);
        applySavedLocale();
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottomNav);
        calculatorFragment = new CalculatorFragment();
        historyFragment = new HistoryFragment();

        if (savedInstanceState == null) {
            showFragment(calculatorFragment);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_calculator) {
                showFragment(calculatorFragment);
            } else if (item.getItemId() == R.id.nav_history) {
                showFragment(historyFragment);
            }
            return true;
        });
    }

    public void switchToCalculator() {
        bottomNav.setSelectedItemId(R.id.nav_calculator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_change_language) { showLanguageDialog(); return true; }
        if (id == R.id.menu_about) { showAboutDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void applySavedLocale() {
        String language = prefsHelper.loadLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void showLanguageDialog() {
        String[] languages = getResources().getStringArray(R.array.language_options_array);
        String current = prefsHelper.loadLanguage();
        int selectedIndex = Arrays.asList(PreferencesHelper.LANGUAGE_CODES).indexOf(current);
        if (selectedIndex < 0) selectedIndex = 0;
        final int[] selected = {selectedIndex};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String code = PreferencesHelper.LANGUAGE_CODES[which];
                    if (!code.equals(current)) {
                        prefsHelper.saveLanguage(code);
                        recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_about)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
```

Note: The `menu_history` item in `main_menu.xml` is no longer needed (history is now a tab). You can remove it from `app/src/main/res/menu/main_menu.xml` to keep the menu clean.

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run unit tests to verify nothing broke**

```bash
./gradlew test
```
Expected: All tests pass.

- [ ] **Step 4: Install on device/emulator and verify manually**

```bash
./gradlew installDebug
```

Verify:
- App launches with dark background
- Calculator tab shows 2 cards + toggle buttons + result bar
- "Advanced parameters" button expands/collapses
- Result updates live as you type area and height
- History tab is empty initially
- Entering values and tapping "Save to history" → switch to History tab → entry appears
- Swipe left on history entry → entry removed
- Language change still works from menu

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/istivat/MainActivity.java
git commit -m "feat: slim down MainActivity to Fragment host, wire bottom navigation"
```

---

## Self-Review Notes

**Spec coverage check:**
- ✅ Dark theme `#0F0F14` / `#1A1A24` / `#FF6B35` — Task 1
- ✅ Cards with rounded corners — Task 8 layout
- ✅ Simple mode (4 fields visible) + Advanced expand — Tasks 8 & 9
- ✅ Segment buttons for housing / system type — Tasks 8 & 9
- ✅ Live results without Calculate button — Task 9 `recalculate()`
- ✅ Bottom navigation 2 tabs — Tasks 5 & 10
- ✅ History as Fragment with RecyclerView — Tasks 6 & 7
- ✅ Swipe-to-delete history — Task 7
- ✅ Tap history entry → restore area → switch to calculator — Task 7
- ✅ PDF share preserved — Task 9
- ✅ 3 languages preserved — Task 2
- ✅ `advancedExpanded` state saved in SharedPreferences — Task 4 & 9

**Type consistency:**
- `HistoryManager.deleteEntryAt(int)` instance method and `deleteEntryAt(List, int)` static — both used correctly in Tasks 3 & 7
- `PreferencesHelper.FormState` constructor arg order is consistent between Tasks 4 & 9
- `wallsPosition` in CalculatorFragment is 0-based index; `wallCount = wallsPosition + 1` passed to HeatingCalculator — matches original logic
