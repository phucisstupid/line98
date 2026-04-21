package com.line98.game.core

@ConsistentCopyVisibility
data class Board private constructor(
    private val cells: List<Cell>,
) {
    operator fun get(position: Position): Cell =
        cells[index(position)]

    fun set(position: Position, cell: Cell): Board =
        Board(cells.toMutableList().also { it[index(position)] = cell })

    fun clear(position: Position): Board =
        set(position, Cell.Empty)

    fun positions(): List<Position> = ALL_POSITIONS

    fun emptyPositions(): List<Position> =
        positions().filter { this[it].isEmpty }

    fun occupiedPositions(): List<Position> =
        positions().filter { !this[it].isEmpty }

    fun isFull(): Boolean =
        emptyPositions().isEmpty()

    private fun index(position: Position): Int =
        position.row * Position.Size + position.col

    companion object {
        private val ALL_POSITIONS: List<Position> =
            List(Position.Size * Position.Size) { index ->
                Position(index / Position.Size, index % Position.Size)
            }

        fun empty(): Board =
            Board(List(Position.Size * Position.Size) { Cell.Empty })
    }
}
