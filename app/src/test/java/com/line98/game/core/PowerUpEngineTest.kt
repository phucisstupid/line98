package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerUpEngineTest {
    private val engine = PowerUpEngine()

    @Test
    fun classicModeRejectsPowerUps() {
        val state = GameState.initial(GameMode.Classic).copy(
            score = 8,
            highScore = 12,
            charges = PowerUpCharges(bomb = 1),
            board = Board.empty().set(Position(4, 4), Cell.Occupied(BallColor.Red)),
        )

        val result = engine.applyBomb(state, Position(4, 4))

        assertEquals(state.board, result.board)
        assertEquals(state.score, result.score)
        assertEquals(state.highScore, result.highScore)
        assertEquals(state.charges, result.charges)
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
            score = 2,
            highScore = 2,
            charges = PowerUpCharges(bomb = 1),
        )

        val result = engine.applyBomb(state, Position(4, 4))

        assertEquals(Cell.Empty, result.board[Position(4, 4)])
        assertEquals(Cell.Empty, result.board[Position(4, 5)])
        assertEquals(Cell.Empty, result.board[Position(5, 4)])
        assertEquals(Cell.Occupied(BallColor.Yellow), result.board[Position(6, 6)])
        assertEquals(5, result.score)
        assertEquals(5, result.highScore)
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
            score = 1,
            highScore = 1,
            charges = PowerUpCharges(rowColumnClear = 2),
        )

        val rowResult = engine.applyRowClear(state, row = 1)
        val columnResult = engine.applyColumnClear(rowResult, col = 4)

        assertEquals(Cell.Empty, rowResult.board[Position(1, 1)])
        assertEquals(Cell.Empty, rowResult.board[Position(1, 2)])
        assertEquals(3, rowResult.score)
        assertEquals(3, rowResult.highScore)
        assertEquals(1, rowResult.charges.rowColumnClear)
        assertEquals(Cell.Empty, columnResult.board[Position(3, 4)])
        assertEquals(4, columnResult.score)
        assertEquals(4, columnResult.highScore)
        assertEquals(0, columnResult.charges.rowColumnClear)
    }

    @Test
    fun zeroChargeBombRejectsWithoutMutatingState() {
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = Board.empty().set(Position(4, 4), Cell.Occupied(BallColor.Red)),
            score = 6,
            highScore = 9,
            charges = PowerUpCharges(bomb = 0),
        )

        val result = engine.applyBomb(state, Position(4, 4))

        assertEquals(state.board, result.board)
        assertEquals(state.score, result.score)
        assertEquals(state.highScore, result.highScore)
        assertEquals(state.charges, result.charges)
        assertEquals("No charges", result.message)
    }
}
