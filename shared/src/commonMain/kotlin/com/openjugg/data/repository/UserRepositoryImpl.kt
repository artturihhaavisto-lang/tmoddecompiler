package com.openjugg.data.repository

import com.openjugg.data.db.toDomain
import com.openjugg.data.db.toJsonString
import com.openjugg.db.OpenJuggDatabase
import com.openjugg.domain.model.UserProfile
import com.openjugg.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl(
    private val db: OpenJuggDatabase
) : IUserRepository {

    private val queries = db.userProfileQueries

    override fun getActiveProfile(): Flow<UserProfile?> = flow {
        emit(queries.getActiveProfile().executeAsOneOrNull()?.toDomain())
    }

    override suspend fun getActiveProfileOnce(): UserProfile? =
        queries.getActiveProfile().executeAsOneOrNull()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile): Long {
        queries.insertUserProfile(
            name                = profile.name,
            age                 = profile.age.toLong(),
            gender              = profile.gender.name,
            bodyweightKg        = profile.bodyweightKg.toDouble(),
            heightCm            = profile.heightCm.toDouble(),
            experienceLevel     = profile.experienceLevel.name,
            trainingGoal        = profile.trainingGoal.name,
            daysPerWeek         = profile.daysPerWeek.toLong(),
            meetDate            = profile.meetDate,
            squatMax            = profile.squatMax.toDouble(),
            benchMax            = profile.benchMax.toDouble(),
            deadliftMax         = profile.deadliftMax.toDouble(),
            squatWeakPoints     = profile.squatWeakPoints.toJsonString(),
            benchWeakPoints     = profile.benchWeakPoints.toJsonString(),
            deadliftWeakPoints  = profile.deadliftWeakPoints.toJsonString()
        )
        return queries.getActiveProfile().executeAsOne().id
    }

    override suspend fun updateProfile(profile: UserProfile) {
        queries.updateUserProfile(
            id                  = profile.id,
            name                = profile.name,
            age                 = profile.age.toLong(),
            gender              = profile.gender.name,
            bodyweightKg        = profile.bodyweightKg.toDouble(),
            heightCm            = profile.heightCm.toDouble(),
            experienceLevel     = profile.experienceLevel.name,
            trainingGoal        = profile.trainingGoal.name,
            daysPerWeek         = profile.daysPerWeek.toLong(),
            meetDate            = profile.meetDate,
            squatMax            = profile.squatMax.toDouble(),
            benchMax            = profile.benchMax.toDouble(),
            deadliftMax         = profile.deadliftMax.toDouble(),
            squatWeakPoints     = profile.squatWeakPoints.toJsonString(),
            benchWeakPoints     = profile.benchWeakPoints.toJsonString(),
            deadliftWeakPoints  = profile.deadliftWeakPoints.toJsonString()
        )
    }
}
