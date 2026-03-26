package com.openjugg.di

import com.openjugg.data.db.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 * Provides the Android SQLite driver using the app Context.
 */
val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
}
