package com.line98.game.core

import org.junit.Test
import kotlin.system.measureTimeMillis

class GameEngineBenchmark {
    @Test
    fun benchmarkFindLines() {
        val engine = GameEngine()
        val board = Board.empty()
            .set(Position(0, 0), Cell.Occupied(BallColor.Red))
            .set(Position(0, 1), Cell.Occupied(BallColor.Red))
            .set(Position(0, 2), Cell.Occupied(BallColor.Red))
            .set(Position(0, 3), Cell.Occupied(BallColor.Red))
            .set(Position(0, 4), Cell.Occupied(BallColor.Red))

        val origin = Position(0, 0)

        // Warmup
        for (i in 0..100000) {
            engine.findLines(board, origin)
        }

        // Measure
        val iterations = 5000000
        val time = measureTimeMillis {
            for (i in 0..iterations) {
                engine.findLines(board, origin)
            }
        }

        println("BENCHMARK_RESULT: $time ms for $iterations iterations")
    }
}
