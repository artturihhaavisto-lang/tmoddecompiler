package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    // ─── Program ─────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity): Long

    @Query("SELECT * FROM programs WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveProgramForUser(userId: Long): ProgramEntity?

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgramById(id: Long): ProgramEntity?

    // ─── Phase ───────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: PhaseEntity): Long

    @Query("SELECT * FROM phases WHERE programId = :programId ORDER BY `order`")
    suspend fun getPhasesForProgram(programId: Long): List<PhaseEntity>

    // ─── Training Week ───────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeek(week: TrainingWeekEntity): Long

    @Query("SELECT * FROM training_weeks WHERE phaseId = :phaseId ORDER BY weekNumber")
    suspend fun getWeeksForPhase(phaseId: Long): List<TrainingWeekEntity>

    // ─── Training Session ────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSessionEntity): Long

    @Update
    suspend fun updateSession(session: TrainingSessionEntity)

    @Query("SELECT * FROM training_sessions WHERE weekId = :weekId ORDER BY dayOfWeek")
    suspend fun getSessionsForWeek(weekId: Long): List<TrainingSessionEntity>

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TrainingSessionEntity?

    // ─── Programmed Exercise ─────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammedExercise(exercise: ProgrammedExerciseEntity): Long

    @Update
    suspend fun updateProgrammedExercise(exercise: ProgrammedExerciseEntity)

    @Query("SELECT * FROM programmed_exercises WHERE sessionId = :sessionId ORDER BY `order`")
    suspend fun getExercisesForSession(sessionId: Long): List<ProgrammedExerciseEntity>

    // ─── Logged Sets ─────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedSet(set: LoggedSetEntity): Long

    @Query("SELECT * FROM logged_sets WHERE programmedExerciseId = :progExId ORDER BY setNumber")
    suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSetEntity>

    @Query("""
        SELECT ls.* FROM logged_sets ls
        INNER JOIN programmed_exercises pe ON ls.programmedExerciseId = pe.id
        WHERE pe.exerciseId = :exerciseId
        ORDER BY ls.id DESC
    """)
    suspend fun getHistoryForExercise(exerciseId: Long): List<LoggedSetEntity>

    // ─── Queries for the adaptation engine ───────────────

    /** Get all logged sets for a given session (across all exercises) */
    @Query("""
        SELECT ls.* FROM logged_sets ls
        INNER JOIN programmed_exercises pe ON ls.programmedExerciseId = pe.id
        WHERE pe.sessionId = :sessionId
        ORDER BY pe.`order`, ls.setNumber
    """)
    suspend fun getAllLoggedSetsForSession(sessionId: Long): List<LoggedSetEntity>

    /** Get all completed sessions for a given week */
    @Query("SELECT * FROM training_sessions WHERE weekId = :weekId AND completed = 1")
    suspend fun getCompletedSessionsForWeek(weekId: Long): List<TrainingSessionEntity>
}