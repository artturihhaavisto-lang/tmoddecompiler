package com.openjugg.engine

import com.openjugg.domain.engine.OneRepMaxEstimator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OneRepMaxEstimatorTest {

    @Test
    fun `epley formula returns weight for single rep`() {
        val result = OneRepMaxEstimator.epley(100f, 1)
        assertEquals(100f, result)
    }

    @Test
    fun `epley formula 5 reps at 80kg`() {
        val result = OneRepMaxEstimator.epley(80f, 5)
        assertTrue(result in 93f..94f)
    }

    @Test
    fun `estimate1RMFromRPE accounts for RIR`() {
        val direct    = OneRepMaxEstimator.estimate1RM(100f, 7)
        val fromRPE   = OneRepMaxEstimator.estimate1RMFromRPE(100f, 5, 8f)
        assertEquals(direct, fromRPE, absoluteTolerance = 1f)
    }

    @Test
    fun `weightForRepsAndRPE rounds to nearest 2_5`() {
        val result = OneRepMaxEstimator.weightForRepsAndRPE(200f, 5, 8f)
        assertEquals(0f, result % 2.5f, absoluteTolerance = 0.01f)
    }

    @Test
    fun `percentageFor returns correct value from chart`() {
        val pct = OneRepMaxEstimator.percentageFor(1, 10f)
        assertEquals(1.00f, pct, absoluteTolerance = 0.001f)
    }

    @Test
    fun `roundToNearest rounds correctly`() {
        assertEquals(100f, OneRepMaxEstimator.roundToNearest(99f, 2.5f))
        assertEquals(100f, OneRepMaxEstimator.roundToNearest(101f, 2.5f))
        assertEquals(102.5f, OneRepMaxEstimator.roundToNearest(102f, 2.5f))
    }
}
