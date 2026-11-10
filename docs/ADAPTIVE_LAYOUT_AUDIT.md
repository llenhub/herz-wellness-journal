# Adaptive layout audit

The existing cream/coral and night palettes, artwork, animations, five-screen flow,
SQLite journal, and settings storage are preserved.

## Findings and focused changes

| Area | Finding | Change |
| --- | --- | --- |
| Welcome | Weighted scrolling body plus a non-scrolling footer could leave almost no content height in landscape or split windows. Artwork assumed enough horizontal space. | Shared `OnboardingLayout` uses one full-screen scroll owner below 480 dp available height or above 1.3 font scale. Taller windows keep the existing pinned footer. Heart size is bounded by available width. |
| Theme selection | Same footer constraint; the fixed-height sky could draw clouds outside a narrow viewport. | Uses the shared short-window layout. Sky height adapts to width and drawing is clipped to its existing rounded shape. Sun/moon, cloud, and palette animations remain. |
| Check-in | Already uses a single bounded vertical scroller with keyboard insets. Form used `rememberSaveable`, but database work belonged to a disposable composition scope. | Keep the form layout and saveable values. Move database work into an activity ViewModel, so resizing/recreation does not cancel loading or saving. |
| Dashboard | Already scrollable; the chart title and fixed chip competed for width. Weekday formatting could produce longer labels on different runtimes. | Weight the title, reserve chip spacing, and explicitly use single-letter labels. Chart height is an intentional local graphic size inside the scroller, not a screen-height assumption. |
| Calendar | Already uses one vertical scroller and seven weighted columns. Selection and month were saveable only while the screen remained composed. | Keep the grid. A root `SaveableStateHolder` retains month, date, scroll positions, and form drafts when switching screens as well as during recreation. |
| Shared controls | Primary buttons had an exact 58 dp height, restricting wrapping. | Use a 58 dp minimum and constrain label width so text can wrap beside the arrow. |
| Window | Existing centered 560 dp content limit was suitable for wide windows. Landscape cutouts needed explicit horizontal protection. | Retain the centered max-width design on every screen and add horizontal display-cutout insets. No nested outer scrolling is added around screen scrollers. |
| Manifest | No `screenOrientation`, `configChanges`, or resize restriction was present. | Preserve normal Android activity recreation and `adjustResize`; remove unused XML namespace and redundant activity label. No orientation lock or recreation bypass is added. |

`rememberSaveable` retains route, theme, setup progress, save acknowledgement,
form values, calendar selection and month. `SaveableStateHolder` retains per-screen
state across navigation. The ViewModel retains loaded data, save progress, and pending
completion across configuration changes. SQLite and preferences retain saved data
and theme across closing/reopening the app.

## Reproduction

With a dedicated Android emulator running:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

`AdaptiveLayoutTest` changes window size at every screen, verifies reachable actions,
recreates the activity, checks the same ViewModel instance, and asserts that mood,
energy, draft reflection, night theme, route, calendar month/date, and saved entries
survive. Display-size overrides are restored in `finally`.

For host-controlled emulator posture commands, build/install the application
and test APKs and run the instrumentation runner with `-e postureHandshake true`.
The test creates `cache/posture-ready`; after `adb emu fold`, signal it with
`adb shell run-as com.example.herz touch cache/posture-folded`. When
`cache/posture-unfold-ready` appears, run `adb emu unfold` and create
`cache/posture-unfolded` the same way. Assertions run at each checkpoint while the
check-in is still unsaved. These markers exist only during the test and are removed
in `finally`.

## Actual verification — 2026-11-10

Build: `assembleDebug` and `assembleDebugAndroidTest` passed. Four JVM unit tests
passed. Lint passed with no errors; inherited dependency/custom-lint warnings remain.
The final installed APKs passed all three instrumentation tests (130.222 seconds),
including the optional live posture handshake. Output is in
[adaptive-instrumentation.txt](adaptive-instrumentation.txt).

Device: existing **Pixel_Fold_API_35 AVD**, Android 15 / API 35, density 420.
All five screens were exercised at each of these measured window configurations:

| Window override (pixels) | Reported configuration (dp) | Coverage |
| --- | --- | --- |
| 1080 × 1920 | 411 × 731 | Phone portrait |
| 1920 × 1080 | 731 × 411 | Phone landscape |
| 1080 × 2092 | 411 × 797 | Foldable cover-display dimensions |
| 2208 × 1840 | 841 × 701 | Unfolded inner-display dimensions |
| 1840 × 2208 | 701 × 841 | Rotated inner-display dimensions |
| 840 × 1600 | 320 × 610 | Narrow window |
| 1920 × 700 | 731 × 267 | Very short landscape window |

Activity recreation preserved onboarding route, selected night theme, check-in mood,
energy, reflection, ViewModel identity, calendar navigation, selected month/date,
and saved journal data. The draft was saved and then read through the calendar and
database. The existing onboarding/edit/calendar flow and database reopen/update tests
also passed.

`adb emu fold` and `adb emu unfold` both returned `OK`; draft assertions passed after
each command. **This AVD exposed only the DEFAULT Android device state and continued
to report an 841 × 701 dp app window at both posture checkpoints.** Therefore this
is a console-posture continuity check, not verification of an Android-driven
cover-display handoff. Cover/inner window resizing and recreation were tested
separately above. No physical foldable, occluding dual-screen hinge, tabletop posture,
or second Android OS version was tested.

An initial extended test run stalled on a redundant raw Android idle wait. It was
stopped; the wait was removed and the soft keyboard explicitly closed before the
resize matrix. The final run above completed without failures. The earlier five-size
matrix had also passed. Test display overrides were restored after completion.
