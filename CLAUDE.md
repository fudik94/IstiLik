# IstiLik — Heating Calculator Android App

Android app that calculates required heating power for rooms/apartments, designed for the Azerbaijani climate.

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

Single-module Android app — Fragment-based UI, Activity is a thin host:

```
app/src/main/java/com/example/istivat/
  MainActivity.java         — Fragment host, toolbar/insets, language dialog, about dialog
  CalculatorFragment.java   — All calculator UI, input validation, PDF generation
  HistoryFragment.java      — History list UI, load-into-calculator navigation
  HistoryManager.java       — Read/write history entries to SharedPreferences
  HistoryAdapter.java       — RecyclerView adapter for history list
  HeatingCalculator.java    — Pure calculation logic (no Android dependencies)
  PreferencesHelper.java    — SharedPreferences keys and language codes

app/src/main/res/
  values/         — Azerbaijani strings (default language)
  values-en/      — English strings
  values-ru/      — Russian strings
  values-et/      — Estonian strings
  layout/         — activity_main.xml, fragment_calculator.xml, fragment_history.xml,
                    spinner_item.xml, spinner_dropdown_item.xml
  menu/           — main_menu.xml, bottom_nav_menu.xml
  xml/            — file_paths.xml (FileProvider), backup rules, data extraction rules
```

**DO NOT create a `values-night/` folder** — it overrides the permanent dark theme on dark-mode devices, breaking the UI.

## Key Config

- **Package**: `com.example.istivat`
- **Min SDK**: 24 | **Target/Compile SDK**: 36
- **AGP**: 8.12.1
- **Language**: Java (not Kotlin)
- **Build scripts**: Kotlin DSL (`.kts`)
- **Theme**: `Theme.Material3.Dark.NoActionBar` (permanent dark — not system-adaptive)
- **Dependencies**: AppCompat, Material Design 3 (1.12.0), ConstraintLayout, Activity

## Calculation Logic (HeatingCalculator.java)

Formula: `heatLoss = area × baseLoad × heightFactor × insulationFactor × wallFactor × floorFactor`

- Base load: 100 W/m² (apartment), 120 W/m² (house)
- Height reference: 2.7 m
- Insulation factors: Good=0.85, Medium=1.0, Poor=1.2
- Attic floor adds ×1.1
- Each additional wall beyond 1 adds +10%
- Underfloor heating offsets: 75 W/m² of floor area
- DHW points: +1 kW each
- Boiler sizes (kW tiers): 24 / 28 / 32 / 40 / 50

## State Management

All input state saved to `SharedPreferences` ("heating_prefs") on `onPause()`.
History stored as newline-separated entries with `|` delimiter: `name|date|area|boilerKw` (4 fields).
Old 3-field format `date|area|boilerKw` is still parsed for backward compatibility (name defaults to "").

## Languages

Default: Azerbaijani (`az`). Also: Russian (`ru`), English (`en`), Estonian (`et`).
`PreferencesHelper.LANGUAGE_CODES = {"az", "ru", "en", "et"}` — index matches spinner position.

**`language_options_array` must be identical in ALL locale folders** — each language name shown in its own language: `Azərbaycan dili / Русский / English / Eesti`. If arrays differ per locale, language switcher shows translated names instead of self-names.

## Locale System

Locale is applied in `MainActivity.attachBaseContext()` via `base.createConfigurationContext(config)`.
Fragments do **not** automatically inherit this locale from `getResources()` — must build a `localizedContext` explicitly:
```java
Configuration config = new Configuration(requireContext().getResources().getConfiguration());
config.setLocale(locale);
localizedContext = requireContext().createConfigurationContext(config);
```
Use `localizedContext.getResources()` for string arrays; use `requireContext()` for adapter/view constructors (theming).

## Dropdown (MaterialAutoCompleteTextView) Gotchas

- **Filtering bug**: `MaterialAutoCompleteTextView` filters dropdown items by the current text value. If the field shows "Yaxşı", only "Yaxşı" appears in the list. Fix: override `getFilter()` in the `ArrayAdapter` to always return all items unchanged.
- **View state restoration**: Android's `onViewStateRestored()` runs AFTER `onViewCreated()` and overwrites manually-set dropdown text with the previously-saved state (old language). Fix: call `view.setSaveEnabled(false)` on each `MaterialAutoCompleteTextView` before setting text.
- **Theming**: pass `requireContext()` (not `localizedContext`) as the ArrayAdapter constructor context — otherwise dropdown text becomes black (loses dark theme).

## Gotchas

- `systemPosition == 1` = UNDERFLOOR_ONLY (radiator power field disabled in this mode)
- Wall count is parsed as `Integer` from the dropdown string value (not from position)
- DHW dropdown: position 0 = 1 point, position N = N+1 points
- Floor area must be ≤ room area; validation enforces this dynamically
- Language change calls `recreate()` — triggers full Activity restart
- PDF is generated via Android's built-in `PdfDocument` API with Canvas drawing; saved to `getCacheDir()`
- FileProvider authority: `com.example.istivat.provider`
- Toolbar height: use `wrap_content` + `minHeight="?attr/actionBarSize"` — fixed height clips content after insets padding is added
- Status bar inset applied as top padding to toolbar via `ViewCompat.setOnApplyWindowInsetsListener`
- `MainActivity.switchToCalculator()` — call from HistoryFragment to navigate back after loading an entry

## Material Design

Uses Material Design 3 components throughout:
`MaterialToolbar`, `MaterialButton`, `MaterialAutoCompleteTextView`, `TextInputLayout`, `MaterialAlertDialogBuilder`.
Theme: `Theme.IstiVat` extends `Theme.Material3.Dark.NoActionBar` (defined in `res/values/themes.xml`).
