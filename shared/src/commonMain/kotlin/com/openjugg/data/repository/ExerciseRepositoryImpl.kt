package com.openjugg.data.repository

import com.openjugg.data.db.toDomain
import com.openjugg.data.db.toJsonString
import com.openjugg.data.seed.ExerciseSeedData
import com.openjugg.db.OpenJuggDatabase
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExerciseRepositoryImpl(
    private val db: OpenJuggDatabase
) : IExerciseRepository {

    private val queries = db.exerciseQueries

    override fun getAllExercises(): Flow<List<Exercise>> = flow {
        emit(queries.getAllExercises().executeAsList().map { it.toDomain() })
    }

    override suspend fun getAllExercisesOnce(): List<Exercise> =
        queries.getAllExercises().executeAsList().map { it.toDomain() }

    override suspend fun getExerciseById(id: Long): Exercise? =
        queries.getExerciseById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun getByLiftType(liftType: LiftType): List<Exercise> =
        queries.getExercisesByLiftType(liftType.name).executeAsList().map { it.toDomain() }

    override suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<Exercise> =
        queries.getExercisesByLiftTypeAndCategory(liftType.name, category.name)
            .executeAsList().map { it.toDomain() }

    override suspend fun seedIfEmpty() {
        val count = queries.countExercises().executeAsOne()
        if (count > 0L) return

        ExerciseSeedData.getAll().forEach { exercise ->
            queries.insertExercise(
                id                  = exercise.id,
                name                = exercise.name,
                liftType            = exercise.liftType.name,
                category            = exercise.category.name,
                primaryMuscles      = exercise.primaryMuscles.toJsonString(),
                secondaryMuscles    = exercise.secondaryMuscles.toJsonString(),
                equipment           = exercise.equipment.name,
                description         = exercise.description,
                cues                = exercise.cues.toJsonString(),
                commonMistakes      = exercise.commonMistakes.toJsonString(),
                addressesWeakPoints = exercise.addressesWeakPoints.toJsonString()
            )
        }
    }
}
