package com.openjugg.data.repository

import com.openjugg.data.local.dao.ProgramDao
import com.openjugg.data.local.entity.*
import com.openjugg.data.local.mapper.*
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IProgramRepository
import javax.inject.Inject

class ProgramRepositoryImpl @Inject constructor(
    private val dao: ProgramDao
) : IProgramRepository {

    /**
     * Persists the entire generated program tree:
     * Program → Phases → Weeks → Sessions → ProgrammedExercises
     */
    override suspend fun saveFullProgram(program: Program): Long {
        val programId = dao.insertProgram(
            ProgramEntity(
                userId = program.userId,
                name = program.name,
                goal = program.goal,
                startDate = program.startDate,
                endDate = program.endDate
            )
        )

        for (phase in program.phases) {
            val phaseId = dao.insertPhase(
                PhaseEntity(
                    programId = programId,
                    type = phase.type,
                    weekCount = phase.weekCount,
                    order = phase.order
                )
            )

            for (week in phase.weeks) {
                val weekId = dao.insertWeek(
                    TrainingWeekEntity(phaseId = phaseId, weekNumber = week.weekNumber)
                )

                for (session in week.sessions) {
                    val sessionId = dao.insertSession(
                        TrainingSessionEntity(
                            weekId = weekId,
                            dayOfWeek = session.dayOfWeek,
                            label = session.label,
                            readinessScore = null,
                            readinessInputs = null,
                            sessionDifficulty = null,
                            completed = false
                        )
                    )

                    for (exercise in session.exercises) {
                        dao.insertProgrammedExercise(
                            ProgrammedExerciseEntity(
                                sessionId = sessionId,
                                exerciseId = exercise.exerciseId,
                                exerciseName = exercise.exerciseName,
                                order = exercise.order,
                                targetSets = exercise.targetSets,
                                targetReps = exercise.targetReps,
                                targetRpe = exercise.targetRpe,
                                suggestedWeightKg = exercise.suggestedWeightKg,
                                isAmrap = exercise.isAmrap
                            )
                        )
                    }
                }
            }
        }

        return programId
    }

    /**
     * Reconstructs the full program tree from the database.
     */
    override suspend fun getActiveProgram(userId: Long): Program? {
        val programEntity = dao.getActiveProgramForUser(userId) ?: return null

        val phases = dao.getPhasesForProgram(programEntity.id).map { phaseEntity ->
            val weeks = dao.getWeeksForPhase(phaseEntity.id).map { weekEntity ->
                val sessions = dao.getSessionsForWeek(weekEntity.id).map { sessionEntity ->
                    val exercises = dao.getExercisesForSession(sessionEntity.id).map { exEntity ->
                        val sets = dao.getSetsForProgrammedExercise(exEntity.id).map { it.toDomain() }
                        exEntity.toDomain(sets)
                    }
                    sessionEntity.toDomain(exercises)
                }
                TrainingWeek(
                    id = weekEntity.id,
                    phaseId = weekEntity.phaseId,
                    weekNumber = weekEntity.weekNumber,
                    sessions = sessions
                )
            }
            Phase(
                id = phaseEntity.id,
                programId = phaseEntity.programId,
                type = phaseEntity.type,
                weekCount = phaseEntity.weekCount,
                order = phaseEntity.order,
                weeks = weeks
            )
        }

        return Program(
            id = programEntity.id,
            userId = programEntity.userId,
            name = programEntity.name,
            goal = programEntity.goal,
            startDate = programEntity.startDate,
            endDate = programEntity.endDate,
            phases = phases
        )
    }

    override suspend fun getSessionById(id: Long): TrainingSession? {
        val entity = dao.getSessionById(id) ?: return null
        val exercises = dao.getExercisesForSession(id).map { exEntity ->
            val sets = dao.getSetsForProgrammedExercise(exEntity.id).map { it.toDomain() }
            exEntity.toDomain(sets)
        }
        return entity.toDomain(exercises)
    }

    override suspend fun updateSession(session: TrainingSession) =
        dao.updateSession(session.toEntity())

    override suspend fun updateProgrammedExercise(exercise: ProgrammedExercise) =
        dao.updateProgrammedExercise(exercise.toEntity())

    override suspend fun logSet(set: LoggedSet): Long =
        dao.insertLoggedSet(set.toEntity())

    override suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSet> =
        dao.getSetsForProgrammedExercise(progExId).map { it.toDomain() }

    override suspend fun getExerciseHistory(exerciseId: Long): List<LoggedSet> =
        dao.getHistoryForExercise(exerciseId).map { it.toDomain() }
}