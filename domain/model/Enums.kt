package com.openjugg.domain.model

// ─── User / Profile ──────────────────────────────────────

enum class Gender { MALE, FEMALE, OTHER }

enum class ExperienceLevel {
    BEGINNER,      // < 1 year
    INTERMEDIATE,  // 1-3 years
    ADVANCED,      // 3-7 years
    ELITE          // 7+ years
}

enum class TrainingGoal {
    POWERLIFTING,   // SBD focus
    POWERBUILDING,  // SBD + hypertrophy
    POWER_COMBO     // Hybrid phases
}

enum class WeakPoint {
    // Squat
    OUT_OF_THE_HOLE, MID_RANGE_SQUAT, LOCKOUT_SQUAT, UPPER_BACK_ROUNDING,

    // Bench
    OFF_THE_CHEST, MID_RANGE_BENCH, LOCKOUT_BENCH, BAR_PATH_INSTABILITY,

    // Deadlift
    OFF_THE_FLOOR, BELOW_THE_KNEE, LOCKOUT_DEADLIFT, GRIP_WEAKNESS
}

// ─── Programming ─────────────────────────────────────────

enum class PhaseType {
    HYPERTROPHY,   // High volume, moderate intensity (60-72% 1RM)
    STRENGTH,      // Moderate volume, high intensity (75-85% 1RM)
    PEAKING,       // Low volume, very high intensity (85-95%+ 1RM)
    DELOAD         // Recovery week (50-60% 1RM, halved volume)
}

enum class ExerciseCategory {
    PRIMARY,       // Competition SBD
    VARIATION,     // Close variation (e.g., pause squat)
    ACCESSORY      // Supplemental (e.g., leg press, rows)
}

enum class MuscleGroup {
    QUADS, HAMSTRINGS, GLUTES, LOWER_BACK, UPPER_BACK,
    CHEST, FRONT_DELTS, SIDE_DELTS, REAR_DELTS,
    TRICEPS, BICEPS, CORE, FOREARMS, CALVES
}

enum class LiftType {
    SQUAT, BENCH, DEADLIFT
}

enum class EquipmentType {
    BARBELL, DUMBBELL, CABLE, MACHINE, BODYWEIGHT, BAND, SPECIALTY_BAR
}