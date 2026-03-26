package com.openjugg.domain.usecase

import com.openjugg.domain.engine.ReadinessAdjuster
import com.openjugg.domain.model.ReadinessInputs
import com.openjugg.domain.model.TrainingSession
import com.openjugg.domain.repository.IProgramRepository

/**
 * Takes the user's readiness ratings, returns the day's adjustments,
 * and persists the readiness data to the session.
 */
class AssessReadinessUseCase(
    private val programRepo: IProgramRepository? = null
) {
    data class ReadinessResult(
        val adjustments: ReadinessAdjuster.Adjustments,
        val updatedSession: TrainingSession
    )

    suspend operator fun invoke(
        session: TrainingSession,
        inputs: ReadinessInputs
    ): ReadinessResult {
        val adjustments = ReadinessAdjuster.assess(inputs)

        val updatedSession = session.copy(
            readinessScore = adjustments.compositeScore,
            readinessInputs = inputs
        )

        // Persist to DB if repo is provided
        programRepo?.updateSession(updatedSession)

        return ReadinessResult(
            adjustments = adjustments,
            updatedSession = updatedSession
        )
    }
}
