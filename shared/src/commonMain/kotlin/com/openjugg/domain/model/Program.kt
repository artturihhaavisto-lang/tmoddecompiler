package com.openjugg.domain.model

data class Program(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val goal: TrainingGoal,
    val startDate: Long,    // epoch millis
    val endDate: Long,
    val phases: List<Phase> = emptyList()
)

data class Phase(
    val id: Long = 0,
    val programId: Long = 0,
    val type: PhaseType,
    val weekCount: Int,
    val order: Int,         // 0-indexed order in program
    val weeks: List<TrainingWeek> = emptyList()
)

data class TrainingWeek(
    val id: Long = 0,
    val phaseId: Long = 0,
    val weekNumber: Int,    // 1-indexed within the phase
    val sessions: List<TrainingSession> = emptyList()
)

data class TrainingSession(
    val id: Long = 0,
    val weekId: Long = 0,
    val dayOfWeek: Int,     // 1=Mon ... 7=Sun
    val label: String = "",  // e.g. "Squat Day", "Bench Day"
    val readinessScore: Float? = null,
    val readinessInputs: ReadinessInputs? = null,
    val sessionDifficulty: Int? = null,  // 1-10 post-workout
    val completed: Boolean = false,
    val exercises: List<ProgrammedExercise> = emptyList()
)


data class ProgrammedExercise(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long,
    val exerciseName: String = "",
    val order: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetRpe: Float,          // e.g. 7.0, 8.0, 9.0
    val suggestedWeightKg: Float,
    val isAmrap: Boolean = false,  // last set is AMRAP?
    val loggedSets: List<LoggedSet> = emptyList()
)

data class LoggedSet(
    val id: Long = 0,
    val programmedExerciseId: Long = 0,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rpe: Float,         // actual logged RPE
    val skipped: Boolean = false
)