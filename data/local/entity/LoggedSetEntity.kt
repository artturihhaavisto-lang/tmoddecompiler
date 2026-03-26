package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "logged_sets",
    foreignKeys = [ForeignKey(
        entity = ProgrammedExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["programmedExerciseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoggedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programmedExerciseId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rpe: Float,
    val skipped: Boolean
)