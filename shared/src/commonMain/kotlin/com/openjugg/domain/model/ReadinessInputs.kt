package com.openjugg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReadinessInputs(
    val sleep: Int,
    val nutrition: Int,
    val motivation: Int,
    val overallSoreness: Int,
    val quadSoreness: Int = 1,
    val hamstringSoreness: Int = 1,
    val chestSoreness: Int = 1,
    val backSoreness: Int = 1,
    val shoulderSoreness: Int = 1
)
