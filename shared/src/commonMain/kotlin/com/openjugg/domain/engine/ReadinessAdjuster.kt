package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Pre-session readiness system.
 *
 * Takes the user's subjective readiness inputs and produces adjustments
 * to the day's planned training:
 *   - Weight adjustment (as a multiplier, e.g. 0.90 = reduce by 10%)
 *   - Set adjustment (how many sets to add or remove from accessories)
 *   - Whether to skip variation work entirely
 *
 * Readiness composite score (1-5):
 *   = (sleep + nutrition + motivation + (6 - overallSoreness)) / 4
 *
 * Note: soreness is inverted because 5 = extremely sore = bad.
 */
object ReadinessAdjuster {

    data class Adjustments(
        val weightMultiplier: Float,        // e.g., 1.0 = no change, 0.90 = -10%
        val accessorySetDelta: Int,         // e.g., -1 = drop 1 set from each accessory
        val skipVariations: Boolean,        // If true, only do competition lift
        val compositeScore: Float,          // For logging/display
        val message: String                 // User-facing feedback
    )

    /**
     * Calculate readiness adjustments.
     */
    fun assess(inputs: ReadinessInputs): Adjustments {
        // Invert soreness: 1 (fresh) → 5, 5 (destroyed) → 1
        val invertedSoreness = 6f - inputs.overallSoreness

        val composite = (inputs.sleep + inputs.nutrition + inputs.motivation + invertedSoreness) / 4f

        return when {
            // ─── Very low readiness (1.0 - 2.0) ─────────
            composite < 2.0f -> Adjustments(
                weightMultiplier = 0.85f,
                accessorySetDelta = -2,
                skipVariations = true,
                compositeScore = composite,
                message = "Very low readiness. Significantly reducing load and volume. Focus on movement quality today."
            )

            // ─── Low readiness (2.0 - 2.75) ─────────────
            composite < 2.75f -> Adjustments(
                weightMultiplier = 0.90f,
                accessorySetDelta = -1,
                skipVariations = false,
                compositeScore = composite,
                message = "Below average readiness. Slightly reducing load. Listen to your body."
            )

            // ─── Normal readiness (2.75 - 3.75) ─────────
            composite < 3.75f -> Adjustments(
                weightMultiplier = 1.0f,
                accessorySetDelta = 0,
                skipVariations = false,
                compositeScore = composite,
                message = "Normal readiness. Proceeding as planned."
            )

            // ─── High readiness (3.75 - 4.5) ────────────
            composite < 4.5f -> Adjustments(
                weightMultiplier = 1.0f,  // Don't auto-increase weight; let RPE guide it
                accessorySetDelta = 0,
                skipVariations = false,
                compositeScore = composite,
                message = "Feeling good! You may push intensity if RPE allows."
            )

            // ─── Excellent readiness (4.5 - 5.0) ────────
            else -> Adjustments(
                weightMultiplier = 1.02f,  // Tiny bump — confidence boost
                accessorySetDelta = 1,     // Can handle extra accessory volume
                skipVariations = false,
                compositeScore = composite,
                message = "Excellent readiness! Slight weight increase. Consider an extra set on accessories."
            )
        }
    }

    /**
     * Check if a specific muscle group is too sore to train.
     * Used to further reduce volume for exercises targeting that muscle.
     *
     * @return multiplier for sets on exercises targeting that muscle (0.5 = halve sets, 1.0 = no change)
     */
    fun muscleGroupSorenessMultiplier(inputs: ReadinessInputs, liftType: LiftType): Float {
        val relevantSoreness = when (liftType) {
            LiftType.SQUAT -> maxOf(inputs.quadSoreness, inputs.hamstringSoreness)
            LiftType.BENCH -> maxOf(inputs.chestSoreness, inputs.shoulderSoreness)
            LiftType.DEADLIFT -> maxOf(inputs.hamstringSoreness, inputs.backSoreness)
        }

        return when {
            relevantSoreness >= 5 -> 0.50f   // Extremely sore → halve volume
            relevantSoreness >= 4 -> 0.75f   // Very sore → reduce by 25%
            else -> 1.0f                      // Manageable
        }
    }
}