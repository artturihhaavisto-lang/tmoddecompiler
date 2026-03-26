package com.openjugg.domain.usecase

import com.openjugg.domain.model.Program
import com.openjugg.domain.repository.IProgramRepository

class GetActiveProgramUseCase(
    private val programRepo: IProgramRepository
) {
    suspend operator fun invoke(userId: Long): Program? =
        programRepo.getActiveProgram(userId)
}
