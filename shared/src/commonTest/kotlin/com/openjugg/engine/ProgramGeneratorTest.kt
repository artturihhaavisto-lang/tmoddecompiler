package com.openjugg.engine

import com.openjugg.data.seed.ExerciseSeedData
import com.openjugg.domain.engine.ProgramGenerator
import com.openjugg.domain.model.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProgramGeneratorTest {

    private val generator = ProgramGenerator(ExerciseSeedData.getAll())

    private fun testUser(
        days: Int = 4,
        level: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
        goal: TrainingGoal = TrainingGoal.POWERLIFTING
    ) = UserProfile(
        id = 1, name = "Test", age = 28, gender = Gender.MALE,
        bodyweightKg = 90f, heightCm = 180f,
        experienceLevel = level,
        trainingGoal = goal,
        daysPerWeek = days,
        squatMax = 180f, benchMax = 120f, deadliftMax = 220f,
        squatWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE),
        benchWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH),
        deadliftWeakPoints = listOf(WeakPoint.OFF_THE_FLOOR)
    )

    @Test
    fun `generates a program with phases`() {
        val program = generator.generate(testUser())
        assertTrue(program.phases.isNotEmpty())
    }

    @Test
    fun `program has correct number of sessions per week`() {
        val program = generator.generate(testUser(days = 4))
        val week = program.phases.first().weeks.first()
        assertTrue(week.sessions.size == 4)
    }

    @Test
    fun `all sessions have exercises`() {
        val program = generator.generate(testUser())
        program.phases.forEach { phase ->
            phase.weeks.forEach { week ->
                week.sessions.forEach { session ->
                    assertTrue(session.exercises.isNotEmpty(),
                        "Session on day ${session.dayOfWeek} in ${phase.type} has no exercises")
                }
            }
        }
    }

    @Test
    fun `all exercises have positive suggested weight`() {
        val program = generator.generate(testUser())
        program.phases.forEach { phase ->
            phase.weeks.forEach { week ->
                week.sessions.forEach { session ->
                    session.exercises.forEach { ex ->
                        assertTrue(ex.suggestedWeightKg > 0f,
                            "Exercise ${ex.exerciseName} has 0 or negative weight")
                    }
                }
            }
        }
    }

    @Test
    fun `3 day split generates 3 sessions per week`() {
        val program = generator.generate(testUser(days = 3))
        val week = program.phases.first().weeks.first()
        assertTrue(week.sessions.size == 3)
    }
}
