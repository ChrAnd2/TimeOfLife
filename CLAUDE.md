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
./gradlew :androidApp:testDebugUnitTest                   # Android app unit tests (ViewModels, Koin graph check)

# single test class / method
./gradlew :sharedLogic:testAndroidHostTest --tests "de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimatorTest"
./gradlew :androidApp:testDebugUnitTest --tests "*OnboardingViewModelTest.someMethod"
```

iOS app: open `iosApp/` in Xcode. It imports the static `SharedLogic` framework.

## Module layout

- **sharedLogic**: all business logic, shared by both platforms: data, domain and Koin modules. No
  UI.
- **androidApp**: the actual Android UI, written in plain Jetpack Compose in this module. It does
  **not** use `sharedUI`.
- **sharedUI** and **iosApp**: still the KMP wizard template (Greeting/`App()` placeholder). The
  real UI so far exists only in `androidApp`.

## Architecture

### sharedLogic (`commonMain`)

Layers: `data` → `domain`, wired by Koin in `di/SharedModules.kt`
(`sharedModules = platformModule + dataModule + domainModule`).
`platformModule` is `expect`/`actual` per platform and provides the `DataStore<Preferences>`
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
       `LifespanSetting.Manual`, otherwise the `Lifespan.Estimated`
       from `estimate(...)`, which includes the baseline and the adjusting `HealthFactor`s.
    2. `StudyBasedLifespanEstimator` = country/sex baseline (`BaselineLifeExpectancyProvider`) + Σ
       `HealthFactor.yearsEffect`. Not clamped: users can be older than the estimate
       (`LifeTimeBreakdown.beyondLifespan`).
    3. `LifeTimeCalculator` does only the time math (lived/remaining/per-activity time). It receives
       an already decided `Lifespan`.
    4. `GetLifeTimeBreakdownUseCase` is the UI entry point: profile + today → `LifeTimeBreakdown`.

Each health factor (smoking, alcohol, activity, BMI, conditions) lives in its own file in
`domain/model/lifespan/factor/`
and carries its own `yearsEffect`. **`docs/lifespan-estimation.md` documents the method and the
study source for every number.** Keep it in sync when you change a factor or the baseline table.

### androidApp

- `TimeOfLifeApplication` starts Koin with `appModules = sharedModules + appModule`
  (`di/AppModule.kt`). `appModule`
  provides `Clock`, the `Navigator` singleton and the ViewModels.
- Navigation uses **Navigation 3**. `ui/navigation/Navigator` is a Koin singleton that owns the back
  stack (`SnapshotStateList<NavKey>`), so ViewModels navigate directly without UI callbacks.
  `AppNavigation` renders it with
  `NavDisplay`. Routes are `@Serializable` `NavKey` objects in `Routes.kt`.
- `MainViewModel` picks the start route (`OnboardingRoute` vs `HomeRoute`) from
  `UserProfileRepository.isOnboardingCompleted`.
- Screens follow `XScreen` / `XViewModel` / `XUiState` (/ `XEvent`) per feature folder under `ui/`.
- `KoinModulesTest` verifies the whole Koin graph. Add any new constructor dependency to a module or
  this test fails. Inject `Clock` rather than calling `Clock.System` directly so ViewModels stay
  testable.

## Comments

Only add comments to functions where really necessary and have documentation benefints. Never add
comments just to explain what was generated.