package com.openjugg.domain.usecase

import com.openjugg.domain.model.LoggedSet
import com.openjugg.domain.repository.IProgramRepository

class GetExerciseHistoryUseCase(
    private val programRepo: IProgramRepository
) {
    suspend operator fun invoke(exerciseId: Long): List<LoggedSet> =
        programRepo.getExerciseHistory(exerciseId)
}
