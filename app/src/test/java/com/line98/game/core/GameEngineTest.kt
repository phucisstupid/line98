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
