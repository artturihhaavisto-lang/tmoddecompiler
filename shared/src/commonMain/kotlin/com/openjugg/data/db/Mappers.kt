package com.openjugg.data.db

import com.openjugg.db.*
import com.openjugg.domain.model.ExerciseCategory
import com.openjugg.domain.model.ExperienceLevel
import com.openjugg.domain.model.EquipmentType
import com.openjugg.domain.model.Gender
import com.openjugg.domain.model.LiftType
import com.openjugg.domain.model.MuscleGroup
import com.openjugg.domain.model.PhaseType
import com.openjugg.domain.model.ReadinessInputs
import com.openjugg.domain.model.TrainingGoal
import com.openjugg.domain.model.WeakPoint

// ─── UserProfile ─────────────────────────────────────────

fun UserProfile.toDomain() = com.openjugg.domain.model.UserProfile(
    id                 = id,
    name               = name,
    age                = age.toInt(),
    gender             = Gender.valueOf(gender),
    bodyweightKg       = bodyweightKg.toFloat(),
    heightCm           = heightCm.toFloat(),
    experienceLevel    = ExperienceLevel.valueOf(experienceLevel),
    trainingGoal       = TrainingGoal.valueOf(trainingGoal),
    daysPerWeek        = daysPerWeek.toInt(),
    meetDate           = meetDate,
    squatMax           = squatMax.toFloat(),
    benchMax           = benchMax.toFloat(),
    deadliftMax        = deadliftMax.toFloat(),
    squatWeakPoints    = squatWeakPoints.toWeakPointList(),
    benchWeakPoints    = benchWeakPoints.toWeakPointList(),
    deadliftWeakPoints = deadliftWeakPoints.toWeakPointList()
)

// ─── Exercise ────────────────────────────────────────────

fun Exercise.toDomain() = com.openjugg.domain.model.Exercise(
    id                  = id,
    name                = name,
    liftType            = LiftType.valueOf(liftType),
    category            = ExerciseCategory.valueOf(category),
    primaryMuscles      = primaryMuscles.toMuscleGroupList(),
    secondaryMuscles    = secondaryMuscles.toMuscleGroupList(),
    equipment           = EquipmentType.valueOf(equipment),
    description         = description,
    cues                = cues.toStringList(),
    commonMistakes      = commonMistakes.toStringList(),
    addressesWeakPoints = addressesWeakPoints.toWeakPointList()
)

// ─── Program ─────────────────────────────────────────────

fun Program.toDomain(phases: List<com.openjugg.domain.model.Phase> = emptyList()) = com.openjugg.domain.model.Program(
    id        = id,
    userId    = userId,
    name      = name,
    goal      = TrainingGoal.valueOf(goal),
    startDate = startDate,
    endDate   = endDate,
    phases    = phases
)

// ─── Phase ───────────────────────────────────────────────

fun Phase.toDomain(weeks: List<com.openjugg.domain.model.TrainingWeek> = emptyList()) = com.openjugg.domain.model.Phase(
    id        = id,
    programId = programId,
    type      = PhaseType.valueOf(type),
    weekCount = weekCount.toInt(),
    order     = orderIndex.toInt(),
    weeks     = weeks
)

// ─── TrainingWeek ────────────────────────────────────────

fun TrainingWeek.toDomain(sessions: List<com.openjugg.domain.model.TrainingSession> = emptyList()) = com.openjugg.domain.model.TrainingWeek(
    id         = id,
    phaseId    = phaseId,
    weekNumber = weekNumber.toInt(),
    sessions   = sessions
)

// ─── TrainingSession ─────────────────────────────────────

fun TrainingSession.toDomain(exercises: List<com.openjugg.domain.model.ProgrammedExercise> = emptyList()) = com.openjugg.domain.model.TrainingSession(
    id                = id,
    weekId            = weekId,
    dayOfWeek         = dayOfWeek.toInt(),
    label             = label,
    readinessScore    = readinessScore?.toFloat(),
    readinessInputs   = readinessInputs?.toReadinessInputs(),
    sessionDifficulty = sessionDifficulty?.toInt(),
    completed         = completed != 0L,
    exercises         = exercises
)

// ─── ProgrammedExercise ──────────────────────────────────

fun ProgrammedExercise.toDomain(loggedSets: List<com.openjugg.domain.model.LoggedSet> = emptyList()) = com.openjugg.domain.model.ProgrammedExercise(
    id                = id,
    sessionId         = sessionId,
    exerciseId        = exerciseId,
    exerciseName      = exerciseName,
    order             = orderIndex.toInt(),
    targetSets        = targetSets.toInt(),
    targetReps        = targetReps.toInt(),
    targetRpe         = targetRpe.toFloat(),
    suggestedWeightKg = suggestedWeightKg.toFloat(),
    isAmrap           = isAmrap != 0L,
    loggedSets        = loggedSets
)

// ─── LoggedSet ───────────────────────────────────────────

fun LoggedSet.toDomain() = com.openjugg.domain.model.LoggedSet(
    id                   = id,
    programmedExerciseId = programmedExerciseId,
    setNumber            = setNumber.toInt(),
    weightKg             = weightKg.toFloat(),
    reps                 = reps.toInt(),
    rpe                  = rpe.toFloat(),
    skipped              = skipped != 0L
)
