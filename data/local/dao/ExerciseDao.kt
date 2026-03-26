package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.ExerciseEntity
import com.openjugg.domain.model.ExerciseCategory
import com.openjugg.domain.model.LiftType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Query("SELECT * FROM exercises")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllOnce(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE liftType = :liftType")
    suspend fun getByLiftType(liftType: LiftType): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE liftType = :liftType AND category = :category")
    suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}