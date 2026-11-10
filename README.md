# Herz

A Kotlin / Jetpack Compose wellness journal for Android 7.0+ (API 24).
Tracks **self-reported mood and energy**, with an optional daily reflection.
It does not measure heart rate or use health sensors.

## Run

Open this folder in Android Studio, let Gradle sync, and run `app` on an emulator or device.
Use Android Studio's bundled JDK. From PowerShell with `JAVA_HOME` set to that JDK:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

## Flow

Welcome → animated day/night theme selection → daily check-in → dashboard.
After the first saved check-in, subsequent launches open the dashboard.
The dashboard theme button changes and remembers the theme at any time.
Home and Calendar provide access to trends and monthly history.

- Choose one of five moods, energy from 1–5, and an optional reflection (up to 2,000 characters).
- Saving again on the same local date updates that day's entry.
- Seven-day statistics include only recorded days. Missing days have dots instead of mood bars.
- Calendar weeks begin on Monday; browse past months, tap dates, or return to Today.
- Future dates are disabled. Previous entries can be viewed; today's entry can be edited.
- Form and navigation state survive activity recreation; layouts scroll for small screens and keyboards.

## Storage and structure

Uses Android's built-in `SQLiteOpenHelper` for durable check-ins and `SharedPreferences`
for theme and first-run state. Database reads and writes run on `Dispatchers.IO`.
Journal and preference files are excluded from Android backup and device transfer.
These platform APIs keep this prototype dependency-light; Room and DataStore can replace
the storage layer later. No seeded entries, accounts, network requests, or health permissions.

`HerzApp.kt` coordinates screens and saveable navigation state. `HerzViewModel.kt` owns
loaded entries and in-flight database operations across activity recreation. Each of the five screens has its own
Kotlin file. `HerzComponents.kt` contains original Canvas heart, mood, and sky artwork
and shared UI elements. Theme colors live under `ui/theme`.

Tests cover date boundaries, leap years, Monday-first calendar alignment, weekly filtering,
database reopening and same-day updates, and onboarding → theme → save → edit → calendar.
The UI tests reset the app's journal/settings: run them on a dedicated test emulator.

Landscape and foldable changes, the per-screen audit, and tested configurations are
documented in [the adaptive layout audit](docs/ADAPTIVE_LAYOUT_AUDIT.md).
