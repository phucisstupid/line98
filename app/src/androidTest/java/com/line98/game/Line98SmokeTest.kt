package com.line98.game

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.line98.game.core.BallColor
import com.line98.game.core.Board
import com.line98.game.core.Cell
import com.line98.game.core.GameMode
import com.line98.game.core.GameState
import com.line98.game.core.Position
import com.line98.game.ui.screens.GameScreen
import com.line98.game.ui.screens.MenuScreen
import com.line98.game.ui.theme.Line98Theme
import com.line98.game.data.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class Line98SmokeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun menuClassicButtonOpensGameScreen() {
        rule.setContent {
            val showGame = remember { mutableStateOf(false) }
            Line98Theme {
                if (showGame.value) {
                    GameScreen(
                        state = GameState.initial(GameMode.Classic),
                        onCellTap = {},
                        onPowerUp = {},
                        onMenu = {},
                        onRestart = {},
                    )
                } else {
                    MenuScreen(
                        settings = UserSettings(),
                        onStartClassic = { showGame.value = true },
                        onStartPowerUp = {},
                        onSettings = {},
                    )
                }
            }
        }

        rule.onNodeWithText("Classic").performClick()
        rule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun boardCellIsPresentAndClickable() {
        var tappedPosition: Position? = null
        val board = Board.empty().set(Position(0, 0), Cell.Occupied(BallColor.Red))

        rule.setContent {
            Line98Theme {
                GameScreen(
                    state = GameState.initial(GameMode.Classic).copy(board = board),
                    onCellTap = { tappedPosition = it },
                    onPowerUp = {},
                    onMenu = {},
                    onRestart = {},
                )
            }
        }

        rule.onNode(
            hasContentDescription("Row 1, column 1", substring = true),
        ).assertIsDisplayed().performClick()

        rule.runOnIdle {
            assertEquals(Position(0, 0), tappedPosition)
        }
    }
}
