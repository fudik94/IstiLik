#  🔥 IstiLik  Heating Calculator

Android app for calculating the required heating power for rooms, apartments, and houses — designed for the Azerbaijani climate.

---

## 🧮 Features

**Calculator**
- Room area, ceiling height, housing type (apartment / house)
- Number of exterior walls, insulation level, floor type (standard / attic)
- Heating system type: combined (radiators + underfloor) or underfloor only
- Domestic hot water (DHW) points
- Live results: heat loss (W), radiator sections, boiler power recommendation

**History**
- Save calculations with a custom name
- Browse past results, reload any entry into the calculator

**PDF Report**
- Generate a professional PDF summary of any calculation
- Share directly via WhatsApp, email, or any app

**Multilingual**
- Azerbaijani, Russian, English, Estonian
- Language switcher in the app menu — no system settings needed

**Other**
- Permanent dark theme (Material Design 3)
- All inputs auto-saved — picks up where you left off
- Input validation (floor area ≤ room area, required fields, etc.)

---

## Screenshots

<img src="https://github.com/user-attachments/assets/96e39cf5-5fb2-4507-9f0e-e97efe079263" width="300" alt="App Screenshot">


## Tech Stack

- **Language:** Java
- **Min SDK:** 24 (Android 7.0) | **Target SDK:** 36
- **UI:** Material Design 3, Fragment-based navigation
- **PDF:** Android `PdfDocument` + `Canvas`
- **Storage:** `SharedPreferences`
- **Build:** Gradle with Kotlin DSL

---

## Contact

**istilik.info@gmail.com**
Developed by Fuad — based in Estonia
