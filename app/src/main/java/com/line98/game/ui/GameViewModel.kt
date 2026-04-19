package com.line98.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.line98.game.core.BallColor
import com.line98.game.core.Cell
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
    Menu,
    Game,
    Settings,
}

data class UiState(
    val screen: Screen = Screen.Menu,
    val game: GameState = GameState.initial(GameMode.Classic),
    val settings: UserSettings = UserSettings(),
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application.applicationContext)
    private val gameEngine = GameEngine()
    private val powerUpEngine = PowerUpEngine()
    private val localState = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> =
        combine(localState, preferences.settings) { local, settings ->
            local.copy(settings = settings)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    fun openMenu() {
        localState.value = localState.value.copy(screen = Screen.Menu)
    }

    fun openSettings() {
        localState.value = localState.value.copy(screen = Screen.Settings)
    }

    fun startGame(mode: GameMode) {
        val settings = uiState.value.settings
        val highScore = when (mode) {
            GameMode.Classic -> settings.classicHighScore
            GameMode.PowerUp -> settings.powerUpHighScore
        }

        viewModelScope.launch {
            preferences.setLastMode(mode)
        }

        localState.value = localState.value.copy(
            screen = Screen.Game,
            game = gameEngine.newGame(mode, highScore),
        )
    }

    fun tapCell(position: Position) {
        val game = localState.value.game
        if (game.isGameOver) return

        val activePowerUp = game.activePowerUp
        if (activePowerUp != null) {
            applyPowerUp(position, game, activePowerUp)
            return
        }

        val cell = game.board[position]
        val nextGame = when {
            game.selected == null && cell is Cell.Occupied -> game.copy(selected = position, message = null)
            game.selected == null && cell.isEmpty -> game.copy(message = "Select a ball")
            game.selected == position -> game.copy(selected = null, message = null)
            game.selected != null && cell is Cell.Occupied -> game.copy(selected = position, message = null)
            game.selected != null && cell.isEmpty -> gameEngine.move(game, game.selected, position)
            else -> game
        }

        replaceGame(nextGame)
    }

    fun activatePowerUp(type: PowerUpType) {
        val game = localState.value.game
        if (game.mode != GameMode.PowerUp || game.isGameOver) return
        if (game.charges.count(type) <= 0) return

        val nextPowerUp = if (game.activePowerUp == type) null else type
        replaceGame(
            game.copy(
                activePowerUp = nextPowerUp,
                selected = null,
                message = null,
            ),
        )
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticsEnabled(enabled)
        }
    }

    private fun applyPowerUp(
        position: Position,
        game: GameState,
        activePowerUp: PowerUpType,
    ) {
        val nextGame = when (activePowerUp) {
            PowerUpType.Bomb -> powerUpEngine.applyBomb(game, position)
            PowerUpType.ColorChanger -> {
                val cell = game.board[position]
                if (cell is Cell.Occupied) {
                    powerUpEngine.applyColorChanger(game, position, nextBallColor(cell.color))
                } else {
                    powerUpEngine.applyColorChanger(game, position, BallColor.Red)
                }
            }
            PowerUpType.RowClear -> powerUpEngine.applyRowClear(game, position.row)
            PowerUpType.ColumnClear -> powerUpEngine.applyColumnClear(game, position.col)
        }

        val changed =
            nextGame.board != game.board ||
                nextGame.score != game.score ||
                nextGame.highScore != game.highScore ||
                nextGame.charges != game.charges ||
                nextGame.isGameOver != game.isGameOver

        replaceGame(
            if (changed) {
                nextGame.copy(activePowerUp = null, selected = null)
            } else {
                nextGame
            },
        )
    }

    private fun replaceGame(nextGame: GameState) {
        val previousGame = localState.value.game
        localState.value = localState.value.copy(game = nextGame)

        if (nextGame.highScore > previousGame.highScore) {
            viewModelScope.launch {
                preferences.saveHighScore(nextGame.mode, nextGame.highScore)
            }
        }
    }

    private fun nextBallColor(color: BallColor): BallColor {
        val colors = BallColor.entries
        val currentIndex = colors.indexOf(color)
        val nextIndex = (currentIndex + 1) % colors.size
        return colors[nextIndex]
    }
}
