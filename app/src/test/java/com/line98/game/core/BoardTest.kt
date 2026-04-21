package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun emptyCreatesBoardWithAllEmptyCells() {
        val board = Board.empty()

        // Size of board should be Size * Size (81 for Size=9)
        assertEquals(Position.Size * Position.Size, board.positions().size)

        // All positions should be empty
        assertTrue(board.positions().all { board[it] == Cell.Empty })

        // emptyPositions should contain all positions
        assertEquals(Position.Size * Position.Size, board.emptyPositions().size)

        // occupiedPositions should be empty
        assertTrue(board.occupiedPositions().isEmpty())

        // Board should not be full
        assertFalse(board.isFull())
    }

    @Test
    fun setCreatesNewBoardWithUpdatedCell() {
        val emptyBoard = Board.empty()
        val pos = Position(4, 4)
        val cell = Cell.Occupied(BallColor.Red)

        val newBoard = emptyBoard.set(pos, cell)

        // The new board should have the cell
        assertEquals(cell, newBoard[pos])

        // The old board should remain empty (immutability)
        assertEquals(Cell.Empty, emptyBoard[pos])

        // Occupied and empty positions should reflect the change
        assertEquals(1, newBoard.occupiedPositions().size)
        assertTrue(newBoard.occupiedPositions().contains(pos))
        assertEquals(Position.Size * Position.Size - 1, newBoard.emptyPositions().size)
    }

    @Test
    fun clearCreatesNewBoardWithEmptyCell() {
        val pos = Position(2, 3)
        val cell = Cell.Occupied(BallColor.Blue)
        val board = Board.empty().set(pos, cell)

        assertEquals(cell, board[pos])

        val clearedBoard = board.clear(pos)

        // The cell at pos should be empty in the cleared board
        assertEquals(Cell.Empty, clearedBoard[pos])

        // The old board should still have the cell
        assertEquals(cell, board[pos])

        // Occupied positions should be empty again
        assertTrue(clearedBoard.occupiedPositions().isEmpty())
    }

    @Test
    fun positionsReturnsAllValidPositions() {
        val board = Board.empty()
        val positions = board.positions()

        assertEquals(Position.Size * Position.Size, positions.size)

        // Ensure that row and col iterate correctly from 0 until Size
        for (row in 0 until Position.Size) {
            for (col in 0 until Position.Size) {
                assertTrue(positions.contains(Position(row, col)))
            }
        }
    }

    @Test
    fun isFullReturnsTrueWhenNoEmptyCells() {
        var board = Board.empty()

        assertFalse(board.isFull())

        // Fill the board
        board.positions().forEach { pos ->
            board = board.set(pos, Cell.Occupied(BallColor.Green))
        }

        assertTrue(board.isFull())
        assertTrue(board.emptyPositions().isEmpty())
        assertEquals(Position.Size * Position.Size, board.occupiedPositions().size)
    }
}
