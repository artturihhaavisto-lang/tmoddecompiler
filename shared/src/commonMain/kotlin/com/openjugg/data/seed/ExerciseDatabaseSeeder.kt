package com.openjugg.data.seed

import com.openjugg.data.local.dao.ExerciseDao
import com.openjugg.data.local.entity.ExerciseEntity
import com.openjugg.domain.model.Exercise

/**
 * Seeds the Room database with the initial exercise library on first launch.
 */
class ExerciseDatabaseSeeder(private val exerciseDao: ExerciseDao) {

    suspend fun seedIfEmpty() {
        if (exerciseDao.count() > 0) return

        val entities = ExerciseSeedData.getAll().map { it.toEntity() }
        exerciseDao.insertAll(entities)
    }

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        name = name,
        liftType = liftType,
        category = category,
        primaryMuscles = primaryMuscles,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        description = description,
        cues = cues,
        commonMistakes = commonMistakes,
        addressesWeakPoints = addressesWeakPoints
    )
}