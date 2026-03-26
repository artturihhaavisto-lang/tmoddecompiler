package com.openjugg.domain.engine

import kotlin.math.roundToInt

/**
 * Estimates 1RM from weight × reps using multiple validated formulas.
 * Also calculates the weight needed for a given % of 1RM and target RPE.
 *
 * RPE-to-reps-in-reserve mapping:
 *   RPE 10 = 0 RIR (true max)
 *   RPE 9  = 1 RIR
 *   RPE 8  = 2 RIR
 *   RPE 7  = 3 RIR
 *   RPE 6  = 4 RIR
 */
object OneRepMaxEstimator {

    // ─── 1RM Estimation ──────────────────────────────────

    /** Epley formula: 1RM = w × (1 + r/30) */
    fun epley(weight: Float, reps: Int): Float =
        if (reps == 1) weight else weight * (1f + reps / 30f)

    /** Brzycki formula: 1RM = w × 36 / (37 - r) */
    fun brzycki(weight: Float, reps: Int): Float =
        if (reps == 1) weight else weight * 36f / (37f - reps)

    /**
     * Average of Epley and Brzycki for a more robust estimate.
     * Used as the primary estimator throughout the app.
     */
    fun estimate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0) return 0f
        if (reps == 1) return weight
        return (epley(weight, reps) + brzycki(weight, reps)) / 2f
    }

    /**
     * Estimate 1RM from a set logged at a specific RPE.
     * We convert RPE to effective reps:
     *   effectiveReps = actualReps + (10 - RPE)
     * Then use the standard formula.
     *
     * Example: 5 reps @ RPE 8 → effective reps = 5 + 2 = 7 → estimate 1RM from 7-rep set.
     */
    fun estimate1RMFromRPE(weight: Float, reps: Int, rpe: Float): Float {
        val repsInReserve = 10f - rpe
        val effectiveReps = reps + repsInReserve.toInt()
        return estimate1RM(weight, effectiveReps)
    }

    // ─── Weight Calculation ──────────────────────────────

    /**
     * Given a 1RM and a target percentage, return the working weight.
     * Result is rounded to nearest 2.5 kg (standard plate increments).
     */
    fun weightForPercentage(oneRepMax: Float, percentage: Float): Float {
        val raw = oneRepMax * percentage
        return roundToNearest(raw, 2.5f)
    }

    /**
     * Given a 1RM, target reps, and target RPE, calculate the suggested working weight.
     *
     * Logic: At RPE [targetRpe] for [targetReps], the effective max reps = targetReps + RIR.
     * We invert the Epley formula:
     *   weight = 1RM / (1 + effectiveReps / 30)
     */
    fun weightForRepsAndRPE(oneRepMax: Float, targetReps: Int, targetRpe: Float): Float {
        val rir = 10f - targetRpe
        val effectiveReps = targetReps + rir.toInt()
        if (effectiveReps <= 0) return oneRepMax
        val raw = oneRepMax / (1f + effectiveReps / 30f)
        return roundToNearest(raw, 2.5f)
    }

    // ─── RPE / Percentage Mapping ────────────────────────

    /**
     * Converts a phase-level intensity percentage + target RPE into a
     * practical working percentage of 1RM.
     *
     * This is a lookup table based on commonly used RPE charts:
     * Rows = reps (1-12), Columns = RPE (6-10)
     * Values = % of 1RM
     */
    private val rpePercentageChart: Map<Int, Map<Float, Float>> = mapOf(
        1  to mapOf(10f to 1.00f, 9.5f to 0.978f, 9f to 0.955f, 8.5f to 0.939f, 8f to 0.922f, 7.5f to 0.906f, 7f to 0.890f, 6.5f to 0.874f, 6f to 0.858f),
        2  to mapOf(10f to 0.955f, 9.5f to 0.939f, 9f to 0.922f, 8.5f to 0.906f, 8f to 0.890f, 7.5f to 0.874f, 7f to 0.858f, 6.5f to 0.842f, 6f to 0.826f),
        3  to mapOf(10f to 0.922f, 9.5f to 0.906f, 9f to 0.890f, 8.5f to 0.874f, 8f to 0.858f, 7.5f to 0.842f, 7f to 0.826f, 6.5f to 0.811f, 6f to 0.795f),
        4  to mapOf(10f to 0.890f, 9.5f to 0.874f, 9f to 0.858f, 8.5f to 0.842f, 8f to 0.826f, 7.5f to 0.811f, 7f to 0.795f, 6.5f to 0.779f, 6f to 0.763f),
        5  to mapOf(10f to 0.863f, 9.5f to 0.847f, 9f to 0.832f, 8.5f to 0.816f, 8f to 0.800f, 7.5f to 0.784f, 7f to 0.769f, 6.5f to 0.753f, 6f to 0.737f),
        6  to mapOf(10f to 0.837f, 9.5f to 0.822f, 9f to 0.807f, 8.5f to 0.791f, 8f to 0.775f, 7.5f to 0.760f, 7f to 0.744f, 6.5f to 0.728f, 6f to 0.713f),
        7  to mapOf(10f to 0.811f, 9.5f to 0.797f, 9f to 0.782f, 8.5f to 0.767f, 8f to 0.752f, 7.5f to 0.737f, 7f to 0.723f, 6.5f to 0.708f, 6f to 0.694f),
        8  to mapOf(10f to 0.786f, 9.5f to 0.773f, 9f to 0.760f, 8.5f to 0.746f, 8f to 0.732f, 7.5f to 0.718f, 7f to 0.704f, 6.5f to 0.690f, 6f to 0.676f),
        9  to mapOf(10f to 0.765f, 9.5f to 0.752f, 9f to 0.739f, 8.5f to 0.726f, 8f to 0.713f, 7.5f to 0.700f, 7f to 0.687f, 6.5f to 0.674f, 6f to 0.661f),
        10 to mapOf(10f to 0.744f, 9.5f to 0.732f, 9f to 0.720f, 8.5f to 0.707f, 8f to 0.695f, 7.5f to 0.683f, 7f to 0.670f, 6.5f to 0.658f, 6f to 0.646f),
        11 to mapOf(10f to 0.723f, 9.5f to 0.712f, 9f to 0.700f, 8.5f to 0.689f, 8f to 0.677f, 7.5f to 0.666f, 7f to 0.654f, 6.5f to 0.643f, 6f to 0.631f),
        12 to mapOf(10f to 0.703f, 9.5f to 0.694f, 9f to 0.684f, 8.5f to 0.674f, 8f to 0.664f, 7.5f to 0.654f, 7f to 0.644f, 6.5f to 0.634f, 6f to 0.624f)
    )

    /**
     * Look up the percentage of 1RM for a given rep count and RPE.
     * Falls back to Epley-based inverse calculation if outside chart range.
     */
    fun percentageFor(reps: Int, rpe: Float): Float {
        val clampedReps = reps.coerceIn(1, 12)
        val clampedRpe = rpe.coerceIn(6f, 10f)

        // Try exact lookup
        rpePercentageChart[clampedReps]?.get(clampedRpe)?.let { return it }

        // Interpolate between nearest RPE values
        val repRow = rpePercentageChart[clampedReps] ?: return weightForRepsAndRPE(1f, reps, rpe)
        val sortedKeys = repRow.keys.sorted()
        val lower = sortedKeys.lastOrNull { it <= clampedRpe } ?: sortedKeys.first()
        val upper = sortedKeys.firstOrNull { it >= clampedRpe } ?: sortedKeys.last()

        if (lower == upper) return repRow[lower]!!

        val lowerVal = repRow[lower]!!
        val upperVal = repRow[upper]!!
        val ratio = (clampedRpe - lower) / (upper - lower)
        return lowerVal + ratio * (upperVal - lowerVal)
    }

    /**
     * Get suggested weight from 1RM using the RPE chart.
     */
    fun suggestedWeight(oneRepMax: Float, reps: Int, rpe: Float): Float {
        val pct = percentageFor(reps, rpe)
        return roundToNearest(oneRepMax * pct, 2.5f)
    }

    // ─── Utility ─────────────────────────────────────────

    fun roundToNearest(value: Float, increment: Float): Float {
        return (Math.round(value / increment) * increment)
    }
}