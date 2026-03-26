package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Calculates Minimum Effective Volume (MEV) and Maximum Recoverable Volume (MRV)
 * per lift per week, measured in hard working sets.
 *
 * Based on sports science literature and practical coaching guidelines:
 *   - Beginners need less volume and recover slower from relative intensity
 *   - Advanced lifters need more volume but can recover from more
 *   - Age, gender, and bodyweight influence recovery capacity
 *   - Each phase has different volume targets relative to MEV and MRV
 *
 * All values are in SETS PER WEEK for that specific lift (not muscle group).
 */
object VolumeCalculator {

    /**
     * Volume landmarks per lift per week.
     */
    data class VolumeLandmarks(
        val mev: Int,   // Minimum Effective Volume (sets/week) — minimum to progress
        val mrv: Int,   // Maximum Recoverable Volume (sets/week) — maximum before overtraining
        val mav: Int    // Maximum Adaptive Volume — the "sweet spot" between MEV and MRV
    )

    /**
     * Base volume ranges per experience level (sets per lift per week).
     * These are for the main competition lift (squat/bench/deadlift) only.
     */
    private data class BaseVolume(val mev: Int, val mrv: Int)

    private val baseVolumes = mapOf(
        ExperienceLevel.BEGINNER     to BaseVolume(mev = 6,  mrv = 12),
        ExperienceLevel.INTERMEDIATE to BaseVolume(mev = 8,  mrv = 16),
        ExperienceLevel.ADVANCED     to BaseVolume(mev = 10, mrv = 20),
        ExperienceLevel.ELITE        to BaseVolume(mev = 12, mrv = 24)
    )

    /**
     * Calculate volume landmarks for a given user and lift.
     *
     * Modifiers:
     *   - Age > 35: MRV decreases by 1 set per 5 years over 35
     *   - Age < 25: MRV increases by 1 (young recovery advantage)
     *   - Deadlift: MRV reduced by ~15% (higher systemic fatigue per set)
     *   - Female: Typically can handle slightly higher relative volume (+1 MEV, +2 MRV)
     */
    fun calculate(user: UserProfile, liftType: LiftType): VolumeLandmarks {
        val base = baseVolumes[user.experienceLevel]
            ?: baseVolumes[ExperienceLevel.INTERMEDIATE]!!

        var mev = base.mev
        var mrv = base.mrv

        // ─── Age modifier ────────────────────────────────
        if (user.age > 35) {
            val yearsOver35 = (user.age - 35) / 5
            mrv -= yearsOver35
        } else if (user.age < 25) {
            mrv += 1
        }

        // ─── Gender modifier ─────────────────────────────
        if (user.gender == Gender.FEMALE) {
            mev += 1
            mrv += 2
        }

        // ─── Lift-specific modifier ──────────────────────
        when (liftType) {
            LiftType.DEADLIFT -> {
                // Deadlifts are more systemically fatiguing
                mev = (mev * 0.85f).toInt().coerceAtLeast(4)
                mrv = (mrv * 0.85f).toInt()
            }
            LiftType.BENCH -> {
                // Bench tends to tolerate slightly more volume
                mrv += 1
            }
            LiftType.SQUAT -> { /* baseline */ }
        }

        // Ensure minimums
        mev = mev.coerceAtLeast(4)
        mrv = mrv.coerceAtLeast(mev + 4)

        // MAV = midpoint between MEV and MRV
        val mav = (mev + mrv) / 2

        return VolumeLandmarks(mev = mev, mrv = mrv, mav = mav)
    }

    /**
     * Determine the target sets for a specific phase of training.
     *
     *   Hypertrophy:  Start at MAV, ramp toward MRV over the phase weeks
     *   Strength:     Start at MEV + 2, stay around low-to-moderate volume
     *   Peaking:      At or slightly above MEV (minimal volume, maximal intensity)
     *   Deload:       50% of MEV
     */
    fun setsForPhaseWeek(
        landmarks: VolumeLandmarks,
        phaseType: PhaseType,
        weekInPhase: Int,     // 1-indexed
        totalWeeksInPhase: Int
    ): Int {
        val progress = weekInPhase.toFloat() / totalWeeksInPhase.toFloat()  // 0.0 → 1.0

        return when (phaseType) {
            PhaseType.HYPERTROPHY -> {
                // Ramp from MAV toward MRV
                val startVol = landmarks.mav
                val endVol = landmarks.mrv
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.STRENGTH -> {
                // Moderate volume, slight ramp down as intensity increases
                val startVol = landmarks.mav - 1
                val endVol = landmarks.mev + 2
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.PEAKING -> {
                // Low volume, decreasing toward the end
                val startVol = landmarks.mev + 1
                val endVol = landmarks.mev
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.DELOAD -> {
                // Half of MEV
                (landmarks.mev / 2).coerceAtLeast(2)
            }
        }
    }
}
