# Line 98 Android Design

## Summary

Build a native Android Line 98 mobile game in Kotlin using Jetpack Compose. The app includes a pure Classic mode with unchanged Line 98 rules and a separate Power-up mode with three tools: Bomb, Color Changer, and Row/Column Clear.

The first version is local-only. It does not include accounts, ads, purchases, cloud sync, online leaderboards, or daily challenges.

## Goals

- Ship a native Android puzzle game with touch-first mobile controls.
- Preserve Classic Line 98 as a clean, no-power-up mode.
- Add a Power-up mode that extends the game without changing the core movement and clearing rules.
- Keep game rules in plain Kotlin so they can be unit tested without Android UI.
- Use a Clean Arcade visual direction: dark board-first UI, bright balls, compact HUD, and thumb-friendly controls.

## Non-Goals

- Cross-platform iOS support.
- Web, Capacitor, React Native, or Flutter implementation.
- Online services, monetization, or multiplayer.
- Complex progression systems such as levels, missions, or daily boards.
- Heavy custom game engine rendering.

## Gameplay

### Classic Mode

Classic mode follows traditional Line 98 rules:

- 9x9 board.
- Multiple colored balls.
- Player selects one occupied cell, then selects an empty target cell.
- A move is valid only when an open orthogonal path exists through empty cells.
- Moving a ball does not allow diagonal path steps.
- After a valid move, the game clears any horizontal, vertical, or diagonal line of 5 or more same-colored balls.
- If the move does not clear a line, new balls spawn onto empty cells.
- The game ends when the board has no playable empty cells for spawning or movement.
- No power-ups appear, can be earned, or can be activated in Classic mode.

### Power-up Mode

Power-up mode uses the same board, movement, pathfinding, spawning, line clearing, scoring, and game-over rules as Classic mode. It adds three player-activated power-ups:

- Bomb: clears a small area around a selected cell.
- Color Changer: changes one selected ball to a chosen or strategically useful color.
- Row/Column Clear: clears a selected full row or full column.

Power-ups are earned through play rather than spawned randomly onto the board. The first implementation should award charges from score or clear-count thresholds. Each activation spends one charge. Power-up moves should provide clear visual feedback and should not be available in Classic mode.

## UI Design

The app uses the Clean Arcade direction:

- Dark full-screen surface.
- Large centered 9x9 board sized to fit mobile screens.
- Bright colored balls with simple highlights for readability.
- Compact top HUD with mode, score, high score, reset or pause, and next-ball information.
- Next-ball preview placed close to the board.
- Classic mode has no power-up controls.
- Power-up mode adds a bottom thumb bar with Bomb, Color Changer, and Row/Column Clear buttons.
- Each power-up button shows its current charge count.
- Selection states are explicit: selected ball glow, selected power-up state, valid target hint, and blocked-move feedback.

The main menu is minimal:

- Classic
- Power-up
- Settings
- High score display

Settings include sound and haptics toggles.

## Architecture

Use a native Android Kotlin project with Jetpack Compose for UI and plain Kotlin for game rules.

Core logic:

- `Board`: owns the 9x9 grid and cell access.
- `Cell`: represents empty or occupied board positions.
- `BallColor`: enum of available ball colors.
- `Position`: row and column value object.
- `GameMode`: Classic or Power-up.
- `GameState`: snapshot of board, score, next balls, selected cell, active power-up, charges, and game-over state.
- `GameEngine`: validates moves, finds paths, clears lines, scores, spawns balls, and detects game over.
- `PowerUpEngine`: applies Bomb, Color Changer, and Row/Column Clear effects.
- `GameViewModel`: bridges Compose UI to the engine and owns screen-level state.

Compose UI:

- Main menu screen.
- Game screen.
- Settings screen.
- Reusable board, HUD, next-preview, and power-up bar composables.

The engine should not depend on Compose, Android context, navigation, or persistence APIs.

## Data Flow

1. Compose renders `GameState` from `GameViewModel`.
2. User taps a ball, empty cell, or power-up button.
3. `GameViewModel` translates the tap into an engine action.
4. `GameEngine` or `PowerUpEngine` returns a new `GameState`.
5. `GameViewModel` persists high score or settings changes when needed.
6. Compose recomposes from the updated state.

Random spawning should be injected behind a small random provider interface or seeded source so tests can control spawn behavior.

## Persistence

Use local persistence only:

- Separate Classic and Power-up high scores.
- Sound enabled flag.
- Haptics enabled flag.
- Last selected game mode.

Saving an in-progress game is optional and should not block the first version.

## Error Handling And Feedback

The game should avoid modal errors during play. Invalid actions should produce lightweight feedback:

- Tapping an unreachable target briefly marks the path as blocked or shakes the selected ball.
- Trying to use a power-up with zero charges disables or visibly rejects the button.
- Trying to activate a power-up in Classic mode is impossible because controls are not rendered and the engine rejects power-up actions for Classic mode.
- Game over shows score, high score, restart, and menu actions.

## Testing

Unit tests focus on game rules:

- Pathfinding: open path, blocked path, same-cell move, occupied target, and no diagonal path steps.
- Line clearing: horizontal, vertical, both diagonals, 5+ balls, and overlapping lines.
- Spawning and game over: spawn after non-clearing moves, no spawn after clearing moves, full-board behavior.
- Classic mode: no power-up availability or activation.
- Power-up mode: Bomb, Color Changer, Row/Column Clear, and charge spending.
- Deterministic random behavior through an injected seeded source.

Android tests:

- Compose smoke test for menu navigation.
- Compose smoke test for tapping a board cell.
- Manual emulator pass for board sizing, touch feel, blocked move feedback, and mode switching.

## Acceptance Criteria

- Android app builds and launches on an emulator.
- Player can start Classic mode and play by classic rules.
- Player can start Power-up mode and use Bomb, Color Changer, and Row/Column Clear.
- Classic mode never exposes or activates power-ups.
- High scores persist separately by mode.
- Sound and haptics settings persist locally.
- Core engine tests pass.
- UI uses the Clean Arcade direction and keeps the board readable on mobile screens.
