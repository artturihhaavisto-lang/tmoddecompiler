package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.*

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val liftType: LiftType,
    val category: ExerciseCategory,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: EquipmentType,
    val description: String,
    val cues: List<String>,
    val commonMistakes: List<String>,
    val addressesWeakPoints: List<WeakPoint>
)