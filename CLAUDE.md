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

Single-module Android app with two source files:

```
app/src/main/java/com/example/istivat/
  MainActivity.java       — All UI, input validation, state management, PDF generation
  HeatingCalculator.java  — Pure calculation logic (no Android dependencies)

app/src/main/res/
  values/         — Azerbaijani strings (default language)
  values-en/      — English strings
  values-ru/      — Russian strings
  values-night/   — Dark mode theme overrides
  layout/         — activity_main.xml, spinner_item.xml, spinner_dropdown_item.xml
  xml/            — file_paths.xml (FileProvider), backup rules, data extraction rules
```

## Key Config

- **Package**: `com.example.istivat`
- **Min SDK**: 24 | **Target/Compile SDK**: 36
- **AGP**: 8.12.1
- **Language**: Java (not Kotlin)
- **Build scripts**: Kotlin DSL (`.kts`)
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
History stored as newline-separated entries with `|` delimiter: `date|area|boilerKw`.

## Gotchas

- `systemPosition == 1` = UNDERFLOOR_ONLY (radiator power field disabled in this mode)
- Wall count is parsed as `Integer` from the dropdown string value (not from position)
- DHW dropdown: position 0 = 1 point, position N = N+1 points
- Floor area must be ≤ room area; validation enforces this dynamically
- Language change calls `recreate()` — triggers full Activity restart
- PDF is generated via Android's built-in `PdfDocument` API, saved to `getCacheDir()`
- FileProvider authority: `com.example.istivat.provider`
- `isRestoring` flag prevents clearing results during state restore in `onCreate`

## Languages

Default: Azerbaijani (`az`). Also: Russian (`ru`), English (`en`).
Language codes array: `{"az", "ru", "en"}` — index matches `LANGUAGE_CODES`.

## Material Design

Uses Material Design 3 components throughout:
`MaterialToolbar`, `MaterialButton`, `MaterialAutoCompleteTextView`, `TextInputLayout`, `MaterialAlertDialogBuilder`.
Theme: `Theme.IstiVat` (defined in `res/values/themes.xml`).
