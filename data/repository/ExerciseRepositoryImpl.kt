package com.openjugg.data.repository

import com.openjugg.data.local.dao.ExerciseDao
import com.openjugg.data.local.mapper.toDomain
import com.openjugg.data.seed.ExerciseDatabaseSeeder
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val seeder: ExerciseDatabaseSeeder
) : IExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllExercisesOnce(): List<Exercise> =
        dao.getAllOnce().map { it.toDomain() }

    override suspend fun getExerciseById(id: Long): Exercise? =
        dao.getById(id)?.toDomain()

    override suspend fun getByLiftType(liftType: LiftType): List<Exercise> =
        dao.getByLiftType(liftType).map { it.toDomain() }

    override suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<Exercise> =
        dao.getByLiftTypeAndCategory(liftType, category).map { it.toDomain() }

    override suspend fun seedIfEmpty() = seeder.seedIfEmpty()
}