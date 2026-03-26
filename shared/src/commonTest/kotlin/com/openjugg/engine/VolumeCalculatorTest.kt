package com.openjugg.engine

import com.openjugg.domain.engine.VolumeCalculator
import com.openjugg.domain.model.*
import kotlin.test.Test
import kotlin.test.assertTrue

class VolumeCalculatorTest {

    private fun testUser(
        level: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
        age: Int = 28,
        gender: Gender = Gender.MALE
    ) = UserProfile(
        name = "Test", age = age, gender = gender,
        bodyweightKg = 85f, heightCm = 178f,
        experienceLevel = level,
        trainingGoal = TrainingGoal.POWERLIFTING,
        daysPerWeek = 4,
        squatMax = 180f, benchMax = 120f, deadliftMax = 220f
    )

    @Test
    fun `MEV is always less than MRV`() {
        LiftType.values().forEach { lift ->
            val result = VolumeCalculator.calculate(testUser(), lift)
            assertTrue(result.mev < result.mrv, "MEV must be < MRV for $lift")
        }
    }

    @Test
    fun `elite lifter has higher volume than beginner`() {
        val elite    = VolumeCalculator.calculate(testUser(ExperienceLevel.ELITE), LiftType.SQUAT)
        val beginner = VolumeCalculator.calculate(testUser(ExperienceLevel.BEGINNER), LiftType.SQUAT)
        assertTrue(elite.mrv > beginner.mrv)
    }

    @Test
    fun `deadlift MRV is lower than squat MRV`() {
        val squat    = VolumeCalculator.calculate(testUser(), LiftType.SQUAT)
        val deadlift = VolumeCalculator.calculate(testUser(), LiftType.DEADLIFT)
        assertTrue(deadlift.mrv < squat.mrv)
    }
}
