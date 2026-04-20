package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test
    fun positionConstructorRejectsOutOfBoundsValues() {
        assertThrows(IllegalArgumentException::class.java) {
            Position(-1, 0)
        }
    }

    @Test
    fun powerUpChargesRejectNegativeCounts() {
        assertThrows(IllegalArgumentException::class.java) {
            PowerUpCharges(bomb = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PowerUpCharges(colorChanger = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PowerUpCharges(rowColumnClear = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PowerUpCharges().copy(bomb = -1)
        }
    }

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

    @Test
    fun clearingFiveInLineKeepsExistingPreviewWhenNoSpawnOccurs() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val baseBoard = Board.empty()
            .set(Position(0, 0), Cell.Occupied(BallColor.Red))
            .set(Position(0, 1), Cell.Occupied(BallColor.Red))
            .set(Position(0, 2), Cell.Occupied(BallColor.Red))
            .set(Position(0, 3), Cell.Occupied(BallColor.Red))
            .set(Position(2, 0), Cell.Occupied(BallColor.Red))
        val preview = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow)
        val state = GameState.initial(GameMode.Classic).copy(
            board = baseBoard,
            nextBalls = preview,
        )

        val result = engine.move(state, Position(2, 0), Position(0, 4))

        assertEquals(preview, result.nextBalls)
    }

    @Test
    fun powerUpModeAwardsBombChargeForAnyClear() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = Board.empty()
                .set(Position(0, 0), Cell.Occupied(BallColor.Red))
                .set(Position(0, 1), Cell.Occupied(BallColor.Red))
                .set(Position(0, 2), Cell.Occupied(BallColor.Red))
                .set(Position(0, 3), Cell.Occupied(BallColor.Red))
                .set(Position(2, 0), Cell.Occupied(BallColor.Red)),
            nextBalls = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow),
            charges = PowerUpCharges(),
        )

        val result = engine.move(state, Position(2, 0), Position(0, 4))

        assertEquals(PowerUpCharges(bomb = 1), result.charges)
    }

    @Test
    fun powerUpModeAwardsHigherChargesForLargeClear() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val state = GameState.initial(GameMode.PowerUp).copy(
            board = Board.empty()
                .set(Position(0, 0), Cell.Occupied(BallColor.Red))
                .set(Position(0, 1), Cell.Occupied(BallColor.Red))
                .set(Position(0, 2), Cell.Occupied(BallColor.Red))
                .set(Position(0, 3), Cell.Occupied(BallColor.Red))
                .set(Position(0, 4), Cell.Occupied(BallColor.Red))
                .set(Position(0, 5), Cell.Occupied(BallColor.Red))
                .set(Position(2, 0), Cell.Occupied(BallColor.Red)),
            nextBalls = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow),
            charges = PowerUpCharges(),
        )

        val result = engine.move(state, Position(2, 0), Position(0, 6))

        assertEquals(
            PowerUpCharges(bomb = 1, colorChanger = 1, rowColumnClear = 1),
            result.charges,
        )
    }

    @Test
    fun classicModeDoesNotAwardChargesForClear() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(8, 8), Position(8, 7), Position(8, 6))))
        val state = GameState.initial(GameMode.Classic).copy(
            board = Board.empty()
                .set(Position(0, 0), Cell.Occupied(BallColor.Red))
                .set(Position(0, 1), Cell.Occupied(BallColor.Red))
                .set(Position(0, 2), Cell.Occupied(BallColor.Red))
                .set(Position(0, 3), Cell.Occupied(BallColor.Red))
                .set(Position(2, 0), Cell.Occupied(BallColor.Red)),
            nextBalls = listOf(BallColor.Blue, BallColor.Green, BallColor.Yellow),
            charges = PowerUpCharges(bomb = 2, colorChanger = 1, rowColumnClear = 3),
        )

        val result = engine.move(state, Position(2, 0), Position(0, 4))

        assertEquals(state.charges, result.charges)
    }

    @Test
    fun spawnedLinesAreClearedAndCanPreventGameOver() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(0, 5), Position(0, 3), Position(0, 4))))
        val state = GameState.initial(GameMode.Classic).copy(
            board = Board.empty()
                .set(Position(0, 0), Cell.Occupied(BallColor.Red))
                .set(Position(0, 1), Cell.Occupied(BallColor.Red))
                .set(Position(0, 5), Cell.Occupied(BallColor.Red))
                .set(Position(1, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(1, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(2, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(3, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(4, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(5, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(6, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(7, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 0), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 1), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 2), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 3), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 4), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 5), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 6), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 7), Cell.Occupied(BallColor.Blue))
                .set(Position(8, 8), Cell.Occupied(BallColor.Blue))
                .set(Position(0, 2), Cell.Empty)
                .set(Position(0, 3), Cell.Empty)
                .set(Position(0, 4), Cell.Empty),
            nextBalls = listOf(BallColor.Red, BallColor.Red, BallColor.Red),
        )

        val result = engine.move(state, Position(0, 5), Position(0, 2))

        assertTrue(result.board[Position(0, 0)].isEmpty)
        assertTrue(result.board[Position(0, 1)].isEmpty)
        assertTrue(result.board[Position(0, 2)].isEmpty)
        assertTrue(result.board[Position(0, 3)].isEmpty)
        assertTrue(result.board[Position(0, 4)].isEmpty)
        assertFalse(result.isGameOver)
    }

    @Test
    fun newGameSpawnsInitialBallsAndKeepsPreview() {
        val engine = GameEngine(FixedRandomSource(listOf(Position(0, 0), Position(1, 1), Position(2, 2), Position(3, 3), Position(4, 4), Position(5, 5))))

        val result = engine.newGame(GameMode.PowerUp, highScore = 17)

        assertEquals(GameMode.PowerUp, result.mode)
        assertEquals(17, result.highScore)
        assertEquals(3, result.nextBalls.size)
        assertEquals(3, result.board.occupiedPositions().size)
        assertFalse(result.board.isFull())
        assertFalse(result.board[Position(0, 0)].isEmpty)
    }
}

private class FixedRandomSource(
    private val positions: List<Position> = emptyList(),
) : RandomSource {
    private var positionIndex = 0
    private var valueIndex = 0

    override fun nextInt(bound: Int): Int {
        if (positions.isNotEmpty() && bound == Position.Size * Position.Size) {
            val position = positions[positionIndex++ % positions.size]
            return position.row * Position.Size + position.col
        }
        return valueIndex++ % bound.coerceAtLeast(1)
    }
}
