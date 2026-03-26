package com.openjugg.data.db

import app.cash.sqldelight.db.SqlDriver
import com.openjugg.db.OpenJuggDatabase

/**
 * Expect/actual pattern: each platform implements its own driver.
 * commonMain only knows about the interface.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

/**
 * Creates the fully initialized SQLDelight database from any platform's driver.
 */
fun createDatabase(driverFactory: DatabaseDriverFactory): OpenJuggDatabase {
    val driver = driverFactory.createDriver()
    return OpenJuggDatabase(driver)
}
