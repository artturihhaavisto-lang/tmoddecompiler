package com.openjugg.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.openjugg.db.OpenJuggDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = OpenJuggDatabase.Schema,
            context = context,
            name = "openjugg.db"
        )
    }
}
