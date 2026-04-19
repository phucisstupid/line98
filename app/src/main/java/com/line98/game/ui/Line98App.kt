package com.line98.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.line98.game.core.GameMode
import com.line98.game.ui.screens.GameScreen
import com.line98.game.ui.screens.MenuScreen
import com.line98.game.ui.screens.SettingsScreen

@Composable
fun Line98App(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.screen) {
        Screen.Menu -> MenuScreen(
            settings = state.settings,
            onStartClassic = { viewModel.startGame(GameMode.Classic) },
            onStartPowerUp = { viewModel.startGame(GameMode.PowerUp) },
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
