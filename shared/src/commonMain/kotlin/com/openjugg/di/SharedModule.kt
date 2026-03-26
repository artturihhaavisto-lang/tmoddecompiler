package com.openjugg.di

import com.openjugg.data.db.createDatabase
import com.openjugg.data.db.DatabaseDriverFactory
import com.openjugg.data.repository.*
import com.openjugg.domain.repository.*
import com.openjugg.domain.engine.ProgramGenerator
import org.koin.dsl.module

/**
 * Core shared Koin module.
 * This is injected identically on Android, iOS, and Desktop.
 * The only difference per platform is the DatabaseDriverFactory.
 */
val sharedModule = module {

    // ─── Database ────────────────────────────────────────
    single { createDatabase(get()) }

    // ─── Repositories ────────────────────────────────────
    single<IUserRepository>     { UserRepositoryImpl(get()) }
    single<IExerciseRepository> { ExerciseRepositoryImpl(get()) }
    single<IProgramRepository>  { ProgramRepositoryImpl(get()) }
}
