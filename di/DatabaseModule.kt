package com.openjugg.di

import android.content.Context
import androidx.room.Room
import com.openjugg.data.local.OpenJuggDatabase
import com.openjugg.data.local.dao.*
import com.openjugg.data.seed.ExerciseDatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenJuggDatabase {
        return Room.databaseBuilder(
            context,
            OpenJuggDatabase::class.java,
            "openjugg.db"
        ).build()
    }

    @Provides fun provideUserProfileDao(db: OpenJuggDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideExerciseDao(db: OpenJuggDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideProgramDao(db: OpenJuggDatabase): ProgramDao = db.programDao()

    @Provides
    @Singleton
    fun provideExerciseSeeder(dao: ExerciseDao): ExerciseDatabaseSeeder =
        ExerciseDatabaseSeeder(dao)
}