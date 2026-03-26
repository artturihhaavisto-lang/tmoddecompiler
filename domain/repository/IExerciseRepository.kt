package com.openjugg.domain.repository

import com.openjugg.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getAllExercisesOnce(): List<Exercise>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun getByLiftType(liftType: LiftType): List<Exercise>
    suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<Exercise>
    suspend fun seedIfEmpty()
}