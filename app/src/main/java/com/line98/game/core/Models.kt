package com.line98.game.core

data class Position(val row: Int, val col: Int) {
    init {
        require(isValid(row, col)) { "Position out of bounds: row=$row col=$col" }
    }

    fun neighbors(): List<Position> =
        listOf(
            row - 1 to col,
            row + 1 to col,
            row to col - 1,
            row to col + 1,
        ).filter { (neighborRow, neighborCol) ->
            isValid(neighborRow, neighborCol)
        }.map { (neighborRow, neighborCol) ->
            Position(neighborRow, neighborCol)
        }

    companion object {
        const val Size = 9

        fun isValid(row: Int, col: Int): Boolean =
            row in 0 until Size && col in 0 until Size
    }
}

enum class BallColor {
    Red,
    Green,
    Blue,
    Yellow,
    Purple,
    Cyan,
}

sealed interface Cell {
    val isEmpty: Boolean

    data object Empty : Cell {
        override val isEmpty: Boolean = true
    }

    data class Occupied(val color: BallColor) : Cell {
        override val isEmpty: Boolean = false
    }
}

enum class GameMode {
    Classic,
    PowerUp,
}

enum class PowerUpType {
    Bomb,
    ColorChanger,
    RowClear,
    ColumnClear,
}

data class PowerUpCharges(
    val bomb: Int = 0,
    val colorChanger: Int = 0,
    val rowColumnClear: Int = 0,
) {
    init {
        require(bomb >= 0) { "Bomb charges cannot be negative" }
        require(colorChanger >= 0) { "Color changer charges cannot be negative" }
        require(rowColumnClear >= 0) { "Row/column clear charges cannot be negative" }
    }

    fun count(type: PowerUpType): Int =
        when (type) {
            PowerUpType.Bomb -> bomb
            PowerUpType.ColorChanger -> colorChanger
            PowerUpType.RowClear,
            PowerUpType.ColumnClear,
            -> rowColumnClear
        }
}
