package com.openjugg.desktop

import com.openjugg.domain.model.*
import com.openjugg.domain.repository.*
import com.openjugg.domain.usecase.GenerateProgramUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppViewModel : KoinComponent {

    private val userRepo: IUserRepository by inject()
    private val exerciseRepo: IExerciseRepository by inject()
    private val programRepo: IProgramRepository by inject()
    private val generateUseCase = GenerateProgramUseCase(userRepo, exerciseRepo, programRepo)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    sealed class Screen {
        object Setup : Screen()
        object LoadingExercises : Screen()
        data class ExerciseSelection(val profile: UserProfile, val exercises: List<Exercise>) : Screen()
        object Generating : Screen()
        data class ProgramView(val program: Program) : Screen()
        data class Error(val message: String, val canRetry: Boolean = true) : Screen()
    }

    private val _screen = MutableStateFlow<Screen>(Screen.Setup)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    fun loadExercisesForSelection(profile: UserProfile) {
        _screen.value = Screen.LoadingExercises
        scope.launch {
            try {
                exerciseRepo.seedIfEmpty()
                val exercises = withContext(Dispatchers.IO) { exerciseRepo.getAllExercisesOnce() }
                _screen.value = Screen.ExerciseSelection(profile, exercises)
            } catch (e: Exception) {
                _screen.value = Screen.Error(e.message ?: "Failed to load exercises")
            }
        }
    }

    fun generate(profile: UserProfile, allowedExerciseIds: Set<Long>) {
        _screen.value = Screen.Generating
        scope.launch {
            val result = withContext(Dispatchers.IO) { generateUseCase(profile, allowedExerciseIds) }
            _screen.value = result.fold(
                onSuccess = { Screen.ProgramView(it) },
                onFailure = { Screen.Error(it.message ?: "Generation failed") }
            )
        }
    }

    fun backToSetup() { _screen.value = Screen.Setup }
    fun backToExercises() {
        val cur = _screen.value
        if (cur is Screen.ProgramView || cur is Screen.Error) _screen.value = Screen.Setup
    }

    fun dispose() { scope.cancel() }
}
