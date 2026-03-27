package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Produces the 4-wave Juggernaut Method structure:
 *   Wave 1 (HYPERTROPHY): 10s wave — 55/60/65% 1RM, 4 weeks
 *   Wave 2 (STRENGTH):    8s wave  — 65/70/75% 1RM, 4 weeks
 *   Wave 3 (PEAKING):     5s wave  — 75/80/82.5% 1RM, 4 weeks
 *   Wave 4 (COMPETITION): 3s wave  — 85/90/92.5% 1RM, 4 weeks
 *
 * Week 4 of every wave is a deload (handled by LoadProgressionManager).
 * Total: 16 weeks.
 */
object PeriodizationPlanner {

    data class PhaseTemplate(
        val type: PhaseType,
        val weeks: Int,
        val order: Int
    )

    fun planPhases(user: UserProfile, totalWeeks: Int? = null): List<PhaseTemplate> {
        // Standard Juggernaut: 4 waves × 4 weeks = 16 weeks
        // If meet date gives fewer weeks, drop later waves
        val available = totalWeeks ?: 16
        val waves = mutableListOf<PhaseTemplate>()
        val waveTypes = listOf(
            PhaseType.HYPERTROPHY,
            PhaseType.STRENGTH,
            PhaseType.PEAKING,
            PhaseType.COMPETITION
        )
        var order = 0
        var weeksUsed = 0
        for (waveType in waveTypes) {
            if (weeksUsed + 4 > available + 2) break  // skip wave if not enough room
            waves.add(PhaseTemplate(waveType, 4, order++))
            weeksUsed += 4
        }
        // Ensure at least 1 wave
        if (waves.isEmpty()) waves.add(PhaseTemplate(PhaseType.HYPERTROPHY, 4, 0))
        return waves
    }

    fun weeksUntilMeet(nowMillis: Long, meetDateMillis: Long): Int {
        val diff = meetDateMillis - nowMillis
        return (diff / (7L * 24 * 60 * 60 * 1000)).toInt().coerceAtLeast(4)
    }
}
