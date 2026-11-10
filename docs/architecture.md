# Architecture

TimeOfLife is a Kotlin Multiplatform project in early development. The business logic is already
shared between Android and iOS. The UI so far exists only in the Android app.

## Current state

```
┌──────────────────────────┐     ┌──────────────────────────┐
│ androidApp               │     │ iosApp                   │
│ Jetpack Compose UI,      │     │ Xcode project,           │
│ ViewModels, widgets      │     │ no UI yet                │
└────────────┬─────────────┘     └────────────┬─────────────┘
             │                                │ SharedLogic.framework
             └───────────────┬────────────────┘
                             ▼
              ┌──────────────────────────────┐
              │ sharedLogic                  │
              │ data · domain · Koin modules │
              └──────────────────────────────┘
```

| Module        | Content                                                                                         |
|---------------|-------------------------------------------------------------------------------------------------|
| `sharedLogic` | Data and domain layers plus the Koin modules. No UI. Targets Android and iOS.                   |
| `androidApp`  | The Android app with the basic functionality: Compose screens, ViewModels, navigation, widgets. |
| `iosApp`      | The Xcode project. It imports `sharedLogic` as the static `SharedLogic` framework.              |
| `sharedUI`    | The future shared Compose Multiplatform UI. It still contains the project template.             |

## Target architecture

Most of the UI will be shared through Compose Multiplatform in `sharedUI`, so Android and iOS show
the same screens from one code base. The screens, ViewModels and UI components that live in
`androidApp` today will move there step by step.

Only the parts that depend on platform APIs stay native:

- **Home screen widgets:** `AppWidgetProvider`s on Android, WidgetKit on iOS.
- **App entry points:** the Android `Application`/`Activity` and the iOS app, which host the shared
  UI and start Koin.

```
┌──────────────────────────┐     ┌──────────────────────────┐
│ androidApp               │     │ iosApp                   │
│ entry point, widgets     │     │ entry point, WidgetKit   │
└────────────┬─────────────┘     └────────────┬─────────────┘
             └───────────────┬────────────────┘
                             ▼
              ┌──────────────────────────────┐
              │ sharedUI                     │
              │ Compose Multiplatform UI     │
              └──────────────┬───────────────┘
                             ▼
              ┌──────────────────────────────┐
              │ sharedLogic                  │
              │ data · domain · Koin modules │
              └──────────────────────────────┘
```

The sections below describe the code as it is today.

## sharedLogic

The module follows a `data` → `domain` layering. Koin wires it together in `di/SharedModules.kt`:

```kotlin
sharedModules = platformModule + dataModule + domainModule
```

`platformModule` is an `expect`/`actual` declaration that provides the platform's
`DataStore<Preferences>`.

### Data layer

- `UserProfileLocalDataSource` stores the whole `UserProfile` as one JSON value in Preferences
  DataStore.
- Serialization happens on DTOs (`data/datasource/dto/`) that are mapped to and from the domain
  models (`data/datasource/mapper/`). The domain models stay free of serialization annotations.
- A stored profile that can't be decoded is treated as missing, so a format change can never crash
  the app.
- `UserProfileRepository` exposes the profile and whether onboarding is completed.

### Domain layer

`domain/model/` holds pure data types, grouped into `profile/`, `lifespan/`, `activity/` and
`lifetime/`. The calculation is a small pipeline of use cases in `domain/usecase/`:

```
UserProfile + today
      │
      ▼
LifespanEstimator ──────────► Lifespan (Manual | Estimated)
      │   uses StudyBasedLifespanEstimator:
      │   baseline(country, sex) + Σ HealthFactor.yearsEffect
      ▼
LifeTimeCalculator ─────────► lived / remaining / per-activity time
      │
      ▼
GetLifeTimeBreakdownUseCase ► LifeTimeBreakdown (entry point for the UI)
```

1. **`LifespanEstimator`** is the only place that decides the life span. It returns
   `Lifespan.Manual` if the user set a value, otherwise a `Lifespan.Estimated` that carries the
   baseline and every adjusting health factor, so the UI can explain how the number came about.
2. **`StudyBasedLifespanEstimator`** adds the baseline from `BaselineLifeExpectancyProvider` and the
   `yearsEffect` of each `HealthFactor`. Each factor (smoking, alcohol, exercise, BMI, conditions)
   lives in its own file and owns its numbers. The result is not clamped: users who are older than
   their estimate see how long they have lived beyond it.
3. **`LifeTimeCalculator`** does only the time math for an already decided `Lifespan`.
4. **`GetLifeTimeBreakdownUseCase`** combines the steps for the UI.

The method and the source of every number are documented
in [Life span estimation](lifespan-estimation.md).

## androidApp

The Android UI is currently written in plain Jetpack Compose inside this module. Its structure
already follows the patterns the shared UI will use.

### Screens and state

Each feature has its own package under `ui/` and follows the same pattern:

| File         | Role                                                |
|--------------|-----------------------------------------------------|
| `XScreen`    | Stateless composables that render the state         |
| `XViewModel` | Holds the state, validates input and handles events |
| `XUiState`   | Immutable data describing the screen                |
| `XEvent`     | User actions sent from the screen to the ViewModel  |

Features: onboarding, home, activity details, profile, personal data, health settings, custom
activities and the custom activity editor. Forms shared by onboarding and profile editing (personal
data and health settings) are implemented once and reused.

Reusable building blocks such as selectors, sliders and number fields live in `ui/components/` with
a `ToL` prefix. Every screen and component has a Compose `@Preview`.

### Navigation

Navigation uses **Navigation 3**. The `Navigator` is a Koin singleton that owns the back stack, so
ViewModels navigate directly without passing callbacks through the UI. `AppNavigation` renders the
back stack with `NavDisplay`, and routes are `@Serializable` `NavKey` objects. `MainViewModel` picks
the start route (onboarding or home) based on whether onboarding is completed.

### Widgets

The *life clock* and *life bar* widgets are `AppWidgetProvider`s that draw their content onto a
`Canvas` bitmap and show it through `RemoteViews`. Large widgets are rendered at a reduced size and
scaled up, so the bitmap stays within the binder memory limit. A Compose settings activity
configures each widget.

### Dependency injection

`TimeOfLifeApplication` starts Koin with `sharedModules + appModule`. `appModule` provides the
`Clock`, the `Navigator` and the ViewModels. The current time is always injected through `Clock`, so
time-dependent logic stays deterministic in tests. `KoinModulesTest` verifies the complete
dependency graph.

## Theming

All colors come from a single `BrandPalette`, used by the Compose theme and by the widgets alike, so
an activity has the same color everywhere. See the [brand guide](brand/README.md).
