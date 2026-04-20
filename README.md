# Line 98

Native Android implementation of Line 98 built with Kotlin, Jetpack Compose, and Material 3.

## Features

- Classic Line 98 mode with a 9x9 board, pathfinding, next-ball preview, scoring, and high scores.
- Power-up mode with Bomb, Color Changer, Row Clear, and Column Clear.
- Clean Arcade visual style with a dark board-first layout.
- Local settings and high scores through DataStore Preferences.
- Unit tests for the game engine and power-up rules.
- GitHub Actions CI and tag-based debug APK releases.

## Requirements

- JDK 17
- Android SDK with API 35
- Android Studio or command-line Android SDK tools

The project includes the Gradle wrapper, so a global Gradle install is not required.

## Build

Use JDK 17 when running Gradle:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug
```

On other systems, set `JAVA_HOME` to your JDK 17 installation path.

## Test

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest
```

## Release

This repository includes a GitHub Actions workflow that creates a GitHub Release with a debug APK when a tag starting with `v` is pushed:

```bash
git tag v1.0.0
git push origin main --tags
```

The generated APK is unsigned/debug-only. Production signing is not configured.

## Dependency Updates

Dependabot is configured for:

- Gradle dependencies
- GitHub Actions

Updates are checked weekly.

## Project Structure

```text
app/src/main/java/com/line98/game/core      Game rules and pure Kotlin engine
app/src/main/java/com/line98/game/data      Local preferences
app/src/main/java/com/line98/game/ui        ViewModel, theme, and Compose UI
app/src/test                                Unit tests
app/src/androidTest                         Compose smoke tests
.github/workflows                          CI and release workflows
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

MIT. See [LICENSE](LICENSE).
