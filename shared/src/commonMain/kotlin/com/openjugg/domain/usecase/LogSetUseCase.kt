package com.openjugg.domain.usecase

import com.openjugg.domain.engine.RpeAutoRegulator
import com.openjugg.domain.model.LoggedSet
import com.openjugg.domain.model.ProgrammedExercise
import com.openjugg.domain.repository.IProgramRepository

/**
 * Logs a set, then runs the intra-session auto-regulation to produce
 * a recommendation for the next set.
 */
class LogSetUseCase(
    private val programRepo: IProgramRepository
) {
    data class LogSetResult(
        val savedSetId: Long,
        val nextSetDecision: RpeAutoRegulator.IntraSetDecision
    )

    suspend operator fun invoke(
        set: LoggedSet,
        programmedExercise: ProgrammedExercise,
        remainingSets: Int
    ): LogSetResult {
        // 1. Persist the set
        val id = programRepo.logSet(set)

        // 2. Auto-regulate based on logged RPE
        val decision = RpeAutoRegulator.decideAfterSet(
            loggedSet              = set,
            targetRpe              = programmedExercise.targetRpe,
            currentSuggestedWeight = programmedExercise.suggestedWeightKg,
            remainingSets          = remainingSets
        )

        // 3. Update the programmed exercise's suggested weight for the next set
        if (decision.adjustedWeightKg != programmedExercise.suggestedWeightKg) {
            programRepo.updateProgrammedExercise(
                programmedExercise.copy(suggestedWeightKg = decision.adjustedWeightKg)
            )
        }

        return LogSetResult(savedSetId = id, nextSetDecision = decision)
    }
}
