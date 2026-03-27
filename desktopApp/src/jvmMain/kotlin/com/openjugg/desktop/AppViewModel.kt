package com.openjugg.desktop

import com.openjugg.domain.model.Program
import com.openjugg.domain.model.UserProfile
import com.openjugg.domain.repository.IExerciseRepository
import com.openjugg.domain.repository.IProgramRepository
import com.openjugg.domain.repository.IUserRepository
import com.openjugg.domain.usecase.GenerateProgramUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        object Generating : Screen()
        data class ProgramView(val program: Program) : Screen()
        data class Error(val message: String) : Screen()
    }

    private val _screen = MutableStateFlow<Screen>(Screen.Setup)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    fun generate(profile: UserProfile) {
        _screen.value = Screen.Generating
        scope.launch {
            val result = withContext(Dispatchers.IO) { generateUseCase(profile) }
            _screen.value = result.fold(
                onSuccess = { Screen.ProgramView(it) },
                onFailure = { Screen.Error(it.message ?: "Program generation failed") }
            )
        }
    }

    fun backToSetup() {
        _screen.value = Screen.Setup
    }

    fun dispose() {
        scope.cancel()
    }
}
