package com.openjugg.domain.model

data class UserProfile(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: Gender,
    val bodyweightKg: Float,
    val heightCm: Float,
    val experienceLevel: ExperienceLevel,
    val trainingGoal: TrainingGoal,
    val daysPerWeek: Int,                    // 3-6
    val meetDate: Long? = null,              // epoch millis, nullable
    val squatMax: Float,                     // 1RM in kg
    val benchMax: Float,
    val deadliftMax: Float,
    val squatWeakPoints: List<WeakPoint> = emptyList(),
    val benchWeakPoints: List<WeakPoint> = emptyList(),
    val deadliftWeakPoints: List<WeakPoint> = emptyList()
)