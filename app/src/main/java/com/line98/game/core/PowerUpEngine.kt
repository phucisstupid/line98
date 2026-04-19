package com.line98.game.core

class PowerUpEngine {
    fun applyBomb(state: GameState, center: Position): GameState =
        applyCharged(state, PowerUpType.Bomb) {
            val targets = buildArea(center)
            clearTargets(state, targets, PowerUpType.Bomb)
        }

    fun applyColorChanger(state: GameState, position: Position, color: BallColor): GameState =
        applyCharged(state, PowerUpType.ColorChanger) {
            if (state.board[position].isEmpty) {
                state.copy(message = "Select a ball")
            } else {
                state.copy(
                    board = state.board.set(position, Cell.Occupied(color)),
                    charges = state.charges.spend(PowerUpType.ColorChanger),
                    message = null,
                )
            }
        }

    fun applyRowClear(state: GameState, row: Int): GameState =
        applyCharged(state, PowerUpType.RowClear) {
            require(Position.isValid(row, 0)) { "Row out of bounds: $row" }
            clearTargets(state, List(Position.Size) { col -> Position(row, col) }, PowerUpType.RowClear)
        }

    fun applyColumnClear(state: GameState, col: Int): GameState =
        applyCharged(state, PowerUpType.ColumnClear) {
            require(Position.isValid(0, col)) { "Column out of bounds: $col" }
            clearTargets(state, List(Position.Size) { row -> Position(row, col) }, PowerUpType.ColumnClear)
        }

    private fun applyCharged(
        state: GameState,
        type: PowerUpType,
        action: () -> GameState,
    ): GameState {
        if (state.mode != GameMode.PowerUp) {
            return state.copy(message = "Power-ups are disabled in Classic mode")
        }
        if (state.charges.count(type) <= 0) {
            return state.copy(message = "No charges")
        }
        return action()
    }

    private fun clearTargets(
        state: GameState,
        targets: List<Position>,
        type: PowerUpType,
    ): GameState {
        val occupiedTargets = targets.count { !state.board[it].isEmpty }
        val board = targets.fold(state.board) { current, position -> current.clear(position) }
        val score = state.score + occupiedTargets

        return state.copy(
            board = board,
            charges = state.charges.spend(type),
            score = score,
            highScore = maxOf(state.highScore, score),
            message = null,
        )
    }

    private fun buildArea(center: Position): List<Position> =
        (-1..1).flatMap { rowOffset ->
            (-1..1).mapNotNull { colOffset ->
                val row = center.row + rowOffset
                val col = center.col + colOffset
                if (Position.isValid(row, col)) Position(row, col) else null
            }
        }
}
