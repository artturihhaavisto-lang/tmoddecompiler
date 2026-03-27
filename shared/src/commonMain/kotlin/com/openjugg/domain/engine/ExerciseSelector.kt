package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Selects exercises for each training session based on:
 *   1. The lift type for that day (Squat/Bench/Deadlift)
 *   2. Whether it's a primary day or variation day
 *   3. The user's weak points
 *   4. The current training phase
 *   5. Available exercises in the library
 *
 * Selection priority:
 *   - Primary day → competition lift first, then weak-point variation, then accessories
 *   - Variation day → weak-point variation first, then accessories
 *   - Hypertrophy → more accessories
 *   - Peaking → competition lift only, minimal accessories
 */
object ExerciseSelector {

    data class SelectedExercise(
        val exercise: Exercise,
        val isPrimary: Boolean   // Is this the main lift or an accessory?
    )

    /**
     * Select exercises for a single session slot.
     *
     * @param liftType        The main lift for this slot
     * @param isVariationDay  Is this a "secondary" day for this lift?
     * @param phaseType       Current phase
     * @param weakPoints      User's weak points for this lift
     * @param exerciseLibrary All available exercises
     * @param totalSets       Total sets budgeted for this lift this session
     * @return                Ordered list of selected exercises with metadata
     */
    fun selectForSession(
        liftType: LiftType,
        isVariationDay: Boolean,
        phaseType: PhaseType,
        weakPoints: List<WeakPoint>,
        exerciseLibrary: List<Exercise>,
        totalSets: Int,
        allowedExerciseIds: Set<Long> = emptySet()
    ): List<SelectedExercise> {

        val liftExercises = exerciseLibrary.filter { it.liftType == liftType }
        val filtered = if (allowedExerciseIds.isEmpty()) liftExercises
                       else liftExercises.filter { it.category == ExerciseCategory.PRIMARY || it.id in allowedExerciseIds }
        val primary = filtered.filter { it.category == ExerciseCategory.PRIMARY }
        val variations = filtered.filter { it.category == ExerciseCategory.VARIATION }
        val accessories = filtered.filter { it.category == ExerciseCategory.ACCESSORY }

        // Score variations by how many of the user's weak points they address
        val scoredVariations = variations
            .map { ex -> ex to ex.addressesWeakPoints.count { it in weakPoints } }
            .sortedByDescending { it.second }

        val scoredAccessories = accessories
            .map { ex -> ex to ex.addressesWeakPoints.count { it in weakPoints } }
            .sortedByDescending { it.second }

        val result = mutableListOf<SelectedExercise>()

        when {
            // ─── PEAKING: Competition lift only ──────────
            phaseType == PhaseType.PEAKING -> {
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }
            }

            // ─── DELOAD: Competition lift, light ─────────
            phaseType == PhaseType.DELOAD -> {
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }
                // Maybe one light accessory
                scoredAccessories.firstOrNull()?.let {
                    result.add(SelectedExercise(it.first, isPrimary = false))
                }
            }

            // ─── VARIATION DAY ───────────────────────────
            isVariationDay -> {
                // Lead with best variation for weak points
                val bestVariation = scoredVariations.firstOrNull()
                if (bestVariation != null) {
                    result.add(SelectedExercise(bestVariation.first, isPrimary = true))
                } else {
                    // Fallback to competition lift
                    primary.firstOrNull()?.let {
                        result.add(SelectedExercise(it, isPrimary = true))
                    }
                }

                // Add accessories based on remaining set budget
                val accessorySets = (totalSets - estimateSetsForExercise(result.size, totalSets))
                    .coerceAtLeast(0)
                addAccessories(scoredAccessories, result, accessorySets, phaseType)
            }

            // ─── PRIMARY DAY (Hypertrophy or Strength) ──
            else -> {
                // Competition lift first
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }

                // Add a variation if in hypertrophy (more volume to distribute)
                if (phaseType == PhaseType.HYPERTROPHY && scoredVariations.isNotEmpty()) {
                    result.add(SelectedExercise(scoredVariations.first().first, isPrimary = false))
                }

                // Fill remaining with accessories
                val usedSets = result.size * 3 // rough estimate: 3 sets each
                val remainingSets = (totalSets - usedSets).coerceAtLeast(0)
                addAccessories(scoredAccessories, result, remainingSets, phaseType)
            }
        }

        return result
    }

    /**
     * Add accessory exercises up to the set budget.
     */
    private fun addAccessories(
        scoredAccessories: List<Pair<Exercise, Int>>,
        result: MutableList<SelectedExercise>,
        availableSets: Int,
        phaseType: PhaseType
    ) {
        val maxAccessories = when (phaseType) {
            PhaseType.HYPERTROPHY -> 3
            PhaseType.STRENGTH    -> 2
            PhaseType.PEAKING     -> 1
            PhaseType.COMPETITION -> 1
            PhaseType.DELOAD      -> 1
        }

        val setsPerAccessory = 3
        val accessoryCount = (availableSets / setsPerAccessory).coerceAtMost(maxAccessories)

        val alreadySelected = result.map { it.exercise.id }.toSet()
        scoredAccessories
            .filter { it.first.id !in alreadySelected }
            .take(accessoryCount)
            .forEach { result.add(SelectedExercise(it.first, isPrimary = false)) }
    }

    /**
     * Rough estimate: primary exercise gets ~40% of total sets, variations get ~30%, rest is accessories.
     */
    private fun estimateSetsForExercise(exerciseIndex: Int, totalSets: Int): Int {
        return when (exerciseIndex) {
            0 -> (totalSets * 0.40f).toInt().coerceAtLeast(3)
            1 -> (totalSets * 0.30f).toInt().coerceAtLeast(2)
            else -> 3
        }
    }
}