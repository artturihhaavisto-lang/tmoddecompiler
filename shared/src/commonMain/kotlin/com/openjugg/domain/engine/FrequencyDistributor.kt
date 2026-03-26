package com.openjugg.domain.engine

import com.openjugg.domain.model.LiftType

/**
 * Distributes SBD (Squat, Bench, Deadlift) training across the user's available
 * training days per week.
 *
 * Principles:
 *   - Bench can be trained more frequently (2-4x/week) due to lower systemic fatigue
 *   - Squat and deadlift share lower body demand → spread them apart
 *   - Never squat and deadlift heavy on the same day
 *   - Each session gets a "primary" lift and may get a secondary lift
 */
object FrequencyDistributor {

    /**
     * Represents what lifts go on a given training day.
     * @param dayOfWeek  1=Monday ... 7=Sunday
     * @param primary    The main lift for this day
     * @param secondary  Optional secondary lift (lighter or variation)
     * @param label      Human-friendly name
     */
    data class DaySlot(
        val dayOfWeek: Int,
        val primary: LiftType,
        val secondary: LiftType? = null,
        val label: String
    )

    /**
     * Generate the weekly training template.
     *
     * @param daysPerWeek  3-6 training days
     * @return             List of DaySlots mapping lifts to days
     */
    fun distribute(daysPerWeek: Int): List<DaySlot> {
        return when (daysPerWeek.coerceIn(3, 6)) {
            3 -> threeDaySplit()
            4 -> fourDaySplit()
            5 -> fiveDaySplit()
            6 -> sixDaySplit()
            else -> fourDaySplit()
        }
    }

    /**
     * 3-Day Split: Classic powerlifting layout
     *   Day 1 (Mon): Squat
     *   Day 2 (Wed): Bench
     *   Day 3 (Fri): Deadlift
     *
     * Frequency: S=1, B=1, D=1
     */
    private fun threeDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT, null, "Squat Day"),
        DaySlot(3, LiftType.BENCH, null, "Bench Day"),
        DaySlot(5, LiftType.DEADLIFT, null, "Deadlift Day")
    )

    /**
     * 4-Day Split: Upper/Lower hybrid
     *   Day 1 (Mon): Squat (primary) + Bench (light)
     *   Day 2 (Tue): Bench (primary)
     *   Day 3 (Thu): Deadlift (primary) + Bench (variation)
     *   Day 4 (Fri): Squat (variation)
     *
     * Frequency: S=2, B=3, D=1
     */
    private fun fourDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    LiftType.BENCH,    "Squat + Bench"),
        DaySlot(2, LiftType.BENCH,    null,               "Bench Day"),
        DaySlot(4, LiftType.DEADLIFT, LiftType.BENCH,    "Deadlift + Bench"),
        DaySlot(5, LiftType.SQUAT,    null,               "Squat Variation")
    )

    /**
     * 5-Day Split: High frequency
     *   Day 1 (Mon): Squat (primary)
     *   Day 2 (Tue): Bench (primary)
     *   Day 3 (Wed): Deadlift (primary)
     *   Day 4 (Thu): Bench (variation)
     *   Day 5 (Fri): Squat (variation) + Deadlift (light accessory)
     *
     * Frequency: S=2, B=2, D=2
     */
    private fun fiveDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    null,               "Squat Day"),
        DaySlot(2, LiftType.BENCH,    null,               "Bench Day"),
        DaySlot(3, LiftType.DEADLIFT, null,               "Deadlift Day"),
        DaySlot(4, LiftType.BENCH,    null,               "Bench Variation"),
        DaySlot(5, LiftType.SQUAT,    LiftType.DEADLIFT,  "Squat + Light DL")
    )

    /**
     * 6-Day Split: Very high frequency (advanced)
     *   Day 1 (Mon): Squat (heavy)
     *   Day 2 (Tue): Bench (heavy)
     *   Day 3 (Wed): Deadlift (heavy)
     *   Day 4 (Thu): Squat (variation)
     *   Day 5 (Fri): Bench (variation)
     *   Day 6 (Sat): Deadlift (variation)
     *
     * Frequency: S=2, B=2, D=2
     */
    private fun sixDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    null, "Squat (Heavy)"),
        DaySlot(2, LiftType.BENCH,    null, "Bench (Heavy)"),
        DaySlot(3, LiftType.DEADLIFT, null, "Deadlift (Heavy)"),
        DaySlot(4, LiftType.SQUAT,    null, "Squat (Variation)"),
        DaySlot(5, LiftType.BENCH,    null, "Bench (Variation)"),
        DaySlot(6, LiftType.DEADLIFT, null, "Deadlift (Variation)")
    )

    /**
     * Count how many times per week a given lift appears (as primary or secondary).
     */
    fun frequencyOf(slots: List<DaySlot>, liftType: LiftType): Int {
        return slots.count { it.primary == liftType || it.secondary == liftType }
    }
}