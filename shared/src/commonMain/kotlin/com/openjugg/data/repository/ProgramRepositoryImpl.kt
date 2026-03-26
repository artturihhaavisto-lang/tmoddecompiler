package com.openjugg.data.repository

import com.openjugg.data.db.toDomain
import com.openjugg.data.db.toJsonString
import com.openjugg.db.OpenJuggDatabase
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IProgramRepository

class ProgramRepositoryImpl(
    private val db: OpenJuggDatabase
) : IProgramRepository {

    private val programQ  = db.programQueries
    private val phaseQ    = db.phaseQueries
    private val weekQ     = db.trainingWeekQueries
    private val sessionQ  = db.trainingSessionQueries
    private val exQ       = db.programmedExerciseQueries
    private val setQ      = db.loggedSetQueries

    /**
     * Persist the entire generated program tree in a single transaction.
     */
    override suspend fun saveFullProgram(program: Program): Long {
        var savedProgramId = -1L

        db.transaction {
            programQ.insertProgram(
                userId    = program.userId,
                name      = program.name,
                goal      = program.goal.name,
                startDate = program.startDate,
                endDate   = program.endDate
            )
            savedProgramId = programQ.getActiveProgramForUser(program.userId)
                .executeAsOne().id

            program.phases.forEach { phase ->
                phaseQ.insertPhase(
                    programId  = savedProgramId,
                    type       = phase.type.name,
                    weekCount  = phase.weekCount.toLong(),
                    orderIndex = phase.order.toLong()
                )
                val phaseId = phaseQ.getPhasesForProgram(savedProgramId)
                    .executeAsList().last().id

                phase.weeks.forEach { week ->
                    weekQ.insertTrainingWeek(
                        phaseId    = phaseId,
                        weekNumber = week.weekNumber.toLong()
                    )
                    val weekId = weekQ.getWeeksForPhase(phaseId)
                        .executeAsList().last().id

                    week.sessions.forEach { session ->
                        sessionQ.insertTrainingSession(
                            weekId            = weekId,
                            dayOfWeek         = session.dayOfWeek.toLong(),
                            label             = session.label,
                            readinessScore    = null,
                            readinessInputs   = null,
                            sessionDifficulty = null,
                            completed         = 0L
                        )
                        val sessionId = sessionQ.getSessionsForWeek(weekId)
                            .executeAsList().last().id

                        session.exercises.forEach { exercise ->
                            exQ.insertProgrammedExercise(
                                sessionId         = sessionId,
                                exerciseId        = exercise.exerciseId,
                                exerciseName      = exercise.exerciseName,
                                orderIndex        = exercise.order.toLong(),
                                targetSets        = exercise.targetSets.toLong(),
                                targetReps        = exercise.targetReps.toLong(),
                                targetRpe         = exercise.targetRpe.toDouble(),
                                suggestedWeightKg = exercise.suggestedWeightKg.toDouble(),
                                isAmrap           = if (exercise.isAmrap) 1L else 0L
                            )
                        }
                    }
                }
            }
        }

        return savedProgramId
    }

    /**
     * Reconstruct the full program tree from the database.
     */
    override suspend fun getActiveProgram(userId: Long): Program? {
        val programRow = programQ.getActiveProgramForUser(userId).executeAsOneOrNull()
            ?: return null

        val phases = phaseQ.getPhasesForProgram(programRow.id).executeAsList().map { phaseRow ->
            val weeks = weekQ.getWeeksForPhase(phaseRow.id).executeAsList().map { weekRow ->
                val sessions = sessionQ.getSessionsForWeek(weekRow.id).executeAsList().map { sessionRow ->
                    val exercises = exQ.getExercisesForSession(sessionRow.id).executeAsList().map { exRow ->
                        val sets = setQ.getSetsForProgrammedExercise(exRow.id)
                            .executeAsList().map { it.toDomain() }
                        exRow.toDomain(sets)
                    }
                    sessionRow.toDomain(exercises)
                }
                weekRow.toDomain(sessions)
            }
            phaseRow.toDomain(weeks)
        }

        return programRow.toDomain(phases)
    }

    override suspend fun getSessionById(id: Long): TrainingSession? {
        val row = sessionQ.getSessionById(id).executeAsOneOrNull() ?: return null
        val exercises = exQ.getExercisesForSession(id).executeAsList().map { exRow ->
            val sets = setQ.getSetsForProgrammedExercise(exRow.id)
                .executeAsList().map { it.toDomain() }
            exRow.toDomain(sets)
        }
        return row.toDomain(exercises)
    }

    override suspend fun updateSession(session: TrainingSession) {
        sessionQ.updateTrainingSession(
            id                = session.id,
            readinessScore    = session.readinessScore?.toDouble(),
            readinessInputs   = session.readinessInputs?.toJsonString(),
            sessionDifficulty = session.sessionDifficulty?.toLong(),
            completed         = if (session.completed) 1L else 0L
        )
    }

    override suspend fun updateProgrammedExercise(exercise: ProgrammedExercise) {
        exQ.updateProgrammedExercise(
            id                = exercise.id,
            suggestedWeightKg = exercise.suggestedWeightKg.toDouble(),
            targetSets        = exercise.targetSets.toLong()
        )
    }

    override suspend fun logSet(set: LoggedSet): Long {
        setQ.insertLoggedSet(
            programmedExerciseId = set.programmedExerciseId,
            setNumber            = set.setNumber.toLong(),
            weightKg             = set.weightKg.toDouble(),
            reps                 = set.reps.toLong(),
            rpe                  = set.rpe.toDouble(),
            skipped              = if (set.skipped) 1L else 0L
        )
        return setQ.getSetsForProgrammedExercise(set.programmedExerciseId)
            .executeAsList().last().id
    }

    override suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSet> =
        setQ.getSetsForProgrammedExercise(progExId).executeAsList().map { it.toDomain() }

    override suspend fun getExerciseHistory(exerciseId: Long): List<LoggedSet> =
        setQ.getHistoryForExercise(exerciseId).executeAsList().map { it.toDomain() }
}
