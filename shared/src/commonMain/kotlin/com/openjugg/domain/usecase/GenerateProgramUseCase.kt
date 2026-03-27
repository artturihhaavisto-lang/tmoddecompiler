package com.openjugg.domain.usecase

import com.openjugg.domain.engine.ProgramGenerator
import com.openjugg.domain.model.Program
import com.openjugg.domain.model.UserProfile
import com.openjugg.domain.repository.IExerciseRepository
import com.openjugg.domain.repository.IProgramRepository
import com.openjugg.domain.repository.IUserRepository

/**
 * Orchestrates the full program generation flow:
 *   1. Save user profile
 *   2. Seed exercises if needed
 *   3. Load exercise library
 *   4. Run the ProgramGenerator engine
 *   5. Persist the generated program
 *   6. Return the program
 */
class GenerateProgramUseCase(
    private val userRepo: IUserRepository,
    private val exerciseRepo: IExerciseRepository,
    private val programRepo: IProgramRepository
) {
    suspend operator fun invoke(profile: UserProfile, allowedExerciseIds: Set<Long> = emptySet()): Result<Program> {
        return runCatching {
            // 1. Save profile
            val userId = userRepo.saveProfile(profile)
            val savedProfile = profile.copy(id = userId)

            // 2. Seed exercise library if empty
            exerciseRepo.seedIfEmpty()

            // 3. Load exercise library
            val exercises = exerciseRepo.getAllExercisesOnce()

            // 4. Generate program
            val generator = ProgramGenerator(exercises)
            val program = generator.generate(savedProfile, allowedExerciseIds)

            // 5. Persist
            val programId = programRepo.saveFullProgram(program)

            // 6. Return hydrated program
            program.copy(id = programId, userId = userId)
        }
    }
}
