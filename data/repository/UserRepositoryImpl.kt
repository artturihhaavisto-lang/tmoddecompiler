package com.openjugg.data.repository

import com.openjugg.data.local.dao.UserProfileDao
import com.openjugg.data.local.mapper.toDomain
import com.openjugg.data.local.mapper.toEntity
import com.openjugg.domain.model.UserProfile
import com.openjugg.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : IUserRepository {

    override fun getActiveProfile(): Flow<UserProfile?> =
        dao.getActiveProfile().map { it?.toDomain() }

    override suspend fun getActiveProfileOnce(): UserProfile? =
        dao.getActiveProfileOnce()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile): Long =
        dao.insert(profile.toEntity())

    override suspend fun updateProfile(profile: UserProfile) =
        dao.update(profile.toEntity())
}