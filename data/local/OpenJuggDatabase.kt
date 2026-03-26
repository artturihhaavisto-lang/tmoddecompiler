package com.openjugg.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openjugg.data.local.converter.Converters
import com.openjugg.data.local.dao.*
import com.openjugg.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        ExerciseEntity::class,
        ProgramEntity::class,
        PhaseEntity::class,
        TrainingWeekEntity::class,
        TrainingSessionEntity::class,
        ProgrammedExerciseEntity::class,
        LoggedSetEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OpenJuggDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
}