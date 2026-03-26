package com.openjugg.domain.model

import kotlinx.serialization.Serializable

// ─── User / Profile ──────────────────────────────────────

@Serializable enum class Gender { MALE, FEMALE, OTHER }

@Serializable
enum class ExperienceLevel {
    BEGINNER,      // < 1 year
    INTERMEDIATE,  // 1-3 years
    ADVANCED,      // 3-7 years
    ELITE          // 7+ years
}

@Serializable
enum class TrainingGoal {
    POWERLIFTING,   // SBD focus
    POWERBUILDING,  // SBD + hypertrophy
    POWER_COMBO     // Hybrid phases
}

@Serializable
enum class WeakPoint {
    // Squat
    OUT_OF_THE_HOLE, MID_RANGE_SQUAT, LOCKOUT_SQUAT, UPPER_BACK_ROUNDING,

    // Bench
    OFF_THE_CHEST, MID_RANGE_BENCH, LOCKOUT_BENCH, BAR_PATH_INSTABILITY,

    // Deadlift
    OFF_THE_FLOOR, BELOW_THE_KNEE, LOCKOUT_DEADLIFT, GRIP_WEAKNESS
}

// ─── Programming ─────────────────────────────────────────

@Serializable
enum class PhaseType {
    HYPERTROPHY,   // High volume, moderate intensity (60-72% 1RM)
    STRENGTH,      // Moderate volume, high intensity (75-85% 1RM)
    PEAKING,       // Low volume, very high intensity (85-95%+ 1RM)
    DELOAD         // Recovery week (50-60% 1RM, halved volume)
}

@Serializable
enum class ExerciseCategory {
    PRIMARY,       // Competition SBD
    VARIATION,     // Close variation (e.g., pause squat)
    ACCESSORY      // Supplemental (e.g., leg press, rows)
}

@Serializable
enum class MuscleGroup {
    QUADS, HAMSTRINGS, GLUTES, LOWER_BACK, UPPER_BACK,
    CHEST, FRONT_DELTS, SIDE_DELTS, REAR_DELTS,
    TRICEPS, BICEPS, CORE, FOREARMS, CALVES
}

@Serializable enum class LiftType { SQUAT, BENCH, DEADLIFT }

@Serializable
enum class EquipmentType {
    BARBELL, DUMBBELL, CABLE, MACHINE, BODYWEIGHT, BAND, SPECIALTY_BAR
}