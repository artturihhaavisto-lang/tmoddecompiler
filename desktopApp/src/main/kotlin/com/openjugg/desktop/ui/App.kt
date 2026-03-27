package com.openjugg.desktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.openjugg.desktop.AppViewModel

@Composable
fun App(viewModel: AppViewModel) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val screen by viewModel.screen.collectAsState()
            when (val s = screen) {
                is AppViewModel.Screen.Setup ->
                    SetupScreen(onGenerate = viewModel::generate)

                is AppViewModel.Screen.Generating ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is AppViewModel.Screen.ProgramView ->
                    ProgramScreen(program = s.program, onBack = viewModel::backToSetup)

                is AppViewModel.Screen.Error ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${s.message}")
                    }
            }
        }
    }
}
