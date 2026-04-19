package com.line98.game.core

import kotlin.random.Random

/**
 * Returns an index in `0 until bound`; engine code validates results so bad test doubles fail fast.
 */
fun interface RandomSource {
    fun nextInt(bound: Int): Int
}

class KotlinRandomSource(
    private val random: Random = Random.Default,
) : RandomSource {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
