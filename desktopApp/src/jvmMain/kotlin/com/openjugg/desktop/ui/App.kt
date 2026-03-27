package com.openjugg.desktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.openjugg.desktop.AppViewModel

// ── Waybar-matching palette ───────────────────────────────────────────────────
// Source: ~/.config/waybar/style.css
// Background #000000, surfaces #0d0d0d/#111111, text #888888→#cccccc
// Accent: gold #ffdd44 (bible widget), red #cc4444, salmon #ef6478
// No purple.

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF0D0D0D)
private val Surface2    = Color(0xFF111111)
private val Surface3    = Color(0xFF1A1A1A)
private val Border      = Color(0xFF222222)
private val Muted1      = Color(0xFF333333)
private val Muted2      = Color(0xFF444444)
private val Muted3      = Color(0xFF666666)
private val Mid         = Color(0xFF888888)
private val Bright      = Color(0xFFAAAAAA)
private val White       = Color(0xFFCCCCCC)
private val Gold        = Color(0xFFFFDD44)   // #custom-bible accent, no purple
private val GoldDim     = Color(0xFF1A1400)
private val Red         = Color(0xFFCC4444)
private val Salmon      = Color(0xFFEF6478)

val OpenJuggColorScheme = darkColorScheme(
    background            = Black,
    onBackground          = Mid,
    surface               = Surface1,
    onSurface             = White,
    surfaceVariant        = Surface2,
    onSurfaceVariant      = Muted3,
    surfaceContainerHigh  = Surface3,
    surfaceContainerLow   = Surface1,
    primary               = White,
    onPrimary             = Black,
    primaryContainer      = Surface3,
    onPrimaryContainer    = White,
    secondary             = Bright,
    onSecondary           = Black,
    secondaryContainer    = Surface2,
    onSecondaryContainer  = Bright,
    tertiary              = Gold,
    onTertiary            = Black,
    tertiaryContainer     = GoldDim,
    onTertiaryContainer   = Gold,
    error                 = Red,
    onError               = Black,
    errorContainer        = Color(0xFF2A0A0A),
    onErrorContainer      = Red,
    outline               = Border,
    outlineVariant        = Surface3,
    inverseSurface        = White,
    inverseOnSurface      = Black,
    inversePrimary        = Muted1,
    scrim                 = Black,
)

// Phase accent colors (no purple)
val colorHypertrophy = Color(0xFF4A8A4A)   // muted green   — 10s wave
val colorStrength    = Color(0xFF4A7A9A)   // muted blue    — 8s wave
val colorPeaking     = Color(0xFFCC6644)   // amber-red     — 5s wave
val colorCompetition = Color(0xFFFFDD44)   // gold          — 3s wave
val colorDeload      = Color(0xFF555555)   // gray          — deload

@Composable
fun App(viewModel: AppViewModel) {
    MaterialTheme(colorScheme = OpenJuggColorScheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val screen by viewModel.screen.collectAsState()
            when (val s = screen) {
                is AppViewModel.Screen.Setup ->
                    SetupScreen(onNext = viewModel::loadExercisesForSelection)

                is AppViewModel.Screen.LoadingExercises ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold)
                    }

                is AppViewModel.Screen.ExerciseSelection ->
                    ExerciseSelectionScreen(
                        exercises = s.exercises,
                        onBack = viewModel::backToSetup,
                        onGenerate = { selectedIds -> viewModel.generate(s.profile, selectedIds) }
                    )

                is AppViewModel.Screen.Generating ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Gold)
                            Text(
                                "Building your program…",
                                color = Muted3,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                is AppViewModel.Screen.ProgramView ->
                    ProgramScreen(program = s.program, onBack = viewModel::backToSetup)

                is AppViewModel.Screen.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${s.message}", color = Red)
                            if (s.canRetry) {
                                Button(onClick = viewModel::backToSetup) { Text("Back") }
                            }
                        }
                    }
            }
        }
    }
}
