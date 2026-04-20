# Contributing

Contributions are welcome.

## Development Setup

1. Install JDK 17.
2. Install Android Studio or Android SDK command-line tools.
3. Clone the repository.
4. Run the verification command:

```bash
JAVA_HOME=/path/to/jdk17 ./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest
```

## Workflow

1. Create a branch from `main`.
2. Keep changes focused.
3. Add or update tests for game-rule changes.
4. Run verification before opening a pull request.
5. Open a pull request using the template.

## Code Guidelines

- Keep game rules in pure Kotlin under `core`.
- Keep Android and Compose dependencies out of the core engine.
- Prefer immutable game state updates.
- Use Material 3 for native Android controls.
- Keep Classic mode free of power-up behavior.

## Verification

Run:

```bash
JAVA_HOME=/path/to/jdk17 ./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest
```

Connected-device tests require an attached emulator or device:

```bash
JAVA_HOME=/path/to/jdk17 ./gradlew connectedDebugAndroidTest
```
