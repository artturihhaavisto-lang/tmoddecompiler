package com.openjugg.domain.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val liftType: LiftType,
    val category: ExerciseCategory,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: EquipmentType = EquipmentType.BARBELL,
    val description: String = "",
    val cues: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val addressesWeakPoints: List<WeakPoint> = emptyList()
)