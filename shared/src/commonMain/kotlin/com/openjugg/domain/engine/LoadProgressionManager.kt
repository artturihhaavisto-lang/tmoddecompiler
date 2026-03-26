package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Manages progressive overload across weeks within a phase.
 *
 * Each phase type has a different intensity and rep scheme progression:
 *
 *   HYPERTROPHY:
 *     Reps:      8-12
 *     Intensity: 60-72% 1RM
 *     RPE:       6-8
 *     Progression: Volume increases (more sets), intensity stays moderate
 *
 *   STRENGTH:
 *     Reps:      3-6
 *     Intensity: 75-85% 1RM
 *     RPE:       7-9
 *     Progression: Intensity increases, volume decreases slightly
 *
 *   PEAKING:
 *     Reps:      1-3
 *     Intensity: 85-95%+ 1RM
 *     RPE:       8-9.5
 *     Progression: Intensity increases to near-max, volume drops significantly
 *
 *   DELOAD:
 *     Reps:      5-8
 *     Intensity: 50-60% 1RM
 *     RPE:       5-6
 *     Flat (no progression — recovery week)
 */
object LoadProgressionManager {

    /**
     * Parameters for a specific exercise in a specific week.
     */
    data class WeeklyPrescription(
        val targetReps: Int,
        val targetRpe: Float,
        val intensityPercentage: Float,   // of 1RM
        val targetSets: Int
    )

    /**
     * Calculate the prescription for a given exercise on a given week.
     *
     * @param phaseType         Current phase
     * @param weekInPhase       1-indexed week number within the phase
     * @param totalWeeksInPhase Total weeks in this phase
     * @param isPrimaryLift     Is this the competition lift (vs. variation/accessory)?
     * @param totalWeekSets     Total sets allocated for this lift this week (from VolumeCalculator)
     * @param exercisesInSession Number of exercises in this session for this lift type
     */
    fun prescribe(
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        isPrimaryLift: Boolean,
        totalWeekSets: Int,
        exercisesInSession: Int
    ): WeeklyPrescription {
        // Progress ratio: 0.0 at start → 1.0 at end of phase
        val progress = (weekInPhase - 1).toFloat() / maxOf(1, totalWeeksInPhase - 1).toFloat()

        return when (phaseType) {
            PhaseType.HYPERTROPHY -> hypertrophyPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.STRENGTH    -> strengthPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.PEAKING     -> peakingPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.DELOAD      -> deloadPrescription(isPrimaryLift, totalWeekSets, exercisesInSession)
        }
    }

    // ─── HYPERTROPHY ─────────────────────────────────────

    private fun hypertrophyPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            // Primary: 10 → 8 over the phase (slight decrease as volume ramps)
            lerp(10f, 8f, progress).toInt()
        } else {
            // Accessories: stay 10-12
            lerp(12f, 10f, progress).toInt()
        }

        val rpe = if (isPrimary) {
            lerp(6.5f, 8.0f, progress)  // Gradually harder
        } else {
            lerp(6.0f, 7.5f, progress)
        }

        // Intensity derived from RPE chart
        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)

        // Distribute total sets across exercises
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets
        )
    }

    // ─── STRENGTH ────────────────────────────────────────

    private fun strengthPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            lerp(6f, 3f, progress).toInt()   // 6 → 3 over the phase
        } else {
            lerp(8f, 6f, progress).toInt()   // Accessories: 8 → 6
        }

        val rpe = if (isPrimary) {
            lerp(7.0f, 9.0f, progress)
        } else {
            lerp(7.0f, 8.0f, progress)
        }

        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets
        )
    }

    // ─── PEAKING ─────────────────────────────────────────

    private fun peakingPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            lerp(3f, 1f, progress).toInt().coerceAtLeast(1)   // 3 → 1
        } else {
            lerp(5f, 3f, progress).toInt()
        }

        val rpe = if (isPrimary) {
            lerp(8.0f, 9.5f, progress)
        } else {
            lerp(7.0f, 8.0f, progress)
        }

        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets.coerceAtMost(if (isPrimary) 5 else 3)
        )
    }

    // ─── DELOAD ──────────────────────────────────────────

    private fun deloadPrescription(
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        return WeeklyPrescription(
            targetReps = if (isPrimary) 5 else 8,
            targetRpe = if (isPrimary) 6.0f else 5.0f,
            intensityPercentage = if (isPrimary) 0.60f else 0.50f,
            targetSets = distributeSets(totalSets, exerciseCount, isPrimary).coerceAtMost(3)
        )
    }

    // ─── Utility ─────────────────────────────────────────

    /** Linear interpolation */
    private fun lerp(start: Float, end: Float, t: Float): Float =
        start + (end - start) * t.coerceIn(0f, 1f)

    /** Round RPE to nearest 0.5 */
    private fun roundRpe(rpe: Float): Float =
        (Math.round(rpe * 2) / 2.0f).coerceIn(5.0f, 10.0f)

    /**
     * Distribute total weekly sets across exercises in a session.
     * Primary lift gets ~50% of sets, remaining split among accessories.
     */
    private fun distributeSets(totalSets: Int, exerciseCount: Int, isPrimary: Boolean): Int {
        if (exerciseCount <= 1) return totalSets
        return if (isPrimary) {
            (totalSets * 0.50f).toInt().coerceAtLeast(3)
        } else {
            val remaining = totalSets - (totalSets * 0.50f).toInt()
            val accessoryCount = (exerciseCount - 1).coerceAtLeast(1)
            (remaining / accessoryCount).coerceAtLeast(2)
        }
    }
}