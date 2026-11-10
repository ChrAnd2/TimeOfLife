# Development

## Requirements

- JDK 21
- Android Studio with the Android SDK (compile SDK 37, min SDK 27)
- For iOS: macOS with Xcode

## Build and run

| Target  | How                                                                           |
|---------|-------------------------------------------------------------------------------|
| Android | Run configuration in Android Studio, or `./gradlew :androidApp:assembleDebug` |
| iOS     | Open [`iosApp/`](../iosApp) in Xcode and run it from there                    |

## Tests

```bash
./gradlew :sharedLogic:allTests                # all shared logic tests, on every target
./gradlew :sharedLogic:testAndroidHostTest     # shared logic tests on the JVM (fastest)
./gradlew :sharedLogic:iosSimulatorArm64Test   # shared logic tests on the iOS simulator (macOS only)
./gradlew :androidApp:testDebugUnitTest        # Android unit tests (ViewModels, Koin graph)
```

Single test class or method:

```bash
./gradlew :sharedLogic:testAndroidHostTest --tests "de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimatorTest"
./gradlew :androidApp:testDebugUnitTest --tests "*OnboardingViewModelTest.someMethod"
```

The tests in `commonTest` run on every target, so the calculation is verified on the JVM and on iOS
alike.

## Code quality

```bash
./gradlew detekt                     # lint all modules and source sets
./gradlew koverHtmlReport koverLog   # merged JVM coverage, report in build/reports/kover/
```

detekt is configured in [`config/detekt/detekt.yml`](../config/detekt/detekt.yml) and includes the
Compose rules.

## Branches and tickets

Every change belongs to a ticket with an ID of the form `ToL-<number>`. The branch carries that ID
in its name, e.g. `feature/ToL-17_Profile`, and is merged into `develop` through a pull request. The
ticket ID in the branch name is what connects the branch to its TODOs (see below).

## TODOs

TODO comments are tied to tickets. This keeps open work traceable and makes sure a ticket's own
TODOs are resolved before it's merged.

### Format

A TODO must name its ticket:

```kotlin
// TODO(ToL-0): Show the estimate breakdown on the home screen
```

A plain `// TODO: ...` or a TODO with any other format is rejected by detekt. The rule is a
`ForbiddenComment` entry in [`detekt.yml`](../config/detekt/detekt.yml) that matches every
case-sensitive `TODO` not directly followed by `(ToL-<number>)`.

### Two kinds of TODOs

The ticket a TODO names decides what it means:

| TODO names…                 | Meaning                                                        | Allowed when merging |
|-----------------------------|----------------------------------------------------------------|----------------------|
| **The branch's own ticket** | Work in progress: something this ticket still has to do        | No                   |
| **Another ticket**          | A follow-up: something that belongs to a later or other ticket | Yes                  |

While working on a ticket, TODOs with its own ID serve as a checklist directly in the code. Before
the pull request can be merged, all of them have to be resolved, either by doing the work or by
moving the TODO to the ticket that will take care of it.

### The CI check

The step *Check open TODOs of this ticket* in the [CI workflow](../.github/workflows/ci.yml)
enforces this on every pull request:

1. It takes the first `ToL-<number>` from the branch name. If the branch name has none, the step
   only shows a warning and is skipped.
2. It searches the whole repository for `TODO(ToL-<number>)` of that ticket. The `changelog/` folder
   is excluded, so the changelog may mention the ticket freely.
3. Every hit is reported as an error annotation on its file and line in the pull request, and the
   step fails.

To check locally before pushing, run the same search:

```bash
git grep -nF "TODO(ToL-<number>)" -- ':!changelog/'
```

## Changelog

[`changelog/current.md`](../changelog/current.md) collects the changes for the next release. Every
pull request has to add to it, so the changelog is written while the work is fresh, not
reconstructed at release time.

### Format

The file starts with a heading for the upcoming release. Its version stays the placeholder `vX.X.X`
until the release gets its number. Below it, every change is one bullet in the past tense, written
from the user's point of view:

```markdown
# TimeOfLife - Changelog - vX.X.X

- Added onboarding
- Added profile with personal data and custom activities
```

### The CI check

The step *Check changelog* in the [CI workflow](../.github/workflows/ci.yml) compares
`changelog/current.md` between the pull request's base and head commit. If the file wasn't changed,
the step fails with an error on the file and the pull request can't be merged.

The check only looks at whether the file changed, not at what changed. Writing a meaningful entry is
up to the author. For runs started manually (`workflow_dispatch`) the check is skipped, since there
is no pull request to compare.

## Continuous integration

The [CI workflow](../.github/workflows/ci.yml) runs on every pull request into `develop`:

1. [Changelog check](#the-ci-check-1): fails if `changelog/current.md` wasn't updated.
2. [TODO check](#the-ci-check): fails if a TODO of the branch's own ticket is still in the code.
3. detekt, the Android debug build and the JVM tests.
4. The Kover line coverage goes to the job summary, and all reports are uploaded as an artifact.

The TODO check runs even if the changelog check failed, so one run reports both problems.

CI runs on Linux only, so the iOS tests run locally.
