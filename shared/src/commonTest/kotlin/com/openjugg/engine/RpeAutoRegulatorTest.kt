package com.openjugg.engine

import com.openjugg.domain.engine.RpeAutoRegulator
import com.openjugg.domain.model.LoggedSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class RpeAutoRegulatorTest {

    private fun set(rpe: Float, weight: Float = 100f, reps: Int = 5) = LoggedSet(
        id = 0, programmedExerciseId = 1,
        setNumber = 1, weightKg = weight, reps = reps, rpe = rpe
    )

    @Test
    fun `RPE 10 drops all remaining sets`() {
        val decision = RpeAutoRegulator.decideAfterSet(set(10f), 8f, 100f, 2)
        assertTrue(decision.dropRemainingSet)
    }

    @Test
    fun `on target RPE keeps weight unchanged`() {
        val decision = RpeAutoRegulator.decideAfterSet(set(8f), 8f, 100f, 2)
        assertEquals(100f, decision.adjustedWeightKg, absoluteTolerance = 0.01f)
        assertFalse(decision.dropRemainingSet)
    }

    @Test
    fun `too easy RPE increases weight`() {
        val decision = RpeAutoRegulator.decideAfterSet(set(6.5f), 8f, 100f, 2)
        assertTrue(decision.adjustedWeightKg > 100f)
    }

    @Test
    fun `too hard RPE decreases weight`() {
        val decision = RpeAutoRegulator.decideAfterSet(set(9.5f), 8f, 100f, 2)
        assertTrue(decision.adjustedWeightKg < 100f)
    }
}
