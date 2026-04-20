# Repository Guidelines

## Project Structure & Module Organization

`app/` is the Android application module. Core game rules live in `app/src/main/java/com/line98/game/core` and should stay free of Android dependencies. Persistence belongs in `app/src/main/java/com/line98/game/data`. Compose UI, screens, theme, and `GameViewModel` live under `app/src/main/java/com/line98/game/ui`. Unit tests are in `app/src/test`; Android and Compose smoke tests are in `app/src/androidTest`. GitHub CI, release, issue templates, and Dependabot config live in `.github/`.

## Build, Test, and Development Commands

Use JDK 17 for local builds. Replace the path with your installed JDK location.

```bash
JAVA_HOME=/path/to/jdk17 ./gradlew assembleDebug
JAVA_HOME=/path/to/jdk17 ./gradlew testDebugUnitTest
JAVA_HOME=/path/to/jdk17 ./gradlew assembleDebugAndroidTest
JAVA_HOME=/path/to/jdk17 ./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest
```

`assembleDebug` builds a local debug APK. `testDebugUnitTest` runs JVM tests. `assembleDebugAndroidTest` compiles instrumentation tests. `connectedDebugAndroidTest` requires a running emulator or attached Android device.

## Coding Style & Naming Conventions

Use Kotlin with 4-space indentation. Name classes, objects, and composables in `PascalCase`; functions, properties, and local variables in `camelCase`. Prefer immutable state updates with `copy` and keep files focused on one responsibility. Game rules belong in `core`; UI code should use Jetpack Compose, Material 3 components, and lifecycle-aware state collection.

## Testing Guidelines

Use JUnit for pure Kotlin rule tests and Compose UI tests for Android smoke coverage. Test names should describe behavior, for example `clearingFiveInLineKeepsExistingPreviewWhenNoSpawnOccurs`. Add or update tests for rule changes, power-up behavior, persistence-sensitive logic, and regressions found during playtesting.

## Commit & Pull Request Guidelines

The project history uses Conventional Commit prefixes such as `feat:`, `fix:`, `docs:`, `ci:`, and `chore:`. Keep commits scoped to one logical change. Pull requests should include a summary, test plan, linked issue when relevant, and screenshots or screen recordings for visible UI changes.

## Security & Configuration Tips

Do not commit `local.properties`, keystores, `keystore.properties`, or signing secrets. The GitHub release workflow currently publishes debug APKs; signed release builds require repository secrets and explicit signing configuration.
