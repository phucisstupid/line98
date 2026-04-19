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
}
