package com.openjugg.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.openjugg.db.OpenJuggDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".openjugg")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "openjugg.db")

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (!dbFile.exists() || dbFile.length() == 0L) {
            OpenJuggDatabase.Schema.create(driver)
        }

        return driver
    }
}
