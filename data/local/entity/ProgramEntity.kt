package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.TrainingGoal

@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val goal: TrainingGoal,
    val startDate: Long,
    val endDate: Long
)