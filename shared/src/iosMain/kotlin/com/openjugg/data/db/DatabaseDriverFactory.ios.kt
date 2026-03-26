package com.openjugg.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.openjugg.db.OpenJuggDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = OpenJuggDatabase.Schema,
            name = "openjugg.db"
        )
    }
}
