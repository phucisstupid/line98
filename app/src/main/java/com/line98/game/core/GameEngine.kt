package com.line98.game.core

class GameEngine(
    private val random: RandomSource = KotlinRandomSource(),
) {
    fun newGame(mode: GameMode, highScore: Int = 0): GameState {
        val previewBalls = generateNextBalls()
        val initialSpawn = generateNextBalls()
        val spawned = spawnBalls(Board.empty(), initialSpawn)

        return GameState.initial(mode).copy(
            board = spawned.board,
            highScore = highScore,
            nextBalls = previewBalls,
        )
    }

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
        var charges = state.charges

        if (cleared.isNotEmpty()) {
            board = clearPositions(board, cleared)
            score += cleared.size * 2
            charges = charges.awardForClear(state.mode, cleared.size)
        } else {
            val spawned = spawnBalls(board, state.nextBalls)
            board = spawned.board
            val spawnedCleared = spawned.positions.flatMap { findLines(board, it) }.toSet()
            if (spawnedCleared.isNotEmpty()) {
                board = clearPositions(board, spawnedCleared)
                score += spawnedCleared.size * 2
                charges = charges.awardForClear(state.mode, spawnedCleared.size)
            }
        }

        return state.copy(
            board = board,
            score = score,
            highScore = maxOf(state.highScore, score),
            charges = charges,
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

    private data class SpawnResult(
        val board: Board,
        val positions: List<Position>,
    )

    private fun spawnBalls(board: Board, colors: List<BallColor>): SpawnResult {
        var current = board
        val positions = mutableListOf<Position>()
        for (color in colors) {
            val emptyPositions = current.emptyPositions()
            if (emptyPositions.isEmpty()) return SpawnResult(current, positions)

            val preferredIndex = nextRandomInt(
                bound = Position.Size * Position.Size,
                context = "spawn position",
            )
            val preferred = Position(preferredIndex / Position.Size, preferredIndex % Position.Size)
            val chosen = if (current[preferred].isEmpty) {
                preferred
            } else {
                emptyPositions[nextRandomInt(emptyPositions.size, "spawn fallback")]
            }

            current = current.set(chosen, Cell.Occupied(color))
            positions.add(chosen)
        }

        return SpawnResult(current, positions)
    }

    private fun generateNextBalls(): List<BallColor> =
        List(3) {
            BallColor.entries[nextRandomInt(BallColor.entries.size, "ball color")]
        }

    private fun clearPositions(board: Board, positions: Set<Position>): Board =
        positions.fold(board) { current, position -> current.clear(position) }

    private fun PowerUpCharges.awardForClear(mode: GameMode, clearedCount: Int): PowerUpCharges {
        if (mode != GameMode.PowerUp || clearedCount <= 0) return this

        var updated = copy(bomb = bomb + 1)
        if (clearedCount >= 6) {
            updated = updated.copy(colorChanger = updated.colorChanger + 1)
        }
        if (clearedCount >= 7) {
            updated = updated.copy(rowColumnClear = updated.rowColumnClear + 1)
        }
        return updated
    }

    private fun nextRandomInt(bound: Int, context: String): Int {
        require(bound > 0) { "$context bound must be positive, was $bound" }
        val value = random.nextInt(bound)
        require(value in 0 until bound) {
            "$context returned invalid value $value for bound $bound"
        }
        return value
    }

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
