package com.line98.game.core

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class KotlinRandomSourceTest {
    @Test
    fun `delegates to provided Random`() {
        var passedBound = -1
        val fakeRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextInt(until: Int): Int {
                passedBound = until
                return 42
            }
        }

        val source = KotlinRandomSource(fakeRandom)
        val result = source.nextInt(100)

        assertEquals(100, passedBound)
        assertEquals(42, result)
    }
}
