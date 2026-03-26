package com.openjugg.domain.repository

import com.openjugg.domain.model.*

interface IProgramRepository {
    suspend fun saveFullProgram(program: Program): Long
    suspend fun getActiveProgram(userId: Long): Program?
    suspend fun getSessionById(id: Long): TrainingSession?
    suspend fun updateSession(session: TrainingSession)
    suspend fun updateProgrammedExercise(exercise: ProgrammedExercise)
    suspend fun logSet(set: LoggedSet): Long
    suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSet>
    suspend fun getExerciseHistory(exerciseId: Long): List<LoggedSet>
}