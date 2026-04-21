package com.line98.game.ui

import com.line98.game.core.GameMode
import com.line98.game.core.PowerUpType

fun GameMode.displayName(): String =
    when (this) {
        GameMode.Classic -> "Classic"
        GameMode.PowerUp -> "Power-up"
    }

fun PowerUpType.displayName(): String =
    when (this) {
        PowerUpType.Bomb -> "Bomb"
        PowerUpType.ColorChanger -> "Color changer"
        PowerUpType.RowClear -> "Row clear"
        PowerUpType.ColumnClear -> "Column clear"
    }
