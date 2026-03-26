package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Determines the phase structure (number & length of each phase) for the program.
 *
 * Phase ordering follows classical block periodization:
 *   Hypertrophy → Strength → Peaking → Deload
 *
 * If a meet date is provided, the planner works backward from the meet to allocate phases.
 * If no meet date, it creates a general development program.
 */
object PeriodizationPlanner {

    data class PhaseTemplate(
        val type: PhaseType,
        val weeks: Int,
        val order: Int
    )

    /**
     * Plan the phase structure.
     *
     * @param user          The user profile
     * @param totalWeeks    Total available weeks (calculated from meet date, or default)
     * @return              Ordered list of phase templates
     */
    fun planPhases(user: UserProfile, totalWeeks: Int? = null): List<PhaseTemplate> {
        val available = totalWeeks ?: defaultProgramLength(user.experienceLevel)
        return allocatePhases(user.experienceLevel, user.trainingGoal, available)
    }

    /**
     * Default program length if no meet date is set.
     */
    private fun defaultProgramLength(level: ExperienceLevel): Int = when (level) {
        ExperienceLevel.BEGINNER     -> 12  // 12-week cycle
        ExperienceLevel.INTERMEDIATE -> 16  // 16-week cycle
        ExperienceLevel.ADVANCED     -> 16  // 16-week cycle
        ExperienceLevel.ELITE        -> 20  // 20-week cycle
    }

    /**
     * Allocate phases based on experience, goal, and total time.
     *
     * Ratios (approximate):
     *   Beginner:     50% Hypertrophy, 30% Strength, 10% Peak, 10% Deload
     *   Intermediate: 40% Hypertrophy, 30% Strength, 15% Peak, 15% Deload
     *   Advanced:     35% Hypertrophy, 30% Strength, 20% Peak, 15% Deload
     *   Elite:        30% Hypertrophy, 30% Strength, 25% Peak, 15% Deload
     *
     * For Powerbuilding: More hypertrophy, less peaking
     */
    private fun allocatePhases(
        level: ExperienceLevel,
        goal: TrainingGoal,
        totalWeeks: Int
    ): List<PhaseTemplate> {

        // Base ratios by experience
        var hypertrophyRatio: Float
        var strengthRatio: Float
        var peakingRatio: Float
        val deloadWeeks: Int

        when (level) {
            ExperienceLevel.BEGINNER -> {
                hypertrophyRatio = 0.50f; strengthRatio = 0.30f; peakingRatio = 0.10f
                deloadWeeks = 1
            }
            ExperienceLevel.INTERMEDIATE -> {
                hypertrophyRatio = 0.40f; strengthRatio = 0.30f; peakingRatio = 0.15f
                deloadWeeks = 1
            }
            ExperienceLevel.ADVANCED -> {
                hypertrophyRatio = 0.35f; strengthRatio = 0.30f; peakingRatio = 0.20f
                deloadWeeks = 1
            }
            ExperienceLevel.ELITE -> {
                hypertrophyRatio = 0.30f; strengthRatio = 0.30f; peakingRatio = 0.25f
                deloadWeeks = 1
            }
        }

        // Adjust for goal
        when (goal) {
            TrainingGoal.POWERBUILDING -> {
                hypertrophyRatio += 0.10f
                peakingRatio -= 0.05f
                strengthRatio -= 0.05f
            }
            TrainingGoal.POWER_COMBO -> {
                hypertrophyRatio += 0.05f
                peakingRatio -= 0.05f
            }
            TrainingGoal.POWERLIFTING -> { /* default ratios */ }
        }

        val remainingWeeks = totalWeeks - deloadWeeks
        val hypertrophyWeeks = (remainingWeeks * hypertrophyRatio).toInt().coerceAtLeast(2)
        val strengthWeeks = (remainingWeeks * strengthRatio).toInt().coerceAtLeast(2)
        val peakingWeeks = (remainingWeeks - hypertrophyWeeks - strengthWeeks).coerceAtLeast(1)

        // Build phase list with deload inserted between major blocks
        val phases = mutableListOf<PhaseTemplate>()
        var order = 0

        // Hypertrophy block
        phases.add(PhaseTemplate(PhaseType.HYPERTROPHY, hypertrophyWeeks, order++))
        // Deload after hypertrophy
        phases.add(PhaseTemplate(PhaseType.DELOAD, 1, order++))

        // Strength block
        phases.add(PhaseTemplate(PhaseType.STRENGTH, strengthWeeks, order++))

        // Peaking block (no deload between strength and peaking — intensity stays high)
        phases.add(PhaseTemplate(PhaseType.PEAKING, peakingWeeks, order++))

        // Final deload / taper before meet
        if (deloadWeeks > 0) {
            phases.add(PhaseTemplate(PhaseType.DELOAD, 1, order++))
        }

        return phases
    }

    /**
     * Calculate total weeks from now until meet date.
     */
    fun weeksUntilMeet(nowMillis: Long, meetDateMillis: Long): Int {
        val diff = meetDateMillis - nowMillis
        return (diff / (7L * 24 * 60 * 60 * 1000)).toInt().coerceAtLeast(4)
    }
}