package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.*

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: Gender,
    val bodyweightKg: Float,
    val heightCm: Float,
    val experienceLevel: ExperienceLevel,
    val trainingGoal: TrainingGoal,
    val daysPerWeek: Int,
    val meetDate: Long?,
    val squatMax: Float,
    val benchMax: Float,
    val deadliftMax: Float,
    val squatWeakPoints: List<WeakPoint>,
    val benchWeakPoints: List<WeakPoint>,
    val deadliftWeakPoints: List<WeakPoint>
)