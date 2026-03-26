package com.openjugg.data.local.mapper

import com.openjugg.data.local.entity.*
import com.openjugg.domain.model.*

// ─── UserProfile ─────────────────────────────────────────

fun UserProfileEntity.toDomain() = UserProfile(
    id = id, name = name, age = age, gender = gender,
    bodyweightKg = bodyweightKg, heightCm = heightCm,
    experienceLevel = experienceLevel, trainingGoal = trainingGoal,
    daysPerWeek = daysPerWeek, meetDate = meetDate,
    squatMax = squatMax, benchMax = benchMax, deadliftMax = deadliftMax,
    squatWeakPoints = squatWeakPoints, benchWeakPoints = benchWeakPoints,
    deadliftWeakPoints = deadliftWeakPoints
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id, name = name, age = age, gender = gender,
    bodyweightKg = bodyweightKg, heightCm = heightCm,
    experienceLevel = experienceLevel, trainingGoal = trainingGoal,
    daysPerWeek = daysPerWeek, meetDate = meetDate,
    squatMax = squatMax, benchMax = benchMax, deadliftMax = deadliftMax,
    squatWeakPoints = squatWeakPoints, benchWeakPoints = benchWeakPoints,
    deadliftWeakPoints = deadliftWeakPoints
)

// ─── Exercise ────────────────────────────────────────────

fun ExerciseEntity.toDomain() = Exercise(
    id = id, name = name, liftType = liftType, category = category,
    primaryMuscles = primaryMuscles, secondaryMuscles = secondaryMuscles,
    equipment = equipment, description = description, cues = cues,
    commonMistakes = commonMistakes, addressesWeakPoints = addressesWeakPoints
)

// ─── LoggedSet ───────────────────────────────────────────

fun LoggedSetEntity.toDomain() = LoggedSet(
    id = id, programmedExerciseId = programmedExerciseId,
    setNumber = setNumber, weightKg = weightKg,
    reps = reps, rpe = rpe, skipped = skipped
)

fun LoggedSet.toEntity() = LoggedSetEntity(
    id = id, programmedExerciseId = programmedExerciseId,
    setNumber = setNumber, weightKg = weightKg,
    reps = reps, rpe = rpe, skipped = skipped
)

// ─── ProgrammedExercise ──────────────────────────────────

fun ProgrammedExerciseEntity.toDomain(loggedSets: List<LoggedSet> = emptyList()) = ProgrammedExercise(
    id = id, sessionId = sessionId, exerciseId = exerciseId,
    exerciseName = exerciseName, order = order,
    targetSets = targetSets, targetReps = targetReps,
    targetRpe = targetRpe, suggestedWeightKg = suggestedWeightKg,
    isAmrap = isAmrap, loggedSets = loggedSets
)

fun ProgrammedExercise.toEntity() = ProgrammedExerciseEntity(
    id = id, sessionId = sessionId, exerciseId = exerciseId,
    exerciseName = exerciseName, order = order,
    targetSets = targetSets, targetReps = targetReps,
    targetRpe = targetRpe, suggestedWeightKg = suggestedWeightKg,
    isAmrap = isAmrap
)

// ─── TrainingSession ─────────────────────────────────────

fun TrainingSessionEntity.toDomain(exercises: List<ProgrammedExercise> = emptyList()) = TrainingSession(
    id = id, weekId = weekId, dayOfWeek = dayOfWeek, label = label,
    readinessScore = readinessScore, readinessInputs = readinessInputs,
    sessionDifficulty = sessionDifficulty, completed = completed,
    exercises = exercises
)

fun TrainingSession.toEntity() = TrainingSessionEntity(
    id = id, weekId = weekId, dayOfWeek = dayOfWeek, label = label,
    readinessScore = readinessScore, readinessInputs = readinessInputs,
    sessionDifficulty = sessionDifficulty, completed = completed
)