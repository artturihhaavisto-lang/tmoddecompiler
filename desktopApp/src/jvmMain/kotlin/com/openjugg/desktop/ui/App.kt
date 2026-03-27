package com.openjugg.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    SetupScreen(onNext = viewModel::loadExercisesForSelection)

                is AppViewModel.Screen.LoadingExercises ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is AppViewModel.Screen.ExerciseSelection ->
                    ExerciseSelectionScreen(
                        exercises = s.exercises,
                        onBack = viewModel::backToSetup,
                        onGenerate = { selectedIds -> viewModel.generate(s.profile, selectedIds) }
                    )

                is AppViewModel.Screen.Generating ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is AppViewModel.Screen.ProgramView ->
                    ProgramScreen(program = s.program, onBack = viewModel::backToSetup)

                is AppViewModel.Screen.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                            if (s.canRetry) {
                                Button(onClick = viewModel::backToSetup) { Text("Back") }
                            }
                        }
                    }
            }
        }
    }
}
