package com.openjugg.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.openjugg.desktop.ui.App
import com.openjugg.di.desktopModule
import com.openjugg.di.sharedModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(sharedModule, desktopModule)
    }

    val viewModel = AppViewModel()

    application {
        Window(
            onCloseRequest = {
                viewModel.dispose()
                exitApplication()
            },
            title = "OpenJugg",
            state = rememberWindowState(width = 960.dp, height = 720.dp)
        ) {
            App(viewModel)
        }
    }
}
