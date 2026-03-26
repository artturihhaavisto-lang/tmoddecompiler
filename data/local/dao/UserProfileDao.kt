package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY id DESC LIMIT 1")
    fun getActiveProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY id DESC LIMIT 1")
    suspend fun getActiveProfileOnce(): UserProfileEntity?

    @Delete
    suspend fun delete(profile: UserProfileEntity)
}