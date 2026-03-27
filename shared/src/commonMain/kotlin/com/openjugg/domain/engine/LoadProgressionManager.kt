package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Implements the Juggernaut Method wave prescriptions.
 *
 * Each wave has 4 weeks:
 *   Week 1 - Accumulation:   High sets, target reps, no AMRAP
 *   Week 2 - Intensification: Fewer sets + AMRAP, higher %
 *   Week 3 - Realization:    Fewest sets + AMRAP, highest %
 *   Week 4 - Deload:         Light work, recovery
 *
 * Wave percentages:
 *   10s (HYPERTROPHY):   55% / 60% / 65% / 50% (deload)
 *   8s  (STRENGTH):      65% / 70% / 75% / 55%
 *   5s  (PEAKING):       75% / 80% / 82.5% / 60%
 *   3s  (COMPETITION):   85% / 90% / 92.5% / 65%
 *
 * AMRAP goals (minimum reps on AMRAP set to progress):
 *   10s: ≥14 reps   8s: ≥12 reps   5s: ≥8 reps   3s: ≥5 reps
 */
object LoadProgressionManager {

    data class WeeklyPrescription(
        val targetReps: Int,
        val targetRpe: Float,
        val intensityPercentage: Float,
        val targetSets: Int,            // total sets including AMRAP set if applicable
        val isAmrap: Boolean = false,   // last set is AMRAP
        val amrapGoal: Int = 0          // minimum reps on AMRAP to progress
    )

    /**
     * Prescribe for the main competition lift.
     * weekInPhase: 1=Accumulation, 2=Intensification, 3=Realization, 4=Deload
     */
    fun prescribe(
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int = 4,     // kept for API compatibility, always 4 in Juggernaut
        isPrimaryLift: Boolean,
        totalWeekSets: Int = 0,         // kept for API compatibility, ignored
        exercisesInSession: Int = 1     // kept for API compatibility, ignored for primary
    ): WeeklyPrescription {
        val week = weekInPhase.coerceIn(1, 4)
        return if (week == 4) deloadPrescription(phaseType, isPrimaryLift)
        else activePrescription(phaseType, week, isPrimaryLift)
    }

    private fun activePrescription(
        phase: PhaseType,
        week: Int,  // 1, 2, or 3
        isPrimary: Boolean
    ): WeeklyPrescription {
        return when (phase) {
            PhaseType.HYPERTROPHY -> tensWave(week, isPrimary)
            PhaseType.STRENGTH    -> eightsWave(week, isPrimary)
            PhaseType.PEAKING     -> fivesWave(week, isPrimary)
            PhaseType.COMPETITION -> threesWave(week, isPrimary)
            PhaseType.DELOAD      -> deloadPrescription(phase, isPrimary)
        }
    }

    // ── 10s Wave ─────────────────────────────────────────

    private fun tensWave(week: Int, isPrimary: Boolean): WeeklyPrescription {
        if (!isPrimary) return accessoryPrescription(week, highRep = true)
        return when (week) {
            1 -> WeeklyPrescription(targetReps = 10, targetRpe = 6.5f, intensityPercentage = 0.55f, targetSets = 5,  isAmrap = false, amrapGoal = 0)
            2 -> WeeklyPrescription(targetReps = 10, targetRpe = 7.5f, intensityPercentage = 0.60f, targetSets = 4,  isAmrap = true,  amrapGoal = 14)
            3 -> WeeklyPrescription(targetReps = 10, targetRpe = 8.5f, intensityPercentage = 0.65f, targetSets = 3,  isAmrap = true,  amrapGoal = 14)
            else -> deloadPrescription(PhaseType.HYPERTROPHY, true)
        }
    }

    // ── 8s Wave ──────────────────────────────────────────

    private fun eightsWave(week: Int, isPrimary: Boolean): WeeklyPrescription {
        if (!isPrimary) return accessoryPrescription(week, highRep = true)
        return when (week) {
            1 -> WeeklyPrescription(targetReps = 8, targetRpe = 7.0f, intensityPercentage = 0.65f, targetSets = 5, isAmrap = false, amrapGoal = 0)
            2 -> WeeklyPrescription(targetReps = 8, targetRpe = 8.0f, intensityPercentage = 0.70f, targetSets = 4, isAmrap = true,  amrapGoal = 12)
            3 -> WeeklyPrescription(targetReps = 8, targetRpe = 8.5f, intensityPercentage = 0.75f, targetSets = 3, isAmrap = true,  amrapGoal = 12)
            else -> deloadPrescription(PhaseType.STRENGTH, true)
        }
    }

    // ── 5s Wave ──────────────────────────────────────────

    private fun fivesWave(week: Int, isPrimary: Boolean): WeeklyPrescription {
        if (!isPrimary) return accessoryPrescription(week, highRep = false)
        return when (week) {
            1 -> WeeklyPrescription(targetReps = 5, targetRpe = 7.5f, intensityPercentage = 0.75f,  targetSets = 5, isAmrap = false, amrapGoal = 0)
            2 -> WeeklyPrescription(targetReps = 5, targetRpe = 8.5f, intensityPercentage = 0.80f,  targetSets = 4, isAmrap = true,  amrapGoal = 8)
            3 -> WeeklyPrescription(targetReps = 5, targetRpe = 9.0f, intensityPercentage = 0.825f, targetSets = 3, isAmrap = true,  amrapGoal = 8)
            else -> deloadPrescription(PhaseType.PEAKING, true)
        }
    }

    // ── 3s Wave ──────────────────────────────────────────

    private fun threesWave(week: Int, isPrimary: Boolean): WeeklyPrescription {
        if (!isPrimary) return accessoryPrescription(week, highRep = false)
        return when (week) {
            1 -> WeeklyPrescription(targetReps = 3, targetRpe = 8.0f, intensityPercentage = 0.85f,  targetSets = 5, isAmrap = false, amrapGoal = 0)
            2 -> WeeklyPrescription(targetReps = 3, targetRpe = 9.0f, intensityPercentage = 0.90f,  targetSets = 4, isAmrap = true,  amrapGoal = 5)
            3 -> WeeklyPrescription(targetReps = 3, targetRpe = 9.5f, intensityPercentage = 0.925f, targetSets = 3, isAmrap = true,  amrapGoal = 5)
            else -> deloadPrescription(PhaseType.COMPETITION, true)
        }
    }

    // ── Deload ───────────────────────────────────────────

    private fun deloadPrescription(phase: PhaseType, isPrimary: Boolean): WeeklyPrescription {
        if (!isPrimary) return WeeklyPrescription(targetReps = 10, targetRpe = 5.0f, intensityPercentage = 0.40f, targetSets = 2)
        val pct = when (phase) {
            PhaseType.HYPERTROPHY -> 0.50f
            PhaseType.STRENGTH    -> 0.55f
            PhaseType.PEAKING     -> 0.60f
            PhaseType.COMPETITION -> 0.65f
            PhaseType.DELOAD      -> 0.50f
        }
        return WeeklyPrescription(targetReps = 5, targetRpe = 6.0f, intensityPercentage = pct, targetSets = 3)
    }

    // ── Accessories ──────────────────────────────────────

    private fun accessoryPrescription(week: Int, highRep: Boolean): WeeklyPrescription {
        val reps = if (highRep) 12 else 8
        val rpe  = when (week) { 1 -> 7.0f; 2 -> 7.5f; else -> 8.0f }
        val pct  = when (week) { 1 -> 0.55f; 2 -> 0.60f; else -> 0.65f }
        return WeeklyPrescription(targetReps = reps, targetRpe = rpe, intensityPercentage = pct, targetSets = 3)
    }
}
