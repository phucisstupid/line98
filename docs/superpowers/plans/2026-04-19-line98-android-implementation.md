# Line 98 Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Kotlin Android Line 98 game with Classic mode, Power-up mode, Material 3 native UI, local settings, and testable game rules.

**Architecture:** Use a single Android app module. Keep game rules in plain Kotlin under `app/src/main/java/com/line98/game/core`, Android state and persistence under `app/src/main/java/com/line98/game/data` and `.../ui`, and Compose Material 3 screens under `.../ui`. The board is custom Compose UI, while menus, buttons, settings, dialogs, and switches use Material 3.

**Tech Stack:** Kotlin, Gradle Android plugin, Jetpack Compose, Compose Material 3, AndroidX Lifecycle ViewModel, DataStore Preferences, JUnit.

---

## File Structure

- Create `settings.gradle.kts`: Gradle plugin management and module include.
- Create `build.gradle.kts`: root plugin versions.
- Create `app/build.gradle.kts`: Android app module with Compose, lifecycle, DataStore, and test dependencies.
- Create `app/src/main/AndroidManifest.xml`: main activity declaration.
- Create `app/src/main/java/com/line98/game/MainActivity.kt`: Compose app entry point.
- Create `app/src/main/java/com/line98/game/core/Models.kt`: `Position`, `BallColor`, `Cell`, `GameMode`, `PowerUpType`, `PowerUpCharges`, `GameState`.
- Create `app/src/main/java/com/line98/game/core/Board.kt`: immutable 9x9 board model and mutation helpers.
- Create `app/src/main/java/com/line98/game/core/RandomSource.kt`: deterministic random abstraction and Kotlin random implementation.
- Create `app/src/main/java/com/line98/game/core/GameEngine.kt`: move validation, pathfinding, line clearing, scoring, spawning, game over.
- Create `app/src/main/java/com/line98/game/core/PowerUpEngine.kt`: Bomb, Color Changer, Row/Column Clear.
- Create `app/src/main/java/com/line98/game/data/UserPreferences.kt`: DataStore keys and flows for scores/settings.
- Create `app/src/main/java/com/line98/game/ui/GameViewModel.kt`: unidirectional state and game actions.
- Create `app/src/main/java/com/line98/game/ui/Line98App.kt`: simple screen routing.
- Create `app/src/main/java/com/line98/game/ui/theme/Theme.kt`: Material 3 Clean Arcade theme.
- Create `app/src/main/java/com/line98/game/ui/screens/MenuScreen.kt`: Material 3 menu.
- Create `app/src/main/java/com/line98/game/ui/screens/GameScreen.kt`: HUD, board, next preview, power-up bar.
- Create `app/src/main/java/com/line98/game/ui/screens/SettingsScreen.kt`: Material 3 settings toggles.
- Create `app/src/test/java/com/line98/game/core/GameEngineTest.kt`: engine tests.
- Create `app/src/test/java/com/line98/game/core/PowerUpEngineTest.kt`: power-up tests.

## Task 1: Scaffold Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/line98/game/MainActivity.kt`
- Create: `app/src/main/java/com/line98/game/ui/Line98App.kt`
- Create: `app/src/main/java/com/line98/game/ui/theme/Theme.kt`

- [ ] **Step 1: Create Gradle settings**

Write `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Line98"
include(":app")
```

- [ ] **Step 2: Create root Gradle build file**

Write `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

- [ ] **Step 3: Create app Gradle build file**

Write `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.line98.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.line98.game"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 4: Create Android manifest**

Write `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="Line 98"
        android:theme="@style/Theme.Line98">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 5: Create app theme style**

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="Theme.Line98" parent="android:style/Theme.Material.NoActionBar">
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowActionBar">false</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowLightNavigationBar">false</item>
    </style>
</resources>
```

- [ ] **Step 6: Create MainActivity**

Write `app/src/main/java/com/line98/game/MainActivity.kt`:

```kotlin
package com.line98.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.line98.game.ui.Line98App
import com.line98.game.ui.theme.Line98Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Line98Theme {
                Line98App()
            }
        }
    }
}
```

- [ ] **Step 7: Create initial Compose shell**

Write `app/src/main/java/com/line98/game/ui/Line98App.kt`:

```kotlin
package com.line98.game.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Line98App() {
    Text("Line 98")
}
```

Write `app/src/main/java/com/line98/game/ui/theme/Theme.kt`:

```kotlin
package com.line98.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanArcadeColors = darkColorScheme(
    primary = Color(0xFF35C2A1),
    secondary = Color(0xFFFFCE47),
    tertiary = Color(0xFFE84A5F),
    background = Color(0xFF101114),
    surface = Color(0xFF181A1F),
    onPrimary = Color(0xFF061512),
    onSecondary = Color(0xFF1D1700),
    onTertiary = Color.White,
    onBackground = Color(0xFFF7F7F2),
    onSurface = Color(0xFFF7F7F2),
)

@Composable
fun Line98Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanArcadeColors,
        content = content,
    )
}
```

- [ ] **Step 8: Verify scaffold**

Run: `./gradlew test`

Expected: Gradle downloads dependencies and finishes with `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit scaffold**

Run:

```bash
git add settings.gradle.kts build.gradle.kts app
git commit -m "feat: scaffold Android Compose app"
```

## Task 2: Core Models And Board

**Files:**
- Create: `app/src/main/java/com/line98/game/core/Models.kt`
- Create: `app/src/main/java/com/line98/game/core/Board.kt`
- Create: `app/src/test/java/com/line98/game/core/GameEngineTest.kt`

- [ ] **Step 1: Write failing board tests**

Write `app/src/test/java/com/line98/game/core/GameEngineTest.kt`:

```kotlin
package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    @Test
    fun boardStartsEmptyAndSupportsCellUpdates() {
        val board = Board.empty()
        val position = Position(2, 3)

        assertTrue(board[position].isEmpty)

        val updated = board.set(position, Cell.Occupied(BallColor.Red))

        assertEquals(Cell.Occupied(BallColor.Red), updated[position])
        assertTrue(board[position].isEmpty)
    }

    @Test
    fun positionRejectsOutOfBoundsValues() {
        assertFalse(Position.isValid(row = -1, col = 0))
        assertFalse(Position.isValid(row = 9, col = 0))
        assertFalse(Position.isValid(row = 0, col = 9))
        assertTrue(Position.isValid(row = 8, col = 8))
    }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `./gradlew test --tests com.line98.game.core.GameEngineTest`

Expected: FAIL because `Board`, `Position`, `Cell`, and `BallColor` do not exist.

- [ ] **Step 3: Implement core models**

Write `app/src/main/java/com/line98/game/core/Models.kt`:

```kotlin
package com.line98.game.core

data class Position(val row: Int, val col: Int) {
    init {
        require(isValid(row, col)) { "Position out of bounds: row=$row col=$col" }
    }

    fun neighbors(): List<Position> = listOf(
        row - 1 to col,
        row + 1 to col,
        row to col - 1,
        row to col + 1,
    ).filter { (r, c) -> isValid(r, c) }
        .map { (r, c) -> Position(r, c) }

    companion object {
        const val Size = 9

        fun isValid(row: Int, col: Int): Boolean =
            row in 0 until Size && col in 0 until Size
    }
}

enum class BallColor {
    Red, Green, Blue, Yellow, Purple, Cyan
}

sealed interface Cell {
    val isEmpty: Boolean

    data object Empty : Cell {
        override val isEmpty = true
    }

    data class Occupied(val color: BallColor) : Cell {
        override val isEmpty = false
    }
}

enum class GameMode {
    Classic, PowerUp
}

enum class PowerUpType {
    Bomb, ColorChanger, RowClear, ColumnClear
}

data class PowerUpCharges(
    val bomb: Int = 0,
    val colorChanger: Int = 0,
    val rowColumnClear: Int = 0,
) {
    fun count(type: PowerUpType): Int = when (type) {
        PowerUpType.Bomb -> bomb
        PowerUpType.ColorChanger -> colorChanger
        PowerUpType.RowClear,
        PowerUpType.ColumnClear -> rowColumnClear
    }
}
```

- [ ] **Step 4: Implement board**

Write `app/src/main/java/com/line98/game/core/Board.kt`:

```kotlin
package com.line98.game.core

data class Board private constructor(
    private val cells: List<Cell>,
) {
    operator fun get(position: Position): Cell =
        cells[index(position)]

    fun set(position: Position, cell: Cell): Board =
        Board(cells.toMutableList().also { it[index(position)] = cell })

    fun clear(position: Position): Board =
        set(position, Cell.Empty)

    fun positions(): List<Position> =
        List(Position.Size * Position.Size) { index ->
            Position(index / Position.Size, index % Position.Size)
        }

    fun emptyPositions(): List<Position> =
        positions().filter { this[it].isEmpty }

    fun occupiedPositions(): List<Position> =
        positions().filter { !this[it].isEmpty }

    fun isFull(): Boolean =
        emptyPositions().isEmpty()

    private fun index(position: Position): Int =
        position.row * Position.Size + position.col

    companion object {
        fun empty(): Board =
            Board(List(Position.Size * Position.Size) { Cell.Empty })
    }
}
```

- [ ] **Step 5: Run tests and verify they pass**

Run: `./gradlew test --tests com.line98.game.core.GameEngineTest`

Expected: PASS.

- [ ] **Step 6: Commit models and board**

Run:

```bash
git add app/src/main/java/com/line98/game/core app/src/test/java/com/line98/game/core/GameEngineTest.kt
git commit -m "feat: add core board model"
```

## Task 3: Game Engine Movement, Lines, Spawning

**Files:**
- Create: `app/src/main/java/com/line98/game/core/RandomSource.kt`
- Create: `app/src/main/java/com/line98/game/core/GameEngine.kt`
- Modify: `app/src/main/java/com/line98/game/core/Models.kt`
- Modify: `app/src/test/java/com/line98/game/core/GameEngineTest.kt`

- [ ] **Step 1: Extend tests for classic rules**

Append to `GameEngineTest`:

```kotlin
    @Test
    fun pathExistsOnlyThroughEmptyOrthogonalCells() {
        val engine = GameEngine(FixedRandomSource())
        val board = Board.empty()
            .set(Position(0, 0), Cell.Occupied(BallColor.Red))
            .set(Position(0, 1), Cell.Occupied(BallColor.Blue))
            .set(Position(1, 0), Cell.Occupied(BallColor.Blue))

        assertFalse(engine.hasPath(board, Position(0, 0), Position(2, 2)))

        val openBoard = board.clear(Position(1, 0))
        assertTrue(engine.hasPath(openBoard, Position(0, 0), Position(2, 2)))
    }

    @Test
    fun movingToReachableCellMovesBallAndSpawnsWhenNoLineClears() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val state = GameState.initial(GameMode.Classic).copy(
            board = Board.empty().set(Position(0, 0), Cell.Occupied(BallColor.Red)),
            nextBalls = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow),
        )

        val result = engine.move(state, Position(0, 0), Position(0, 2))

        assertTrue(result.board[Position(0, 0)].isEmpty)
        assertEquals(Cell.Occupied(BallColor.Red), result.board[Position(0, 2)])
        assertEquals(Cell.Occupied(BallColor.Blue), result.board[Position(8, 8)])
        assertEquals(Cell.Occupied(BallColor.Green), result.board[Position(8, 7)])
        assertEquals(Cell.Occupied(BallColor.Yellow), result.board[Position(8, 6)])
    }

    @Test
    fun clearingFiveInLineScoresAndDoesNotSpawn() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val baseBoard = Board.empty()
            .set(Position(0, 0), Cell.Occupied(BallColor.Red))
            .set(Position(0, 1), Cell.Occupied(BallColor.Red))
            .set(Position(0, 2), Cell.Occupied(BallColor.Red))
            .set(Position(0, 3), Cell.Occupied(BallColor.Red))
            .set(Position(2, 0), Cell.Occupied(BallColor.Red))
        val state = GameState.initial(GameMode.Classic).copy(
            board = baseBoard,
            nextBalls = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow),
        )

        val result = engine.move(state, Position(2, 0), Position(0, 4))

        assertTrue(result.board[Position(0, 0)].isEmpty)
        assertTrue(result.board[Position(0, 4)].isEmpty)
        assertTrue(result.board[Position(8, 8)].isEmpty)
        assertEquals(10, result.score)
    }
```

Add this helper inside the test file:

```kotlin
private class FixedRandomSource(
    private val positions: List<Position> = emptyList(),
) : RandomSource {
    private var positionIndex = 0
    private var colorIndex = 0
    private val colors = BallColor.entries

    override fun nextInt(bound: Int): Int {
        if (positions.isNotEmpty() && bound == Position.Size * Position.Size) {
            val position = positions[positionIndex++ % positions.size]
            return position.row * Position.Size + position.col
        }
        return colorIndex++ % bound.coerceAtLeast(1)
    }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `./gradlew test --tests com.line98.game.core.GameEngineTest`

Expected: FAIL because `GameState`, `GameEngine`, and `RandomSource` do not exist.

- [ ] **Step 3: Add game state**

Append to `Models.kt`:

```kotlin
data class GameState(
    val mode: GameMode,
    val board: Board,
    val score: Int,
    val highScore: Int,
    val nextBalls: List<BallColor>,
    val selected: Position? = null,
    val activePowerUp: PowerUpType? = null,
    val charges: PowerUpCharges = PowerUpCharges(),
    val isGameOver: Boolean = false,
    val message: String? = null,
) {
    companion object {
        fun initial(mode: GameMode): GameState =
            GameState(
                mode = mode,
                board = Board.empty(),
                score = 0,
                highScore = 0,
                nextBalls = listOf(BallColor.Red, BallColor.Green, BallColor.Blue),
            )
    }
}
```

- [ ] **Step 4: Add random source**

Write `app/src/main/java/com/line98/game/core/RandomSource.kt`:

```kotlin
package com.line98.game.core

import kotlin.random.Random

fun interface RandomSource {
    fun nextInt(bound: Int): Int
}

class KotlinRandomSource(
    private val random: Random = Random.Default,
) : RandomSource {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
```

- [ ] **Step 5: Implement game engine**

Write `app/src/main/java/com/line98/game/core/GameEngine.kt`:

```kotlin
package com.line98.game.core

class GameEngine(
    private val random: RandomSource = KotlinRandomSource(),
) {
    fun hasPath(board: Board, from: Position, to: Position): Boolean {
        if (from == to || board[from].isEmpty || !board[to].isEmpty) return false
        val visited = mutableSetOf<Position>()
        val queue = ArrayDeque<Position>()
        queue.add(from)
        visited.add(from)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (next in current.neighbors()) {
                if (next == to) return true
                if (next !in visited && board[next].isEmpty) {
                    visited.add(next)
                    queue.add(next)
                }
            }
        }
        return false
    }

    fun move(state: GameState, from: Position, to: Position): GameState {
        if (state.isGameOver) return state
        if (!hasPath(state.board, from, to)) return state.copy(message = "Blocked path")

        val movingCell = state.board[from]
        var board = state.board.clear(from).set(to, movingCell)
        val cleared = findLines(board, to)
        var score = state.score

        if (cleared.isNotEmpty()) {
            board = clearPositions(board, cleared)
            score += cleared.size * 2
        } else {
            board = spawnBalls(board, state.nextBalls)
        }

        val nextBalls = generateNextBalls()
        return state.copy(
            board = board,
            score = score,
            highScore = maxOf(state.highScore, score),
            nextBalls = nextBalls,
            selected = null,
            isGameOver = board.isFull(),
            message = null,
        )
    }

    fun findLines(board: Board, origin: Position): Set<Position> {
        val cell = board[origin] as? Cell.Occupied ?: return emptySet()
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        val result = mutableSetOf<Position>()

        for ((dr, dc) in directions) {
            val line = mutableSetOf(origin)
            line.addAll(walk(board, origin, cell.color, dr, dc))
            line.addAll(walk(board, origin, cell.color, -dr, -dc))
            if (line.size >= 5) result.addAll(line)
        }
        return result
    }

    fun spawnBalls(board: Board, colors: List<BallColor>): Board {
        var current = board
        for (color in colors) {
            val empty = current.emptyPositions()
            if (empty.isEmpty()) return current
            val rawIndex = random.nextInt(Position.Size * Position.Size)
            val preferred = Position(rawIndex / Position.Size, rawIndex % Position.Size)
            val chosen = if (current[preferred].isEmpty) preferred else empty[random.nextInt(empty.size)]
            current = current.set(chosen, Cell.Occupied(color))
        }
        return current
    }

    private fun generateNextBalls(): List<BallColor> =
        List(3) { BallColor.entries[random.nextInt(BallColor.entries.size)] }

    private fun clearPositions(board: Board, positions: Set<Position>): Board =
        positions.fold(board) { current, position -> current.clear(position) }

    private fun walk(
        board: Board,
        origin: Position,
        color: BallColor,
        rowDelta: Int,
        colDelta: Int,
    ): Set<Position> {
        val result = mutableSetOf<Position>()
        var row = origin.row + rowDelta
        var col = origin.col + colDelta
        while (Position.isValid(row, col)) {
            val position = Position(row, col)
            if (board[position] != Cell.Occupied(color)) break
            result.add(position)
            row += rowDelta
            col += colDelta
        }
        return result
    }
}
```

- [ ] **Step 6: Run tests and verify they pass**

Run: `./gradlew test --tests com.line98.game.core.GameEngineTest`

Expected: PASS.

- [ ] **Step 7: Commit engine**

Run:

```bash
git add app/src/main/java/com/line98/game/core app/src/test/java/com/line98/game/core/GameEngineTest.kt
git commit -m "feat: implement classic game engine"
```

## Task 4: Power-up Engine

**Files:**
- Create: `app/src/main/java/com/line98/game/core/PowerUpEngine.kt`
- Create: `app/src/test/java/com/line98/game/core/PowerUpEngineTest.kt`
- Modify: `app/src/main/java/com/line98/game/core/Models.kt`

- [ ] **Step 1: Write failing power-up tests**

Write `app/src/test/java/com/line98/game/core/PowerUpEngineTest.kt`:

```kotlin
package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerUpEngineTest {
    private val engine = PowerUpEngine()

    @Test
    fun classicModeRejectsPowerUps() {
        val state = GameState.initial(GameMode.Classic).copy(
            charges = PowerUpCharges(bomb = 1),
            board = Board.empty().set(Position(4, 4), Cell.Occupied(BallColor.Red)),
        )

        val result = engine.applyBomb(state, Position(4, 4))

        assertEquals(state.board, result.board)
        assertEquals("Power-ups are disabled in Classic mode", result.message)
    }

    @Test
    fun bombClearsSelectedCellAndNeighbors() {
        val board = Board.empty()
            .set(Position(4, 4), Cell.Occupied(BallColor.Red))
            .set(Position(4, 5), Cell.Occupied(BallColor.Blue))
            .set(Position(5, 4), Cell.Occupied(BallColor.Green))
            .set(Position(6, 6), Cell.Occupied(BallColor.Yellow))
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = board,
            charges = PowerUpCharges(bomb = 1),
        )

        val result = engine.applyBomb(state, Position(4, 4))

        assertTrue(result.board[Position(4, 4)].isEmpty)
        assertTrue(result.board[Position(4, 5)].isEmpty)
        assertTrue(result.board[Position(5, 4)].isEmpty)
        assertEquals(Cell.Occupied(BallColor.Yellow), result.board[Position(6, 6)])
        assertEquals(0, result.charges.bomb)
    }

    @Test
    fun colorChangerChangesOneBall() {
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = Board.empty().set(Position(2, 2), Cell.Occupied(BallColor.Red)),
            charges = PowerUpCharges(colorChanger = 1),
        )

        val result = engine.applyColorChanger(state, Position(2, 2), BallColor.Blue)

        assertEquals(Cell.Occupied(BallColor.Blue), result.board[Position(2, 2)])
        assertEquals(0, result.charges.colorChanger)
    }

    @Test
    fun rowAndColumnClearSpendSharedCharge() {
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = Board.empty()
                .set(Position(1, 1), Cell.Occupied(BallColor.Red))
                .set(Position(1, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 4), Cell.Occupied(BallColor.Green)),
            charges = PowerUpCharges(rowColumnClear = 2),
        )

        val rowResult = engine.applyRowClear(state, row = 1)
        val columnResult = engine.applyColumnClear(rowResult, col = 4)

        assertTrue(rowResult.board[Position(1, 1)].isEmpty)
        assertTrue(rowResult.board[Position(1, 2)].isEmpty)
        assertEquals(1, rowResult.charges.rowColumnClear)
        assertTrue(columnResult.board[Position(3, 4)].isEmpty)
        assertEquals(0, columnResult.charges.rowColumnClear)
    }
}
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `./gradlew test --tests com.line98.game.core.PowerUpEngineTest`

Expected: FAIL because `PowerUpEngine` does not exist.

- [ ] **Step 3: Add charge spending helpers**

Add to `PowerUpCharges` in `Models.kt`:

```kotlin
    fun spend(type: PowerUpType): PowerUpCharges = when (type) {
        PowerUpType.Bomb -> copy(bomb = (bomb - 1).coerceAtLeast(0))
        PowerUpType.ColorChanger -> copy(colorChanger = (colorChanger - 1).coerceAtLeast(0))
        PowerUpType.RowClear,
        PowerUpType.ColumnClear -> copy(rowColumnClear = (rowColumnClear - 1).coerceAtLeast(0))
    }
```

- [ ] **Step 4: Implement power-up engine**

Write `app/src/main/java/com/line98/game/core/PowerUpEngine.kt`:

```kotlin
package com.line98.game.core

class PowerUpEngine {
    fun applyBomb(state: GameState, center: Position): GameState =
        applyCharged(state, PowerUpType.Bomb) {
            val targets = (-1..1).flatMap { rowOffset ->
                (-1..1).mapNotNull { colOffset ->
                    val row = center.row + rowOffset
                    val col = center.col + colOffset
                    if (Position.isValid(row, col)) Position(row, col) else null
                }
            }
            clearTargets(state, targets, PowerUpType.Bomb)
        }

    fun applyColorChanger(state: GameState, position: Position, color: BallColor): GameState =
        applyCharged(state, PowerUpType.ColorChanger) {
            if (state.board[position].isEmpty) {
                state.copy(message = "Select a ball")
            } else {
                state.copy(
                    board = state.board.set(position, Cell.Occupied(color)),
                    charges = state.charges.spend(PowerUpType.ColorChanger),
                    message = null,
                )
            }
        }

    fun applyRowClear(state: GameState, row: Int): GameState =
        applyCharged(state, PowerUpType.RowClear) {
            clearTargets(state, List(Position.Size) { col -> Position(row, col) }, PowerUpType.RowClear)
        }

    fun applyColumnClear(state: GameState, col: Int): GameState =
        applyCharged(state, PowerUpType.ColumnClear) {
            clearTargets(state, List(Position.Size) { row -> Position(row, col) }, PowerUpType.ColumnClear)
        }

    private fun applyCharged(
        state: GameState,
        type: PowerUpType,
        action: () -> GameState,
    ): GameState {
        if (state.mode != GameMode.PowerUp) {
            return state.copy(message = "Power-ups are disabled in Classic mode")
        }
        if (state.charges.count(type) <= 0) {
            return state.copy(message = "No charges")
        }
        return action()
    }

    private fun clearTargets(
        state: GameState,
        targets: List<Position>,
        type: PowerUpType,
    ): GameState {
        val board = targets.fold(state.board) { current, position -> current.clear(position) }
        return state.copy(
            board = board,
            charges = state.charges.spend(type),
            score = state.score + targets.count { !state.board[it].isEmpty },
            message = null,
        )
    }
}
```

- [ ] **Step 5: Run tests and verify they pass**

Run: `./gradlew test --tests com.line98.game.core.PowerUpEngineTest`

Expected: PASS.

- [ ] **Step 6: Commit power-ups**

Run:

```bash
git add app/src/main/java/com/line98/game/core app/src/test/java/com/line98/game/core/PowerUpEngineTest.kt
git commit -m "feat: add power-up engine"
```

## Task 5: Preferences And ViewModel

**Files:**
- Create: `app/src/main/java/com/line98/game/data/UserPreferences.kt`
- Create: `app/src/main/java/com/line98/game/ui/GameViewModel.kt`
- Modify: `app/src/main/java/com/line98/game/ui/Line98App.kt`

- [ ] **Step 1: Create preferences model**

Write `app/src/main/java/com/line98/game/data/UserPreferences.kt`:

```kotlin
package com.line98.game.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.line98.game.core.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("line98_preferences")

data class UserSettings(
    val classicHighScore: Int = 0,
    val powerUpHighScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val lastMode: GameMode = GameMode.Classic,
)

class UserPreferences(
    private val context: Context,
) {
    private object Keys {
        val ClassicHighScore = intPreferencesKey("classic_high_score")
        val PowerUpHighScore = intPreferencesKey("power_up_high_score")
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
        val HapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val LastMode = stringPreferencesKey("last_mode")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            classicHighScore = prefs[Keys.ClassicHighScore] ?: 0,
            powerUpHighScore = prefs[Keys.PowerUpHighScore] ?: 0,
            soundEnabled = prefs[Keys.SoundEnabled] ?: true,
            hapticsEnabled = prefs[Keys.HapticsEnabled] ?: true,
            lastMode = prefs[Keys.LastMode]?.let { runCatching { GameMode.valueOf(it) }.getOrNull() } ?: GameMode.Classic,
        )
    }

    suspend fun saveHighScore(mode: GameMode, score: Int) {
        context.dataStore.edit { prefs ->
            val key = if (mode == GameMode.Classic) Keys.ClassicHighScore else Keys.PowerUpHighScore
            prefs[key] = maxOf(prefs[key] ?: 0, score)
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SoundEnabled] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HapticsEnabled] = enabled }
    }

    suspend fun setLastMode(mode: GameMode) {
        context.dataStore.edit { prefs -> prefs[Keys.LastMode] = mode.name }
    }
}
```

- [ ] **Step 2: Create ViewModel**

Write `app/src/main/java/com/line98/game/ui/GameViewModel.kt`:

```kotlin
package com.line98.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.line98.game.core.BallColor
import com.line98.game.core.GameEngine
import com.line98.game.core.GameMode
import com.line98.game.core.GameState
import com.line98.game.core.Position
import com.line98.game.core.PowerUpEngine
import com.line98.game.core.PowerUpType
import com.line98.game.data.UserPreferences
import com.line98.game.data.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    Menu, Game, Settings
}

data class UiState(
    val screen: Screen = Screen.Menu,
    val game: GameState = GameState.initial(GameMode.Classic),
    val settings: UserSettings = UserSettings(),
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    private val gameEngine = GameEngine()
    private val powerUpEngine = PowerUpEngine()
    private val localState = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> = combine(localState, preferences.settings) { local, settings ->
        local.copy(settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun openMenu() {
        localState.value = localState.value.copy(screen = Screen.Menu)
    }

    fun openSettings() {
        localState.value = localState.value.copy(screen = Screen.Settings)
    }

    fun startGame(mode: GameMode) {
        viewModelScope.launch { preferences.setLastMode(mode) }
        val highScore = if (mode == GameMode.Classic) {
            uiState.value.settings.classicHighScore
        } else {
            uiState.value.settings.powerUpHighScore
        }
        localState.value = UiState(
            screen = Screen.Game,
            game = GameState.initial(mode).copy(highScore = highScore),
            settings = uiState.value.settings,
        )
    }

    fun tapCell(position: Position) {
        val game = localState.value.game
        val powerUp = game.activePowerUp
        if (powerUp != null) {
            val nextGame = when (powerUp) {
                PowerUpType.Bomb -> powerUpEngine.applyBomb(game, position)
                PowerUpType.ColorChanger -> {
                    val current = game.board[position]
                    val targetColor = BallColor.entries.first { current != com.line98.game.core.Cell.Occupied(it) }
                    powerUpEngine.applyColorChanger(game, position, targetColor)
                }
                PowerUpType.RowClear -> powerUpEngine.applyRowClear(game, position.row)
                PowerUpType.ColumnClear -> powerUpEngine.applyColumnClear(game, position.col)
            }.copy(activePowerUp = null)
            updateGame(nextGame)
            return
        }
        val selected = game.selected
        val cell = game.board[position]
        val nextGame = when {
            selected == null && !cell.isEmpty -> game.copy(selected = position, message = null)
            selected != null && position == selected -> game.copy(selected = null, message = null)
            selected != null && cell.isEmpty -> gameEngine.move(game, selected, position)
            else -> game.copy(selected = position, message = null)
        }
        updateGame(nextGame)
    }

    fun activatePowerUp(type: PowerUpType) {
        updateGame(localState.value.game.copy(activePowerUp = type, message = null))
    }

    fun useBomb(position: Position) {
        updateGame(powerUpEngine.applyBomb(localState.value.game, position))
    }

    fun useColorChanger(position: Position, color: BallColor) {
        updateGame(powerUpEngine.applyColorChanger(localState.value.game, position, color))
    }

    fun useRowClear(row: Int) {
        updateGame(powerUpEngine.applyRowClear(localState.value.game, row))
    }

    fun useColumnClear(col: Int) {
        updateGame(powerUpEngine.applyColumnClear(localState.value.game, col))
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSoundEnabled(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setHapticsEnabled(enabled) }
    }

    private fun updateGame(game: GameState) {
        localState.value = localState.value.copy(game = game)
        viewModelScope.launch { preferences.saveHighScore(game.mode, game.highScore) }
    }
}
```

- [ ] **Step 3: Wire ViewModel into app shell**

Replace `Line98App.kt`:

```kotlin
package com.line98.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.line98.game.ui.screens.GameScreen
import com.line98.game.ui.screens.MenuScreen
import com.line98.game.ui.screens.SettingsScreen

@Composable
fun Line98App(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.screen) {
        Screen.Menu -> MenuScreen(
            settings = state.settings,
            onStartClassic = { viewModel.startGame(com.line98.game.core.GameMode.Classic) },
            onStartPowerUp = { viewModel.startGame(com.line98.game.core.GameMode.PowerUp) },
            onSettings = viewModel::openSettings,
        )
        Screen.Game -> GameScreen(
            state = state.game,
            onCellTap = viewModel::tapCell,
            onPowerUp = viewModel::activatePowerUp,
            onMenu = viewModel::openMenu,
            onRestart = { viewModel.startGame(state.game.mode) },
        )
        Screen.Settings -> SettingsScreen(
            settings = state.settings,
            onSoundChanged = viewModel::setSoundEnabled,
            onHapticsChanged = viewModel::setHapticsEnabled,
            onBack = viewModel::openMenu,
        )
    }
}
```

- [ ] **Step 4: Run compile and expect screen imports to fail**

Run: `./gradlew test`

Expected: FAIL because `MenuScreen`, `GameScreen`, and `SettingsScreen` do not exist yet.

- [ ] **Step 5: Commit ViewModel after UI task is completed**

Do not commit this task until Task 6 adds screen files and the build passes.

## Task 6: Material 3 Screens And Clean Arcade Board

**Files:**
- Create: `app/src/main/java/com/line98/game/ui/screens/MenuScreen.kt`
- Create: `app/src/main/java/com/line98/game/ui/screens/GameScreen.kt`
- Create: `app/src/main/java/com/line98/game/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Create MenuScreen with Material 3 buttons**

Write `app/src/main/java/com/line98/game/ui/screens/MenuScreen.kt`:

```kotlin
package com.line98.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.line98.game.data.UserSettings

@Composable
fun MenuScreen(
    settings: UserSettings,
    onStartClassic: () -> Unit,
    onStartPowerUp: () -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Line 98", style = MaterialTheme.typography.displayMedium)
            Text("Classic best: ${settings.classicHighScore}")
            Text("Power-up best: ${settings.powerUpHighScore}")
            Button(
                onClick = onStartClassic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .semantics { contentDescription = "Start Classic mode" },
            ) {
                Text("Classic")
            }
            Button(
                onClick = onStartPowerUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .semantics { contentDescription = "Start Power-up mode" },
            ) {
                Text("Power-up")
            }
            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .semantics { contentDescription = "Open settings" },
            ) {
                Text("Settings")
            }
        }
    }
}
```

- [ ] **Step 2: Create GameScreen with custom board and Material controls**

Write `app/src/main/java/com/line98/game/ui/screens/GameScreen.kt`:

```kotlin
package com.line98.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.line98.game.core.BallColor
import com.line98.game.core.Cell
import com.line98.game.core.GameMode
import com.line98.game.core.GameState
import com.line98.game.core.Position
import com.line98.game.core.PowerUpType

@Composable
fun GameScreen(
    state: GameState,
    onCellTap: (Position) -> Unit,
    onPowerUp: (PowerUpType) -> Unit,
    onMenu: () -> Unit,
    onRestart: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(state.mode.name, style = MaterialTheme.typography.titleLarge)
                Text("Score ${state.score}")
                Text("Best ${state.highScore}")
            }
            BoardView(state = state, onCellTap = onCellTap)
            NextPreview(state.nextBalls)
            if (state.mode == GameMode.PowerUp) {
                PowerUpBar(state = state, onPowerUp = onPowerUp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMenu) { Text("Menu") }
                Button(onClick = onRestart) { Text("Restart") }
            }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.tertiary) }
        }
    }
}

@Composable
private fun BoardView(state: GameState, onCellTap: (Position) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = "Line 98 board" },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val col = (offset.x / (size.width / Position.Size)).toInt().coerceIn(0, Position.Size - 1)
                        val row = (offset.y / (size.height / Position.Size)).toInt().coerceIn(0, Position.Size - 1)
                        onCellTap(Position(row, col))
                    }
                },
        ) {
            val px = size.width / Position.Size
            val py = size.height / Position.Size
            for (row in 0 until Position.Size) {
                for (col in 0 until Position.Size) {
                    val position = Position(row, col)
                    val left = col * px
                    val top = row * py
                    drawRect(
                        color = Color(0xFF2A2D32),
                        topLeft = Offset(left + 2f, top + 2f),
                        size = Size(px - 4f, py - 4f),
                    )
                    val cell = state.board[position]
                    if (cell is Cell.Occupied) {
                        drawCircle(
                            color = cell.color.toComposeColor(),
                            radius = px.coerceAtMost(py) * 0.34f,
                            center = Offset(left + px / 2f, top + py / 2f),
                        )
                    }
                    if (state.selected == position || state.activePowerUp != null) {
                        drawCircle(
                            color = if (state.selected == position) Color.White else Color(0xFFFFCE47),
                            radius = px.coerceAtMost(py) * 0.42f,
                            center = Offset(left + px / 2f, top + py / 2f),
                            style = Stroke(width = 4f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextPreview(colors: List<BallColor>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Next")
        colors.forEach { color ->
            AssistChip(onClick = {}, label = { Text(color.name) })
        }
    }
}

@Composable
private fun PowerUpBar(state: GameState, onPowerUp: (PowerUpType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = { onPowerUp(PowerUpType.Bomb) }, modifier = Modifier.weight(1f)) {
            Text("Bomb ${state.charges.bomb}")
        }
        Button(onClick = { onPowerUp(PowerUpType.ColorChanger) }, modifier = Modifier.weight(1f)) {
            Text("Color ${state.charges.colorChanger}")
        }
        Button(onClick = { onPowerUp(PowerUpType.RowClear) }, modifier = Modifier.weight(1f)) {
            Text("Row ${state.charges.rowColumnClear}")
        }
        Button(onClick = { onPowerUp(PowerUpType.ColumnClear) }, modifier = Modifier.weight(1f)) {
            Text("Col ${state.charges.rowColumnClear}")
        }
    }
}

private fun BallColor.toComposeColor(): Color = when (this) {
    BallColor.Red -> Color(0xFFE84A5F)
    BallColor.Green -> Color(0xFF35C2A1)
    BallColor.Blue -> Color(0xFF5D8CFF)
    BallColor.Yellow -> Color(0xFFFFCE47)
    BallColor.Purple -> Color(0xFFB86CFF)
    BallColor.Cyan -> Color(0xFF45D8FF)
}
```

- [ ] **Step 3: Create SettingsScreen with Material switches**

Write `app/src/main/java/com/line98/game/ui/screens/SettingsScreen.kt`:

```kotlin
package com.line98.game.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.line98.game.data.UserSettings

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            SettingRow("Sound", settings.soundEnabled, onSoundChanged)
            SettingRow("Haptics", settings.hapticsEnabled, onHapticsChanged)
            Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChanged)
    }
}
```

- [ ] **Step 4: Run compile**

Run: `./gradlew test`

Expected: PASS with the screen imports resolved and no Kotlin compile errors.

- [ ] **Step 5: Commit ViewModel and UI**

Run:

```bash
git add app/src/main/java/com/line98/game/data app/src/main/java/com/line98/game/ui
git commit -m "feat: add Material 3 game UI"
```

## Task 7: Verification And Android Smoke Run

**Files:**
- Modify only files needed to resolve verification failures.

- [ ] **Step 1: Run unit tests**

Run: `./gradlew test`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Build debug APK**

Run: `./gradlew assembleDebug`

Expected: `BUILD SUCCESSFUL` and an APK under `app/build/outputs/apk/debug/`.

- [ ] **Step 3: Run on emulator if available**

Run: `adb devices`

Expected: at least one emulator or device listed. If none is listed, skip this step and report that emulator verification was not available.

If a device is listed, run:

```bash
./gradlew installDebug
adb shell monkey -p com.line98.game 1
```

Expected: app launches to the Line 98 main menu.

- [ ] **Step 4: Manual UI pass**

Check:

- Main menu shows Classic, Power-up, Settings.
- Classic starts without power-up controls.
- Power-up starts with Bomb, Color, Row, and Col controls.
- Settings toggles are visible and touchable.
- Board fits on screen and remains square.
- Text is readable in the Clean Arcade dark theme.

- [ ] **Step 5: Commit verification fixes**

If any fixes were needed, run:

```bash
git add app
git commit -m "fix: complete Android verification"
```

If no fixes were needed, do not create an empty commit.

## Self-Review

- Spec coverage: Native Kotlin Android app, Classic mode, Power-up mode, Material 3 UI practices, local persistence, engine tests, and emulator verification all have tasks.
- Placeholder scan: No placeholder markers or omitted implementation steps remain.
- Type consistency: `GameState`, `Board`, `GameEngine`, `PowerUpEngine`, `GameViewModel`, and screen function names are consistent across tasks.
