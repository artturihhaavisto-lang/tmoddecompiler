package com.openjugg.data.db

import com.openjugg.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Centralized JSON instance used across the app.
 * ignoreUnknownKeys = true for forward compatibility.
 */
internal val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// ─── List<WeakPoint> ─────────────────────────────────────

@JvmName("weakPointListToJson")
internal fun List<WeakPoint>.toJsonString(): String =
    AppJson.encodeToString(this)

internal fun String.toWeakPointList(): List<WeakPoint> =
    AppJson.decodeFromString(this)

// ─── List<MuscleGroup> ───────────────────────────────────

@JvmName("muscleGroupListToJson")
internal fun List<MuscleGroup>.toJsonString(): String =
    AppJson.encodeToString(this)

internal fun String.toMuscleGroupList(): List<MuscleGroup> =
    AppJson.decodeFromString(this)

// ─── List<String> ────────────────────────────────────────

@JvmName("stringListToJson")
internal fun List<String>.toJsonString(): String =
    AppJson.encodeToString(this)

internal fun String.toStringList(): List<String> =
    AppJson.decodeFromString(this)

// ─── ReadinessInputs ─────────────────────────────────────

internal fun ReadinessInputs.toJsonString(): String =
    AppJson.encodeToString(this)

internal fun String.toReadinessInputs(): ReadinessInputs =
    AppJson.decodeFromString(this)
