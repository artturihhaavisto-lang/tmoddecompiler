package com.openjugg.di

import com.openjugg.data.db.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * Desktop/JVM-specific Koin module.
 * Provides the JVM SQLite driver.
 * The driver stores the DB at ~/.openjugg/openjugg.db
 */
val desktopModule = module {
    single { DatabaseDriverFactory() }
}
