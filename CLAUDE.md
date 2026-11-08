# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## What this is

TimeOfLife is a Kotlin Multiplatform app (Android + iOS). It estimates a user's life span and breaks
the remaining time down by recurring activities (sleep, work, …). Package root:
`de.chrisander.timeoflife`.

## Commands

```bash
./gradlew :androidApp:assembleDebug                       # build the Android app
./gradlew :sharedLogic:allTests                           # all sharedLogic tests (commonTest runs on every target)
./gradlew :sharedLogic:testAndroidHostTest                # sharedLogic commonTest + androidHostTest on the JVM (fastest)
./gradlew :sharedLogic:iosSimulatorArm64Test              # sharedLogic tests on iOS simulator (macOS only)
./gradlew :sharedUI:testAndroidHostTest                   # shared UI tests on the JVM (ViewModels, mappers)
./gradlew :androidApp:testDebugUnitTest                   # Android app unit tests (widgets, Koin graph check)
./gradlew detekt                                          # lint all modules and source sets (config/detekt/detekt.yml)
./gradlew koverHtmlReport koverLog                        # merged JVM test coverage, report in build/reports/kover/

# single test class / method
./gradlew :sharedLogic:testAndroidHostTest --tests "de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimatorTest"
./gradlew :sharedUI:testAndroidHostTest --tests "*OnboardingViewModelTest.someMethod"
```

CI (`.github/workflows/ci.yml`) runs on pull requests into `develop`. It first fails if
`changelog/current.md` wasn't changed in the pull request or if a `TODO(ToL-<n>)` of the branch's
own ticket is still in the code, then runs detekt, the debug build and the JVM tests with Kover
coverage. Keep `./gradlew detekt` clean. CI runs on Linux only: never add macOS runners, so iOS
tests run locally only.

iOS app: open `iosApp/` in Xcode and set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.
The app target links the static `SharedUI` framework, the `TimeOfLifeWidget` extension target links
the static `SharedLogic` framework. Never link both into one binary.

## Module layout

- **sharedLogic**: all business logic, shared by both platforms: data, domain and Koin modules. No
  UI.
- **sharedUI**: the whole app UI in Compose Multiplatform, shared by Android and iOS: screens,
  ViewModels, navigation, theme and `uiModule`. Resources live in `src/commonMain/composeResources`
  and are accessed through `de.chrisander.timeoflife.resources.Res`. Platform APIs (number/date
  formatting, country names) go through `expect`/`actual` functions.
- **androidApp**: the Android shell (`MainActivity` shows `App()`) plus the Glance widgets and their
  settings.
- **iosApp**: the iOS shell (`ContentView` shows `MainViewController()`) plus the WidgetKit
  extension in `TimeOfLifeWidget/`, written in SwiftUI. The app writes the profile to the App Group
  `group.de.chrisander.timeoflife` (`WidgetProfileStore` in sharedLogic `iosMain`) and reloads the
  widget timelines; the widget reads it back through `WidgetProfileStore.loadLifeProgress()`.

## Architecture

### sharedLogic (`commonMain`)

Layers: `data` → `domain`, wired by Koin in `di/SharedModules.kt`
(`sharedModules = platformModule + dataModule + domainModule`). `platformModule` is `expect`/
`actual` per platform and provides the `DataStore<Preferences>`
(`data/datastore/DataStoreFactory.*`).

- `data/datasource/UserProfileLocalDataSource` persists `UserProfile` in Preferences DataStore. The
  whole profile is one JSON value (kotlinx.serialization) under a single key, mapped through
  `UserProfileDto` (DTOs in `data/datasource/dto/`, mappers in `data/datasource/mapper/`) so the
  domain models carry no serialization annotations. A profile that can't be decoded is treated as
  missing.
- `domain/model/` holds pure data types grouped into `profile/`, `lifespan/` (plus
  `lifespan/factor/`), `activity/` and `lifetime/`.
- `domain/usecase/` contains the computation pipeline:
    1. `LifespanEstimator.lifespanOf(profile, today)` is the **only** place that decides the life
       span. It returns a sealed `Lifespan`: `Lifespan.Manual` if the user set
       `LifespanSetting.Manual`, otherwise the `Lifespan.Estimated` from `estimate(...)`, which
       includes the baseline and the adjusting `HealthFactor`s.
    2. `StudyBasedLifespanEstimator` = country/sex baseline (`BaselineLifeExpectancyProvider`) + Σ
       `HealthFactor.yearsEffect`. Not clamped: users can be older than the estimate
       (`LifeTimeBreakdown.beyondLifespan`).
    3. `LifeTimeCalculator` does only the time math (lived/remaining/per-activity time). It receives
       an already decided `Lifespan`.
    4. `GetLifeTimeBreakdownUseCase` is the UI entry point: profile + today → `LifeTimeBreakdown`.

Each health factor (smoking, alcohol, activity, BMI, conditions) lives in its own file in
`domain/model/lifespan/factor/` and carries its own `yearsEffect`. **`docs/lifespan-estimation.md`
documents the method and the study source for every number.** Keep it in sync when you change a
factor or the baseline table.

### sharedUI

- `sharedUiModules = sharedModules + uiModule` (`di/UiModule.kt`). `uiModule` provides the
  `Navigator` singleton and the ViewModels. Android starts Koin in `TimeOfLifeApplication` with
  `appModules = sharedUiModules + appModule`, iOS in `initKoin()` (`MainViewController.kt`).
- Navigation uses **Navigation 3**. `ui/navigation/Navigator` is a Koin singleton that owns the back
  stack (`SnapshotStateList<NavKey>`), so ViewModels navigate directly without UI callbacks.
  `AppNavigation` renders it with `NavDisplay`. Routes are `@Serializable` `NavKey` objects in
  `Routes.kt`.
- `MainViewModel` picks the start route (`OnboardingRoute` vs `HomeRoute`) from
  `UserProfileRepository.isOnboardingCompleted`.
- Screens follow `XScreen` / `XViewModel` / `XUiState` (/ `XEvent`) per feature folder under `ui/`.
- The profile screen (`ui/profile/`) lists one row per `ProfileSetting` entry. To add a setting, add
  an entry there and handle it in `ProfileScreen` and `ProfileViewModel.onSettingClick`.
- `HealthSettingsHandler` and `PersonalSettings` are shared by onboarding and profile editing, so a
  change to the onboarding form reaches both.
- `KoinModulesTest` (androidApp) verifies the whole Koin graph. Add any new constructor dependency
  to a module or
  this test fails. Inject `Clock` rather than calling `Clock.System` directly so ViewModels stay
  testable.

### Brand

The app name is always written `TimeOfLife`. `docs/brand/README.md` is the brand guide (logo,
palette, font). Colors come from `brand/BrandPalette.kt` in sharedLogic, and activity colors from
`brand/SegmentColors.kt`, used by the Compose theme and both platforms' widgets; don't hardcode new
colors elsewhere. The launcher icons, logo mark, widget picker previews, iOS app icons and
`iosApp/TimeOfLifeWidget/Brand.swift` hardcode the palette too, so update them when a brand color
changes.

## Code style

- Models (domain models, UI states, events, enums, `model/` types) contain only data. Put each one
  in its own file, with no logic and no composables. Validation, parsing and event handling belong
  in the ViewModel (or a use case), and composables belong in UI files.
- Use extension functions only when they really help, like a small reusable helper on a type you
  don't own. Don't use them to hide logic such as event handling or validation. Write a plain
  function with explicit parameters instead (e.g. `handleHealthSettingsEvent(health, event)` rather
  than `health.apply(event)`).
- Don't write mapping extensions like `private val Sex.labelRes: Int get() = when (this) { … }`.
  Write the `when` right where the value is needed, e.g.
  `label = { stringResource(when (it) { Sex.MALE -> R.string.sex_male … }) }`. Only extract it into
  a plain function when the same mapping is needed in several places.
- Every screen, sub-screen and reusable component (e.g. `HealthSettings`, `FactorInfoButton`) needs
  a `@Preview`. Small private sub-composables don't need one.
- A file is named after its main public/internal declaration (class, composable, enum), e.g.
  `FactorInfoButton.kt` contains `fun FactorInfoButton(...)`. Rename the file when you rename that
  declaration and vice versa.
- Generic, reusable UI building blocks (selectors, sliders, input fields, …) live in
  `ui/components/` and are prefixed with `ToL`, e.g. `ToLLevelSlider`, `ToLSingleChoiceSelector`,
  `ToLNumberField`. Use them instead of building the same Material component again in a screen.
  Composables tied to one feature (e.g. `FactorInfoButton`) stay in that feature's package.
- Previews never get a fixed size: use `@Preview(showBackground = true)` without `heightDp` or
  `widthDp`.

## Comments

TODO comments must name their ticket, like `// TODO(ToL-123): ...`; detekt rejects any other TODO.

Never generate or add comments or KDoc, not even to explain a decision. Make the code explain itself
through names instead. Leave existing comments as they are unless asked to change them.