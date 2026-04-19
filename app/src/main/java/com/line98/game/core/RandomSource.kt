package com.line98.game.core

import kotlin.random.Random

fun interface RandomSource {
    fun nextInt(bound: Int): Int
}

class KotlinRandomSource(
    private val random: Random = Random.Default,
) : RandomSource {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
