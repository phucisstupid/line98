package com.line98.game.core

class GameEngine(
    private val random: RandomSource = KotlinRandomSource(),
) {
    fun hasPath(board: Board, from: Position, to: Position): Boolean {
        if (from == to || board[from].isEmpty || !board[to].isEmpty) return false

        val visited = mutableSetOf(from)
        val queue = ArrayDeque<Position>()
        queue.add(from)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (next in current.neighbors()) {
                if (next == to) return true
                if (next !in visited && board[next].isEmpty) {
                    visited.add(next)
                    queue.add(next)
                }
            }
        }

        return false
    }

    fun move(state: GameState, from: Position, to: Position): GameState {
        if (state.isGameOver) return state
        if (from == to) return state.copy(message = "Select a different cell")
        if (state.board[from].isEmpty) return state.copy(message = "Select a ball")
        if (!state.board[to].isEmpty) return state.copy(message = "Target occupied")
        if (!hasPath(state.board, from, to)) return state.copy(message = "Blocked path")

        val movingCell = state.board[from]
        var board = state.board.clear(from).set(to, movingCell)
        val cleared = findLines(board, to)
        var score = state.score

        if (cleared.isNotEmpty()) {
            board = clearPositions(board, cleared)
            score += cleared.size * 2
        } else {
            board = spawnBalls(board, state.nextBalls)
        }

        return state.copy(
            board = board,
            score = score,
            highScore = maxOf(state.highScore, score),
            nextBalls = generateNextBalls(),
            selected = null,
            isGameOver = board.isFull(),
            message = null,
        )
    }

    fun findLines(board: Board, origin: Position): Set<Position> {
        val cell = board[origin] as? Cell.Occupied ?: return emptySet()
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        val result = mutableSetOf<Position>()

        for ((dr, dc) in directions) {
            val line = mutableSetOf(origin)
            line.addAll(walk(board, origin, cell.color, dr, dc))
            line.addAll(walk(board, origin, cell.color, -dr, -dc))
            if (line.size >= 5) {
                result.addAll(line)
            }
        }

        return result
    }

    fun spawnBalls(board: Board, colors: List<BallColor>): Board {
        var current = board
        for (color in colors) {
            val emptyPositions = current.emptyPositions()
            if (emptyPositions.isEmpty()) return current

            val preferredIndex = random.nextInt(Position.Size * Position.Size)
            val preferred = Position(preferredIndex / Position.Size, preferredIndex % Position.Size)
            val chosen = if (current[preferred].isEmpty) {
                preferred
            } else {
                emptyPositions[random.nextInt(emptyPositions.size)]
            }

            current = current.set(chosen, Cell.Occupied(color))
        }

        return current
    }

    private fun generateNextBalls(): List<BallColor> =
        List(3) { BallColor.entries[random.nextInt(BallColor.entries.size)] }

    private fun clearPositions(board: Board, positions: Set<Position>): Board =
        positions.fold(board) { current, position -> current.clear(position) }

    private fun walk(
        board: Board,
        origin: Position,
        color: BallColor,
        rowDelta: Int,
        colDelta: Int,
    ): Set<Position> {
        val result = mutableSetOf<Position>()
        var row = origin.row + rowDelta
        var col = origin.col + colDelta

        while (Position.isValid(row, col)) {
            val position = Position(row, col)
            if (board[position] != Cell.Occupied(color)) break
            result.add(position)
            row += rowDelta
            col += colDelta
        }

        return result
    }
}
