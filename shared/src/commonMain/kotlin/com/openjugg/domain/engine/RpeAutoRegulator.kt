package com.openjugg.domain.engine

import com.openjugg.domain.model.LoggedSet
import com.openjugg.domain.model.ProgrammedExercise

/**
 * Intra-session and session-to-session auto-regulation based on RPE feedback.
 *
 * This is the real-time adjustment system that makes every set responsive:
 *   - After logging a set, it decides what to do for the next set
 *   - After a session, it adjusts future sessions
 *   - Tracks RPE drift to detect fatigue accumulation
 */
object RpeAutoRegulator {

    /**
     * Decision after logging a single set within a workout.
     */
    data class IntraSetDecision(
        val adjustedWeightKg: Float,     // Suggested weight for the next set
        val dropRemainingSet: Boolean,   // Should we remove the next set entirely?
        val message: String
    )

    /**
     * Analyze a just-logged set and decide what to do for the NEXT set.
     *
     * Rules:
     *   1. If RPE >= 10 (failure) → drop all remaining sets for safety
     *   2. If RPE >= 9.5 and target was < 9 → drop remaining sets
     *   3. If RPE is > 1 above target → reduce weight by 5% for next set
     *   4. If RPE is > 0.5 above target → reduce weight by 2.5%
     *   5. If RPE is > 1 below target → increase weight by 2.5-5% for next set
     *   6. If RPE is on target (± 0.5) → keep weight the same
     */
    fun decideAfterSet(
        loggedSet: LoggedSet,
        targetRpe: Float,
        currentSuggestedWeight: Float,
        remainingSets: Int
    ): IntraSetDecision {
        val rpeDiff = loggedSet.rpe - targetRpe  // positive = harder than expected

        return when {
            // Hit failure or very close → STOP
            loggedSet.rpe >= 10f -> IntraSetDecision(
                adjustedWeightKg = currentSuggestedWeight,
                dropRemainingSet = true,
                message = "RPE 10 reached. Dropping remaining sets for recovery."
            )

            // Way too hard (1.5+ over target) → drop remaining
            rpeDiff >= 1.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 0.92f, 2.5f
                ),
                dropRemainingSet = remainingSets <= 1,
                message = "Significantly over target RPE. ${if (remainingSets <= 1) "Stopping." else "Reducing weight 8%."}"
            )

            // Too hard (0.5-1.5 over) → reduce weight
            rpeDiff >= 0.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * (1f - rpeDiff * 0.025f), 2.5f
                ),
                dropRemainingSet = false,
                message = "Slightly over target RPE. Reducing weight ${(rpeDiff * 2.5f).toInt()}%."
            )

            // Too easy (1+ under target) → can increase weight
            rpeDiff <= -1.0f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 1.05f, 2.5f
                ),
                dropRemainingSet = false,
                message = "Under target RPE. Consider increasing weight ~5%."
            )

            // Slightly easy (0.5-1 under) → small bump
            rpeDiff <= -0.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 1.025f, 2.5f
                ),
                dropRemainingSet = false,
                message = "Slightly under target RPE. Small weight increase suggested."
            )

            // On target
            else -> IntraSetDecision(
                adjustedWeightKg = currentSuggestedWeight,
                dropRemainingSet = false,
                message = "Right on target. Keep the weight."
            )
        }
    }

    /**
     * Post-session analysis: Determine adjustments for NEXT session
     * of the same exercise.
     *
     * @param loggedSets    All sets logged for this exercise in this session
     * @param targetRpe     The target RPE for the exercise
     * @param currentWeight The weight used (average or programmed)
     * @return              Weight adjustment multiplier for next session (e.g., 1.025 = +2.5%)
     */
    data class SessionToSessionAdjustment(
        val weightMultiplier: Float,
        val setsAdjustment: Int,         // -1, 0, or +1
        val estimated1RM: Float,         // Updated e1RM from this session's data
        val message: String
    )

    fun analyzeSession(
        loggedSets: List<LoggedSet>,
        targetRpe: Float,
        oneRepMax: Float
    ): SessionToSessionAdjustment {
        if (loggedSets.isEmpty()) return SessionToSessionAdjustment(1.0f, 0, oneRepMax, "No data.")

        // Filter out skipped sets
        val completedSets = loggedSets.filter { !it.skipped }
        if (completedSets.isEmpty()) return SessionToSessionAdjustment(1.0f, 0, oneRepMax, "All sets skipped.")

        val avgRpe = completedSets.map { it.rpe }.average().toFloat()
        val rpeDiff = avgRpe - targetRpe

        // Estimate 1RM from the best set (highest estimated 1RM)
        val best1RM = completedSets.maxOf { set ->
            OneRepMaxEstimator.estimate1RMFromRPE(set.weightKg, set.reps, set.rpe)
        }

        val weightMultiplier: Float
        val setsAdj: Int
        val msg: String

        when {
            // Average RPE way over target → reduce load next time
            rpeDiff >= 1.5f -> {
                weightMultiplier = 0.95f
                setsAdj = -1
                msg = "Last session was very hard (avg RPE ${String.format("%.1f", avgRpe)}). Reducing load 5% and dropping 1 set."
            }
            rpeDiff >= 0.5f -> {
                weightMultiplier = 0.975f
                setsAdj = 0
                msg = "Last session was harder than target. Slight load reduction."
            }
            rpeDiff <= -1.0f -> {
                weightMultiplier = 1.05f
                setsAdj = 0
                msg = "Last session felt easy (avg RPE ${String.format("%.1f", avgRpe)}). Increasing load 5%."
            }
            rpeDiff <= -0.5f -> {
                weightMultiplier = 1.025f
                setsAdj = 0
                msg = "Room to grow. Small load increase."
            }
            else -> {
                weightMultiplier = 1.0f
                setsAdj = 0
                msg = "Right on target. Maintaining current load."
            }
        }

        return SessionToSessionAdjustment(
            weightMultiplier = weightMultiplier,
            setsAdjustment = setsAdj,
            estimated1RM = best1RM,
            message = msg
        )
    }
}