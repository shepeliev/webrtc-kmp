package com.shepeliev.webrtckmp

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstrainTest {

    @Test
    fun testMatch_exact_set() {
        val constrain = Constrain(exact = 42)

        assertTrue(constrain.matches(42))
        assertFalse(constrain.matches(84))
    }

    @Test
    fun testMatch_ideal_set() {
        val constrain = Constrain(ideal = 42)

        assertTrue(constrain.matches(42))
        assertTrue(constrain.matches(84))
    }

    @Test
    fun testMatch_exact_and_ideal_set() {
        val constrain = Constrain(exact = 42, ideal = 100)

        assertTrue(constrain.matches(42))
        assertFalse(constrain.matches(100))
    }

    @Test
    fun testMatch_nothing_set() {
        val constrain = Constrain<Int>()

        assertFalse(constrain.matches(42))
    }
}
