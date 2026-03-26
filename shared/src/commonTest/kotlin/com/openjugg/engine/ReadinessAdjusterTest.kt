package com.openjugg.engine

import com.openjugg.domain.engine.ReadinessAdjuster
import com.openjugg.domain.model.ReadinessInputs
import com.openjugg.domain.model.LiftType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadinessAdjusterTest {

    private fun inputs(
        sleep: Int = 3, nutrition: Int = 3,
        motivation: Int = 3, soreness: Int = 3
    ) = ReadinessInputs(sleep, nutrition, motivation, soreness)

    @Test
    fun `very low readiness reduces weight by 15 percent`() {
        val result = ReadinessAdjuster.assess(inputs(1, 1, 1, 5))
        assertEquals(0.85f, result.weightMultiplier, absoluteTolerance = 0.001f)
        assertTrue(result.skipVariations)
    }

    @Test
    fun `normal readiness has no adjustments`() {
        val result = ReadinessAdjuster.assess(inputs(3, 3, 3, 2))
        assertEquals(1.0f, result.weightMultiplier, absoluteTolerance = 0.001f)
        assertEquals(0, result.accessorySetDelta)
    }

    @Test
    fun `excellent readiness adds a set`() {
        val result = ReadinessAdjuster.assess(inputs(5, 5, 5, 1))
        assertEquals(1, result.accessorySetDelta)
    }
}
