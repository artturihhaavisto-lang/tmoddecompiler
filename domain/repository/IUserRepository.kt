package com.openjugg.domain.repository

import com.openjugg.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getActiveProfile(): Flow<UserProfile?>
    suspend fun getActiveProfileOnce(): UserProfile?
    suspend fun saveProfile(profile: UserProfile): Long
    suspend fun updateProfile(profile: UserProfile)
}