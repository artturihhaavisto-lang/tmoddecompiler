package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * The master orchestrator. Ties all engine components together to generate
 * a complete, personalized training program from a user profile.
 *
 * Flow:
 *   1. PeriodizationPlanner   → decide phase structure
 *   2. VolumeCalculator       → decide volume per lift per week
 *   3. FrequencyDistributor   → map lifts to training days
 *   4. ExerciseSelector       → choose exercises for each session
 *   5. LoadProgressionManager → assign reps, RPE, intensity per week
 *   6. OneRepMaxEstimator     → convert intensity to actual kg
 *   7. Assemble the full Program object
 */
class ProgramGenerator(
    private val exerciseLibrary: List<Exercise>
) {

    /**
     * Generate a complete program for the given user.
     *
     * @param user  Fully filled-out user profile
     * @param allowedExerciseIds  Set of exercise IDs the user wants included. Empty = all.
     * @return      A complete Program with all phases, weeks, sessions, and exercises populated
     */
    fun generate(user: UserProfile, allowedExerciseIds: Set<Long> = emptySet()): Program {

        val nowMillis = System.currentTimeMillis()

        // ── Step 1: Determine phase structure ────────────
        val totalWeeks = if (user.meetDate != null) {
            PeriodizationPlanner.weeksUntilMeet(nowMillis, user.meetDate)
        } else null

        val phaseTemplates = PeriodizationPlanner.planPhases(user, totalWeeks)
        val programTotalWeeks = phaseTemplates.sumOf { it.weeks }

        // ── Step 2: Volume landmarks per lift ────────────
        val squatVolume = VolumeCalculator.calculate(user, LiftType.SQUAT)
        val benchVolume = VolumeCalculator.calculate(user, LiftType.BENCH)
        val deadliftVolume = VolumeCalculator.calculate(user, LiftType.DEADLIFT)

        val volumeMap = mapOf(
            LiftType.SQUAT to squatVolume,
            LiftType.BENCH to benchVolume,
            LiftType.DEADLIFT to deadliftVolume
        )

        // ── Step 3: Weekly day layout ────────────────────
        val daySlots = FrequencyDistributor.distribute(user.daysPerWeek)

        // For each lift, determine how many sessions per week it appears
        val freqMap = mapOf(
            LiftType.SQUAT to FrequencyDistributor.frequencyOf(daySlots, LiftType.SQUAT),
            LiftType.BENCH to FrequencyDistributor.frequencyOf(daySlots, LiftType.BENCH),
            LiftType.DEADLIFT to FrequencyDistributor.frequencyOf(daySlots, LiftType.DEADLIFT)
        )

        // ── Step 4-6: Build phases → weeks → sessions → exercises ──

        val maxes = mapOf(
            LiftType.SQUAT to user.squatMax,
            LiftType.BENCH to user.benchMax,
            LiftType.DEADLIFT to user.deadliftMax
        )

        val weakPointsMap = mapOf(
            LiftType.SQUAT to user.squatWeakPoints,
            LiftType.BENCH to user.benchWeakPoints,
            LiftType.DEADLIFT to user.deadliftWeakPoints
        )

        var globalWeekCounter = 0
        val phases = phaseTemplates.map { template ->

            val weeks = (1..template.weeks).map { weekNum ->
                globalWeekCounter++

                val sessions = daySlots.map { slot ->
                    buildSession(
                        slot = slot,
                        phaseType = template.type,
                        weekInPhase = weekNum,
                        totalWeeksInPhase = template.weeks,
                        maxes = maxes,
                        volumeMap = volumeMap,
                        freqMap = freqMap,
                        weakPointsMap = weakPointsMap,
                        allowedExerciseIds = allowedExerciseIds
                    )
                }

                TrainingWeek(weekNumber = weekNum, sessions = sessions)
            }

            Phase(type = template.type, weekCount = template.weeks, order = template.order, weeks = weeks)
        }

        val endDate = nowMillis + (programTotalWeeks * 7L * 24 * 60 * 60 * 1000)

        return Program(
            userId = user.id,
            name = "${user.trainingGoal.name} Program — ${programTotalWeeks} Weeks",
            goal = user.trainingGoal,
            startDate = nowMillis,
            endDate = endDate,
            phases = phases
        )
    }

    /**
     * Build a single training session.
     */
    private fun buildSession(
        slot: FrequencyDistributor.DaySlot,
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        maxes: Map<LiftType, Float>,
        volumeMap: Map<LiftType, VolumeCalculator.VolumeLandmarks>,
        freqMap: Map<LiftType, Int>,
        weakPointsMap: Map<LiftType, List<WeakPoint>>,
        allowedExerciseIds: Set<Long>
    ): TrainingSession {

        // Week 4 of every Juggernaut wave is a deload — use DELOAD phase type for
        // exercise selection so we pick the competition lift + 1 light accessory only.
        val selectorPhaseType = if (weekInPhase == 4) PhaseType.DELOAD else phaseType

        val allExercises = mutableListOf<ProgrammedExercise>()
        var exerciseOrder = 0

        // Build exercises for the PRIMARY lift of this day
        val primaryExercises = buildExercisesForLift(
            liftType = slot.primary,
            isVariationDay = false,  // Primary day
            phaseType = selectorPhaseType,
            weekInPhase = weekInPhase,
            totalWeeksInPhase = totalWeeksInPhase,
            oneRepMax = maxes[slot.primary] ?: 100f,
            volumeLandmarks = volumeMap[slot.primary]!!,
            frequency = freqMap[slot.primary] ?: 1,
            weakPoints = weakPointsMap[slot.primary] ?: emptyList(),
            startOrder = exerciseOrder,
            allowedExerciseIds = allowedExerciseIds
        )
        allExercises.addAll(primaryExercises)
        exerciseOrder += primaryExercises.size

        // Build exercises for the SECONDARY lift (if present — it's a variation/lighter day)
        slot.secondary?.let { secondaryLift ->
            val secondaryExercises = buildExercisesForLift(
                liftType = secondaryLift,
                isVariationDay = true,  // Secondary = variation day
                phaseType = selectorPhaseType,
                weekInPhase = weekInPhase,
                totalWeeksInPhase = totalWeeksInPhase,
                oneRepMax = maxes[secondaryLift] ?: 100f,
                volumeLandmarks = volumeMap[secondaryLift]!!,
                frequency = freqMap[secondaryLift] ?: 1,
                weakPoints = weakPointsMap[secondaryLift] ?: emptyList(),
                startOrder = exerciseOrder,
                allowedExerciseIds = allowedExerciseIds
            )
            allExercises.addAll(secondaryExercises)
        }

        val label = if (weekInPhase == 4) "${slot.label} (deload)" else slot.label
        return TrainingSession(
            dayOfWeek = slot.dayOfWeek,
            label = label,
            exercises = allExercises
        )
    }

    /**
     * Build the exercise list for a single lift within a session.
     */
    private fun buildExercisesForLift(
        liftType: LiftType,
        isVariationDay: Boolean,
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        oneRepMax: Float,
        volumeLandmarks: VolumeCalculator.VolumeLandmarks,
        frequency: Int,
        weakPoints: List<WeakPoint>,
        startOrder: Int,
        allowedExerciseIds: Set<Long>
    ): List<ProgrammedExercise> {

        // Fixed accessory set budget based on week in phase (Juggernaut structure)
        val accessorySetBudget = when (weekInPhase) {
            1 -> 12  // accumulation: 3 accessories × 3 sets
            2 -> 9   // intensification: 3 accessories × 3 sets
            3 -> 6   // realization: 2 accessories × 3 sets
            4 -> 3   // deload: 1 accessory × 3 sets
            else -> 9
        }

        // Select exercises
        val selectedExercises = ExerciseSelector.selectForSession(
            liftType = liftType,
            isVariationDay = isVariationDay,
            phaseType = phaseType,
            weakPoints = weakPoints,
            exerciseLibrary = exerciseLibrary,
            totalSets = accessorySetBudget,
            allowedExerciseIds = allowedExerciseIds
        )

        // Prescribe load for each
        return selectedExercises.mapIndexed { index, selected ->
            val prescription = LoadProgressionManager.prescribe(
                phaseType = phaseType,
                weekInPhase = weekInPhase,
                totalWeeksInPhase = totalWeeksInPhase,
                isPrimaryLift = selected.isPrimary,
                totalWeekSets = accessorySetBudget,
                exercisesInSession = selectedExercises.size
            )

            // For the competition lift, use full 1RM.
            // For variations, use 90% of 1RM as a proxy (variations are typically lighter).
            // For accessories, use 70% of 1RM.
            val effectiveMax = when (selected.exercise.category) {
                ExerciseCategory.PRIMARY   -> oneRepMax
                ExerciseCategory.VARIATION -> oneRepMax * 0.90f
                ExerciseCategory.ACCESSORY -> oneRepMax * 0.70f
            }

            val suggestedWeight = OneRepMaxEstimator.weightForPercentage(effectiveMax, prescription.intensityPercentage)

            ProgrammedExercise(
                exerciseId = selected.exercise.id,
                exerciseName = selected.exercise.name,
                order = startOrder + index,
                targetSets = prescription.targetSets,
                targetReps = prescription.targetReps,
                targetRpe = prescription.targetRpe,
                suggestedWeightKg = suggestedWeight,
                isAmrap = prescription.isAmrap && index == 0  // only first (main) exercise
            )
        }
    }
}
