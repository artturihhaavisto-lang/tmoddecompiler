package com.openjugg.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.openjugg.di.desktopModule
import com.openjugg.di.sharedModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(
            sharedModule,
            desktopModule
        )
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "OpenJugg"
        ) {
            // Compose Desktop UI will go here
            // App()
        }
    }
}
