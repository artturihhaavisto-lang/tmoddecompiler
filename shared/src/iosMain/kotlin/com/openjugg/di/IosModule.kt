package com.openjugg.di

import com.openjugg.data.db.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 * Provides the native SQLite driver for iOS/iPadOS/macOS.
 */
val iosModule = module {
    single { DatabaseDriverFactory() }
}
