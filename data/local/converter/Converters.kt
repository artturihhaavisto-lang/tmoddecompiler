package com.openjugg.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openjugg.domain.model.*

class Converters {

    private val gson = Gson()

    // ─── Generic List<String> ────────────────────────────

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    // ─── List<WeakPoint> ─────────────────────────────────

    @TypeConverter
    fun fromWeakPointList(value: List<WeakPoint>): String = gson.toJson(value)

    @TypeConverter
    fun toWeakPointList(value: String): List<WeakPoint> =
        gson.fromJson(value, object : TypeToken<List<WeakPoint>>() {}.type)

    // ─── List<MuscleGroup> ───────────────────────────────

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String = gson.toJson(value)

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> =
        gson.fromJson(value, object : TypeToken<List<MuscleGroup>>() {}.type)

    // ─── ReadinessInputs (embedded as JSON) ──────────────

    @TypeConverter
    fun fromReadinessInputs(value: ReadinessInputs?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toReadinessInputs(value: String?): ReadinessInputs? =
        value?.let { gson.fromJson(it, ReadinessInputs::class.java) }

    // ─── Enums ───────────────────────────────────────────

    @TypeConverter fun fromGender(v: Gender): String = v.name
    @TypeConverter fun toGender(v: String): Gender = Gender.valueOf(v)

    @TypeConverter fun fromExpLevel(v: ExperienceLevel): String = v.name
    @TypeConverter fun toExpLevel(v: String): ExperienceLevel = ExperienceLevel.valueOf(v)

    @TypeConverter fun fromGoal(v: TrainingGoal): String = v.name
    @TypeConverter fun toGoal(v: String): TrainingGoal = TrainingGoal.valueOf(v)

    @TypeConverter fun fromPhaseType(v: PhaseType): String = v.name
    @TypeConverter fun toPhaseType(v: String): PhaseType = PhaseType.valueOf(v)

    @TypeConverter fun fromCategory(v: ExerciseCategory): String = v.name
    @TypeConverter fun toCategory(v: String): ExerciseCategory = ExerciseCategory.valueOf(v)

    @TypeConverter fun fromLiftType(v: LiftType): String = v.name
    @TypeConverter fun toLiftType(v: String): LiftType = LiftType.valueOf(v)

    @TypeConverter fun fromEquipment(v: EquipmentType): String = v.name
    @TypeConverter fun toEquipment(v: String): EquipmentType = EquipmentType.valueOf(v)
}