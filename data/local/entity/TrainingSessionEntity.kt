package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.openjugg.domain.model.ReadinessInputs

@Entity(
    tableName = "training_sessions",
    foreignKeys = [ForeignKey(
        entity = TrainingWeekEntity::class,
        parentColumns = ["id"],
        childColumns = ["weekId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekId: Long,
    val dayOfWeek: Int,
    val label: String,
    val readinessScore: Float?,
    val readinessInputs: ReadinessInputs?,
    val sessionDifficulty: Int?,
    val completed: Boolean
)