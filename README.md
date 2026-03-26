This is a big one. Below is the full local backend, data layer, domain models, seed data for SBD (Squat/Bench/Deadlift), and the complete programming engine with all algorithms. Every file is annotated with its path.

---

# 📁 Project Structure Overview

```
com.openjugg/
├── data/
│   ├── local/
│   │   ├── entity/          ← Room Entities
│   │   ├── dao/             ← Room DAOs
│   │   ├── converter/       ← Type Converters
│   │   └── OpenJuggDatabase.kt
│   ├── repository/          ← Repo Implementations
│   └── seed/                ← Exercise seed data
├── domain/
│   ├── model/               ← Pure domain models
│   ├── engine/              ← 🧠 ALL algorithms live here
│   └── repository/          ← Repo interfaces
└── di/                      ← Hilt modules
```

---

# 1. DOMAIN MODELS (Pure Kotlin — No Android Dependencies)

## `domain/model/Enums.kt`
```kotlin
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
```

## `domain/model/UserProfile.kt`
```kotlin
package com.openjugg.domain.model

data class UserProfile(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: Gender,
    val bodyweightKg: Float,
    val heightCm: Float,
    val experienceLevel: ExperienceLevel,
    val trainingGoal: TrainingGoal,
    val daysPerWeek: Int,                    // 3-6
    val meetDate: Long? = null,              // epoch millis, nullable
    val squatMax: Float,                     // 1RM in kg
    val benchMax: Float,
    val deadliftMax: Float,
    val squatWeakPoints: List<WeakPoint> = emptyList(),
    val benchWeakPoints: List<WeakPoint> = emptyList(),
    val deadliftWeakPoints: List<WeakPoint> = emptyList()
)
```

## `domain/model/Exercise.kt`
```kotlin
package com.openjugg.domain.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val liftType: LiftType,
    val category: ExerciseCategory,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: EquipmentType = EquipmentType.BARBELL,
    val description: String = "",
    val cues: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val addressesWeakPoints: List<WeakPoint> = emptyList()
)
```

## `domain/model/Program.kt`
```kotlin
package com.openjugg.domain.model

data class Program(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val goal: TrainingGoal,
    val startDate: Long,    // epoch millis
    val endDate: Long,
    val phases: List<Phase> = emptyList()
)

data class Phase(
    val id: Long = 0,
    val programId: Long = 0,
    val type: PhaseType,
    val weekCount: Int,
    val order: Int,         // 0-indexed order in program
    val weeks: List<TrainingWeek> = emptyList()
)

data class TrainingWeek(
    val id: Long = 0,
    val phaseId: Long = 0,
    val weekNumber: Int,    // 1-indexed within the phase
    val sessions: List<TrainingSession> = emptyList()
)

data class TrainingSession(
    val id: Long = 0,
    val weekId: Long = 0,
    val dayOfWeek: Int,     // 1=Mon ... 7=Sun
    val label: String = "",  // e.g. "Squat Day", "Bench Day"
    val readinessScore: Float? = null,
    val readinessInputs: ReadinessInputs? = null,
    val sessionDifficulty: Int? = null,  // 1-10 post-workout
    val completed: Boolean = false,
    val exercises: List<ProgrammedExercise> = emptyList()
)

data class ReadinessInputs(
    val sleep: Int,          // 1-5
    val nutrition: Int,      // 1-5
    val motivation: Int,     // 1-5
    val overallSoreness: Int, // 1-5 (1=fresh, 5=destroyed)
    val quadSoreness: Int = 1,
    val hamstringSoreness: Int = 1,
    val chestSoreness: Int = 1,
    val backSoreness: Int = 1,
    val shoulderSoreness: Int = 1
)

data class ProgrammedExercise(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long,
    val exerciseName: String = "",
    val order: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetRpe: Float,          // e.g. 7.0, 8.0, 9.0
    val suggestedWeightKg: Float,
    val isAmrap: Boolean = false,  // last set is AMRAP?
    val loggedSets: List<LoggedSet> = emptyList()
)

data class LoggedSet(
    val id: Long = 0,
    val programmedExerciseId: Long = 0,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rpe: Float,         // actual logged RPE
    val skipped: Boolean = false
)
```

---

# 2. DATA LAYER (Room Database)

## `data/local/converter/Converters.kt`
```kotlin
package com.openjugg.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openjugg.domain.model.*

class Converters {

    private val gson = Gson()

    // ─── Generic List<String> ────────────────────────────

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    // ─── List<WeakPoint> ─────────────────────────────────

    @TypeConverter
    fun fromWeakPointList(value: List<WeakPoint>): String = gson.toJson(value)

    @TypeConverter
    fun toWeakPointList(value: String): List<WeakPoint> =
        gson.fromJson(value, object : TypeToken<List<WeakPoint>>() {}.type)

    // ─── List<MuscleGroup> ───────────────────────────────

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String = gson.toJson(value)

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> =
        gson.fromJson(value, object : TypeToken<List<MuscleGroup>>() {}.type)

    // ─── ReadinessInputs (embedded as JSON) ──────────────

    @TypeConverter
    fun fromReadinessInputs(value: ReadinessInputs?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toReadinessInputs(value: String?): ReadinessInputs? =
        value?.let { gson.fromJson(it, ReadinessInputs::class.java) }

    // ─── Enums ───────────────────────────────────────────

    @TypeConverter fun fromGender(v: Gender): String = v.name
    @TypeConverter fun toGender(v: String): Gender = Gender.valueOf(v)

    @TypeConverter fun fromExpLevel(v: ExperienceLevel): String = v.name
    @TypeConverter fun toExpLevel(v: String): ExperienceLevel = ExperienceLevel.valueOf(v)

    @TypeConverter fun fromGoal(v: TrainingGoal): String = v.name
    @TypeConverter fun toGoal(v: String): TrainingGoal = TrainingGoal.valueOf(v)

    @TypeConverter fun fromPhaseType(v: PhaseType): String = v.name
    @TypeConverter fun toPhaseType(v: String): PhaseType = PhaseType.valueOf(v)

    @TypeConverter fun fromCategory(v: ExerciseCategory): String = v.name
    @TypeConverter fun toCategory(v: String): ExerciseCategory = ExerciseCategory.valueOf(v)

    @TypeConverter fun fromLiftType(v: LiftType): String = v.name
    @TypeConverter fun toLiftType(v: String): LiftType = LiftType.valueOf(v)

    @TypeConverter fun fromEquipment(v: EquipmentType): String = v.name
    @TypeConverter fun toEquipment(v: String): EquipmentType = EquipmentType.valueOf(v)
}
```

## `data/local/entity/UserProfileEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.*

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: Gender,
    val bodyweightKg: Float,
    val heightCm: Float,
    val experienceLevel: ExperienceLevel,
    val trainingGoal: TrainingGoal,
    val daysPerWeek: Int,
    val meetDate: Long?,
    val squatMax: Float,
    val benchMax: Float,
    val deadliftMax: Float,
    val squatWeakPoints: List<WeakPoint>,
    val benchWeakPoints: List<WeakPoint>,
    val deadliftWeakPoints: List<WeakPoint>
)
```

## `data/local/entity/ExerciseEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.*

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val liftType: LiftType,
    val category: ExerciseCategory,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: EquipmentType,
    val description: String,
    val cues: List<String>,
    val commonMistakes: List<String>,
    val addressesWeakPoints: List<WeakPoint>
)
```

## `data/local/entity/ProgramEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openjugg.domain.model.TrainingGoal

@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val goal: TrainingGoal,
    val startDate: Long,
    val endDate: Long
)
```

## `data/local/entity/PhaseEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.openjugg.domain.model.PhaseType

@Entity(
    tableName = "phases",
    foreignKeys = [ForeignKey(
        entity = ProgramEntity::class,
        parentColumns = ["id"],
        childColumns = ["programId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PhaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val type: PhaseType,
    val weekCount: Int,
    val order: Int
)
```

## `data/local/entity/TrainingWeekEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_weeks",
    foreignKeys = [ForeignKey(
        entity = PhaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["phaseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TrainingWeekEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phaseId: Long,
    val weekNumber: Int
)
```

## `data/local/entity/TrainingSessionEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.openjugg.domain.model.ReadinessInputs

@Entity(
    tableName = "training_sessions",
    foreignKeys = [ForeignKey(
        entity = TrainingWeekEntity::class,
        parentColumns = ["id"],
        childColumns = ["weekId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekId: Long,
    val dayOfWeek: Int,
    val label: String,
    val readinessScore: Float?,
    val readinessInputs: ReadinessInputs?,
    val sessionDifficulty: Int?,
    val completed: Boolean
)
```

## `data/local/entity/ProgrammedExerciseEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "programmed_exercises",
    foreignKeys = [ForeignKey(
        entity = TrainingSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ProgrammedExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetRpe: Float,
    val suggestedWeightKg: Float,
    val isAmrap: Boolean
)
```

## `data/local/entity/LoggedSetEntity.kt`
```kotlin
package com.openjugg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "logged_sets",
    foreignKeys = [ForeignKey(
        entity = ProgrammedExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["programmedExerciseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoggedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programmedExerciseId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rpe: Float,
    val skipped: Boolean
)
```

---

## DAOs

## `data/local/dao/UserProfileDao.kt`
```kotlin
package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY id DESC LIMIT 1")
    fun getActiveProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY id DESC LIMIT 1")
    suspend fun getActiveProfileOnce(): UserProfileEntity?

    @Delete
    suspend fun delete(profile: UserProfileEntity)
}
```

## `data/local/dao/ExerciseDao.kt`
```kotlin
package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.ExerciseEntity
import com.openjugg.domain.model.ExerciseCategory
import com.openjugg.domain.model.LiftType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Query("SELECT * FROM exercises")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllOnce(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE liftType = :liftType")
    suspend fun getByLiftType(liftType: LiftType): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE liftType = :liftType AND category = :category")
    suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
```

## `data/local/dao/ProgramDao.kt`
```kotlin
package com.openjugg.data.local.dao

import androidx.room.*
import com.openjugg.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    // ─── Program ─────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity): Long

    @Query("SELECT * FROM programs WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveProgramForUser(userId: Long): ProgramEntity?

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgramById(id: Long): ProgramEntity?

    // ─── Phase ───────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: PhaseEntity): Long

    @Query("SELECT * FROM phases WHERE programId = :programId ORDER BY `order`")
    suspend fun getPhasesForProgram(programId: Long): List<PhaseEntity>

    // ─── Training Week ───────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeek(week: TrainingWeekEntity): Long

    @Query("SELECT * FROM training_weeks WHERE phaseId = :phaseId ORDER BY weekNumber")
    suspend fun getWeeksForPhase(phaseId: Long): List<TrainingWeekEntity>

    // ─── Training Session ────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSessionEntity): Long

    @Update
    suspend fun updateSession(session: TrainingSessionEntity)

    @Query("SELECT * FROM training_sessions WHERE weekId = :weekId ORDER BY dayOfWeek")
    suspend fun getSessionsForWeek(weekId: Long): List<TrainingSessionEntity>

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TrainingSessionEntity?

    // ─── Programmed Exercise ─────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammedExercise(exercise: ProgrammedExerciseEntity): Long

    @Update
    suspend fun updateProgrammedExercise(exercise: ProgrammedExerciseEntity)

    @Query("SELECT * FROM programmed_exercises WHERE sessionId = :sessionId ORDER BY `order`")
    suspend fun getExercisesForSession(sessionId: Long): List<ProgrammedExerciseEntity>

    // ─── Logged Sets ─────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedSet(set: LoggedSetEntity): Long

    @Query("SELECT * FROM logged_sets WHERE programmedExerciseId = :progExId ORDER BY setNumber")
    suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSetEntity>

    @Query("""
        SELECT ls.* FROM logged_sets ls
        INNER JOIN programmed_exercises pe ON ls.programmedExerciseId = pe.id
        WHERE pe.exerciseId = :exerciseId
        ORDER BY ls.id DESC
    """)
    suspend fun getHistoryForExercise(exerciseId: Long): List<LoggedSetEntity>

    // ─── Queries for the adaptation engine ───────────────

    /** Get all logged sets for a given session (across all exercises) */
    @Query("""
        SELECT ls.* FROM logged_sets ls
        INNER JOIN programmed_exercises pe ON ls.programmedExerciseId = pe.id
        WHERE pe.sessionId = :sessionId
        ORDER BY pe.`order`, ls.setNumber
    """)
    suspend fun getAllLoggedSetsForSession(sessionId: Long): List<LoggedSetEntity>

    /** Get all completed sessions for a given week */
    @Query("SELECT * FROM training_sessions WHERE weekId = :weekId AND completed = 1")
    suspend fun getCompletedSessionsForWeek(weekId: Long): List<TrainingSessionEntity>
}
```

---

## Database

## `data/local/OpenJuggDatabase.kt`
```kotlin
package com.openjugg.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openjugg.data.local.converter.Converters
import com.openjugg.data.local.dao.*
import com.openjugg.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        ExerciseEntity::class,
        ProgramEntity::class,
        PhaseEntity::class,
        TrainingWeekEntity::class,
        TrainingSessionEntity::class,
        ProgrammedExerciseEntity::class,
        LoggedSetEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OpenJuggDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
}
```

---

# 3. EXERCISE SEED DATA

## `data/seed/ExerciseSeedData.kt`
```kotlin
package com.openjugg.data.seed

import com.openjugg.domain.model.*

/**
 * Seed data for the three competition lifts + key variations + accessories.
 * Each exercise is tagged with the weak points it addresses so the
 * ExerciseSelector algorithm can make intelligent choices.
 */
object ExerciseSeedData {

    fun getAll(): List<Exercise> = squatExercises + benchExercises + deadliftExercises

    // ══════════════════════════════════════════════════════
    //  S Q U A T
    // ══════════════════════════════════════════════════════

    val squatExercises = listOf(

        // ─── Primary ─────────────────────────────────────
        Exercise(
            id = 1,
            name = "Competition Back Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.PRIMARY,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK, MuscleGroup.CORE),
            equipment = EquipmentType.BARBELL,
            description = "Standard low-bar or high-bar back squat performed to competition depth (hip crease below top of knee).",
            cues = listOf(
                "Brace core hard before descent",
                "Push knees out over toes",
                "Drive up through the whole foot",
                "Keep chest up and upper back tight"
            ),
            commonMistakes = listOf(
                "Cutting depth high",
                "Caving knees inward",
                "Excessive forward lean",
                "Losing brace at the bottom"
            )
        ),

        // ─── Variations ──────────────────────────────────
        Exercise(
            id = 2,
            name = "Pause Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.LOWER_BACK),
            equipment = EquipmentType.BARBELL,
            description = "Back squat with a 2-3 second pause in the hole. Builds strength out of the bottom and reinforces proper positioning.",
            cues = listOf("Controlled descent", "Hold position — no relaxing at the bottom", "Explode up after the count"),
            commonMistakes = listOf("Bouncing instead of pausing", "Relaxing the brace during the pause"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE)
        ),

        Exercise(
            id = 3,
            name = "Tempo Squat (3-1-0)",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.CORE),
            equipment = EquipmentType.BARBELL,
            description = "Squat with a 3-second eccentric, 1-second pause at the bottom, and normal concentric. Builds positional strength and control.",
            cues = listOf("Count 3 full seconds on the way down", "Stay tight throughout"),
            commonMistakes = listOf("Speeding up the eccentric", "Losing upper back tightness"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE, WeakPoint.UPPER_BACK_ROUNDING)
        ),

        Exercise(
            id = 4,
            name = "Pin Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.CORE),
            equipment = EquipmentType.BARBELL,
            description = "Squat from a dead stop on safety pins set at parallel or just below. Eliminates the stretch-shortening cycle.",
            cues = listOf("Settle the bar fully on the pins", "Reset your brace", "Drive explosively"),
            commonMistakes = listOf("Setting pins too high", "Bouncing off the pins"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE, WeakPoint.MID_RANGE_SQUAT)
        ),

        Exercise(
            id = 5,
            name = "Front Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.QUADS),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.CORE, MuscleGroup.GLUTES),
            equipment = EquipmentType.BARBELL,
            description = "Barbell held in front rack position. Demands more upright torso, heavily loads quads and upper back.",
            cues = listOf("Elbows high", "Sit straight down", "Drive elbows up out of the hole"),
            commonMistakes = listOf("Letting elbows drop", "Excessive forward lean"),
            addressesWeakPoints = listOf(WeakPoint.UPPER_BACK_ROUNDING, WeakPoint.MID_RANGE_SQUAT)
        ),

        // ─── Accessories ─────────────────────────────────
        Exercise(
            id = 6,
            name = "Leg Press",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            equipment = EquipmentType.MACHINE,
            description = "Machine-based quad/glute builder. Useful for accumulating volume without spinal loading.",
            cues = listOf("Full range of motion", "Don't lock out aggressively"),
            commonMistakes = listOf("Partial reps", "Letting lower back round off the pad"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE, WeakPoint.MID_RANGE_SQUAT)
        ),

        Exercise(
            id = 7,
            name = "Bulgarian Split Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.CORE),
            equipment = EquipmentType.DUMBBELL,
            description = "Single-leg squat with rear foot elevated. Addresses imbalances and builds unilateral strength.",
            cues = listOf("Keep torso upright", "Front shin roughly vertical at the bottom"),
            commonMistakes = listOf("Standing too close to the bench", "Leaning forward excessively"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE)
        ),

        Exercise(
            id = 8,
            name = "Belt Squat",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            equipment = EquipmentType.MACHINE,
            description = "Squat loaded through a belt at the hips. Zero spinal compression, excellent for volume accumulation.",
            cues = listOf("Squat to full depth", "Drive through the whole foot"),
            commonMistakes = listOf("Cutting depth short"),
            addressesWeakPoints = listOf(WeakPoint.OUT_OF_THE_HOLE, WeakPoint.MID_RANGE_SQUAT)
        ),

        Exercise(
            id = 9,
            name = "Leg Extension",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.QUADS),
            equipment = EquipmentType.MACHINE,
            description = "Isolation exercise for quads. Useful for hypertrophy phases and low-fatigue quad volume.",
            cues = listOf("Full extension at the top", "Controlled eccentric"),
            commonMistakes = listOf("Using momentum", "Partial range of motion"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_SQUAT)
        ),

        Exercise(
            id = 10,
            name = "Good Morning",
            liftType = LiftType.SQUAT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK),
            secondaryMuscles = listOf(MuscleGroup.GLUTES),
            equipment = EquipmentType.BARBELL,
            description = "Hip hinge with barbell on back. Strengthens the posterior chain to resist forward lean in the squat.",
            cues = listOf("Soft knee bend", "Hinge at hips", "Keep back flat"),
            commonMistakes = listOf("Rounding the back", "Squatting the weight up"),
            addressesWeakPoints = listOf(WeakPoint.UPPER_BACK_ROUNDING)
        )
    )

    // ══════════════════════════════════════════════════════
    //  B E N C H   P R E S S
    // ══════════════════════════════════════════════════════

    val benchExercises = listOf(

        Exercise(
            id = 11,
            name = "Competition Bench Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.PRIMARY,
            primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            secondaryMuscles = listOf(MuscleGroup.FRONT_DELTS),
            equipment = EquipmentType.BARBELL,
            description = "Flat barbell bench press with a pause on chest per competition rules.",
            cues = listOf(
                "Retract and depress scapulae",
                "Leg drive into the floor",
                "Touch at the sternum/lower chest",
                "Press back toward the face (J-curve bar path)"
            ),
            commonMistakes = listOf(
                "Flaring elbows at 90°",
                "Losing upper back tightness",
                "Bouncing off chest",
                "Butt coming off the bench"
            )
        ),

        Exercise(
            id = 12,
            name = "Paused Bench Press (Long Pause)",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            secondaryMuscles = listOf(MuscleGroup.FRONT_DELTS),
            equipment = EquipmentType.BARBELL,
            description = "Bench press with 2-3 second pause on chest. Builds starting strength off the chest.",
            cues = listOf("Dead stop on chest", "Maintain tightness during pause", "Explosive press"),
            commonMistakes = listOf("Sinking the bar into chest", "Relaxing during pause"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_CHEST)
        ),

        Exercise(
            id = 13,
            name = "Close-Grip Bench Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST),
            secondaryMuscles = listOf(MuscleGroup.FRONT_DELTS),
            equipment = EquipmentType.BARBELL,
            description = "Bench press with hands ~shoulder width apart. Shifts emphasis to triceps for lockout strength.",
            cues = listOf("Elbows closer to body", "Same back tightness as competition bench"),
            commonMistakes = listOf("Grip too narrow causing wrist pain", "Flaring elbows"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH, WeakPoint.MID_RANGE_BENCH)
        ),

        Exercise(
            id = 14,
            name = "Spoto Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            equipment = EquipmentType.BARBELL,
            description = "Bench press stopping 1-2 inches off the chest. Eliminates the touch-and-go reflex and builds mid-range strength.",
            cues = listOf("Hover the bar 1-2 inches above chest", "Hold for 1 second", "Press through"),
            commonMistakes = listOf("Touching the chest", "Inconsistent hover height"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_CHEST, WeakPoint.MID_RANGE_BENCH)
        ),

        Exercise(
            id = 15,
            name = "Floor Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST),
            equipment = EquipmentType.BARBELL,
            description = "Bench press lying on the floor. Limits ROM, focuses on lockout and tricep strength.",
            cues = listOf("Let upper arms rest briefly on floor", "No leg drive"),
            commonMistakes = listOf("Bouncing arms off the floor"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH, WeakPoint.MID_RANGE_BENCH)
        ),

        Exercise(
            id = 16,
            name = "Dumbbell Bench Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.CHEST),
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.FRONT_DELTS),
            equipment = EquipmentType.DUMBBELL,
            description = "Flat dumbbell bench press. Greater ROM and addresses left-right imbalances.",
            cues = listOf("Control the weight at the bottom", "Press to full lockout"),
            commonMistakes = listOf("Dumbbells drifting apart", "Partial range of motion"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_CHEST, WeakPoint.BAR_PATH_INSTABILITY)
        ),

        Exercise(
            id = 17,
            name = "Overhead Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.FRONT_DELTS, MuscleGroup.TRICEPS),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.CORE),
            equipment = EquipmentType.BARBELL,
            description = "Standing barbell overhead press. Builds pressing strength and shoulder stability.",
            cues = listOf("Brace core", "Press straight up", "Move head through at the top"),
            commonMistakes = listOf("Excessive back lean", "Not finishing overhead"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH)
        ),

        Exercise(
            id = 18,
            name = "Dip (Weighted)",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST),
            secondaryMuscles = listOf(MuscleGroup.FRONT_DELTS),
            equipment = EquipmentType.BODYWEIGHT,
            description = "Parallel bar dips with added weight. Builds chest and tricep mass.",
            cues = listOf("Lean slightly forward for chest emphasis", "Full ROM"),
            commonMistakes = listOf("Partial range of motion", "Excessive shoulder internal rotation"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH, WeakPoint.OFF_THE_CHEST)
        ),

        Exercise(
            id = 19,
            name = "Tricep Pushdown",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = EquipmentType.CABLE,
            description = "Cable tricep pushdown. Isolation exercise for tricep hypertrophy.",
            cues = listOf("Elbows pinned to sides", "Full extension", "Controlled return"),
            commonMistakes = listOf("Using body momentum", "Elbows flaring"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_BENCH)
        ),

        Exercise(
            id = 20,
            name = "Incline Dumbbell Press",
            liftType = LiftType.BENCH,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.FRONT_DELTS),
            secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = EquipmentType.DUMBBELL,
            description = "Incline (30-45°) dumbbell press. Targets upper chest and builds bench press stability.",
            cues = listOf("Control the dumbbells through full ROM", "Press to full lockout"),
            commonMistakes = listOf("Setting bench too steep", "Flaring elbows wide"),
            addressesWeakPoints = listOf(WeakPoint.BAR_PATH_INSTABILITY, WeakPoint.OFF_THE_CHEST)
        )
    )

    // ══════════════════════════════════════════════════════
    //  D E A D L I F T
    // ══════════════════════════════════════════════════════

    val deadliftExercises = listOf(

        Exercise(
            id = 21,
            name = "Competition Deadlift (Conventional)",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.PRIMARY,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK),
            secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.UPPER_BACK, MuscleGroup.FOREARMS),
            equipment = EquipmentType.BARBELL,
            description = "Standard conventional deadlift from the floor. Feet roughly hip-width, hands outside knees.",
            cues = listOf(
                "Push the floor away",
                "Keep the bar against your body",
                "Lockout hips and knees together",
                "Big breath and brace before each rep"
            ),
            commonMistakes = listOf(
                "Hips shooting up first",
                "Rounding the lower back",
                "Hitching at the top",
                "Bar drifting away from body"
            )
        ),

        Exercise(
            id = 22,
            name = "Deficit Deadlift",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.LOWER_BACK),
            equipment = EquipmentType.BARBELL,
            description = "Deadlift standing on a 1-3 inch platform. Increases ROM and builds strength off the floor.",
            cues = listOf("Same mechanics as regular deadlift", "Stay patient off the floor"),
            commonMistakes = listOf("Deficit too large causing form breakdown", "Rounding lower back"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_FLOOR)
        ),

        Exercise(
            id = 23,
            name = "Paused Deadlift (Below Knee)",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.GLUTES),
            equipment = EquipmentType.BARBELL,
            description = "Deadlift with a 2-3 second pause just below the knee. Builds positional strength and reinforces back tightness.",
            cues = listOf("Pause when bar reaches mid-shin to just below knee", "Maintain back angle", "Stay tight"),
            commonMistakes = listOf("Letting hips rise during the pause", "Losing upper back tightness"),
            addressesWeakPoints = listOf(WeakPoint.BELOW_THE_KNEE, WeakPoint.OFF_THE_FLOOR)
        ),

        Exercise(
            id = 24,
            name = "Block Pull / Rack Pull",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK),
            secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.FOREARMS),
            equipment = EquipmentType.BARBELL,
            description = "Deadlift from blocks or rack pins set at knee height. Overloads lockout.",
            cues = listOf("Hips through at the top", "Squeeze glutes hard at lockout"),
            commonMistakes = listOf("Hitching", "Hyperextending at the top"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_DEADLIFT, WeakPoint.BELOW_THE_KNEE)
        ),

        Exercise(
            id = 25,
            name = "Romanian Deadlift (RDL)",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.LOWER_BACK),
            equipment = EquipmentType.BARBELL,
            description = "Hip hinge with slight knee bend, bar lowered to mid-shin. Top-down deadlift variation for hamstring hypertrophy.",
            cues = listOf("Hinge at hips", "Feel the stretch in hamstrings", "Keep bar close"),
            commonMistakes = listOf("Rounding the back", "Bending knees too much"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_FLOOR, WeakPoint.BELOW_THE_KNEE)
        ),

        Exercise(
            id = 26,
            name = "Sumo Deadlift",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.VARIATION,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.LOWER_BACK),
            equipment = EquipmentType.BARBELL,
            description = "Wide-stance deadlift with hands inside the knees. Alternative competition stance or accessory.",
            cues = listOf("Push knees out", "Open hips", "Chest up", "Push the floor away"),
            commonMistakes = listOf("Hips rising too fast", "Knees caving"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_FLOOR)
        ),

        Exercise(
            id = 27,
            name = "Barbell Row",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.UPPER_BACK),
            secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK, MuscleGroup.REAR_DELTS),
            equipment = EquipmentType.BARBELL,
            description = "Bent-over barbell row. Builds the upper back strength needed to maintain position in the deadlift.",
            cues = listOf("Hinge to ~45°", "Pull to lower chest/upper abdomen", "Squeeze shoulder blades"),
            commonMistakes = listOf("Using too much body English", "Standing too upright"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_DEADLIFT, WeakPoint.BELOW_THE_KNEE)
        ),

        Exercise(
            id = 28,
            name = "Hip Thrust",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS),
            equipment = EquipmentType.BARBELL,
            description = "Barbell hip thrust for glute strength and lockout power.",
            cues = listOf("Drive through heels", "Squeeze glutes at the top", "Chin tucked"),
            commonMistakes = listOf("Hyperextending the lower back", "Not reaching full hip extension"),
            addressesWeakPoints = listOf(WeakPoint.LOCKOUT_DEADLIFT)
        ),

        Exercise(
            id = 29,
            name = "Lat Pulldown",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.UPPER_BACK),
            secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = EquipmentType.CABLE,
            description = "Cable lat pulldown. Builds lat strength for keeping the bar close during the pull.",
            cues = listOf("Pull to upper chest", "Squeeze lats at the bottom", "Control the return"),
            commonMistakes = listOf("Pulling behind the neck", "Using body momentum"),
            addressesWeakPoints = listOf(WeakPoint.OFF_THE_FLOOR, WeakPoint.BELOW_THE_KNEE)
        ),

        Exercise(
            id = 30,
            name = "Farmer's Walk",
            liftType = LiftType.DEADLIFT,
            category = ExerciseCategory.ACCESSORY,
            primaryMuscles = listOf(MuscleGroup.FOREARMS, MuscleGroup.CORE),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK),
            equipment = EquipmentType.DUMBBELL,
            description = "Loaded carry for grip endurance and overall stability.",
            cues = listOf("Stand tall", "Short quick steps", "Squeeze the handles hard"),
            commonMistakes = listOf("Leaning to one side", "Taking strides too long"),
            addressesWeakPoints = listOf(WeakPoint.GRIP_WEAKNESS, WeakPoint.LOCKOUT_DEADLIFT)
        )
    )
}
```

## `data/seed/ExerciseDatabaseSeeder.kt`
```kotlin
package com.openjugg.data.seed

import com.openjugg.data.local.dao.ExerciseDao
import com.openjugg.data.local.entity.ExerciseEntity
import com.openjugg.domain.model.Exercise

/**
 * Seeds the Room database with the initial exercise library on first launch.
 */
class ExerciseDatabaseSeeder(private val exerciseDao: ExerciseDao) {

    suspend fun seedIfEmpty() {
        if (exerciseDao.count() > 0) return

        val entities = ExerciseSeedData.getAll().map { it.toEntity() }
        exerciseDao.insertAll(entities)
    }

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        name = name,
        liftType = liftType,
        category = category,
        primaryMuscles = primaryMuscles,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        description = description,
        cues = cues,
        commonMistakes = commonMistakes,
        addressesWeakPoints = addressesWeakPoints
    )
}
```

---

# 4. ENTITY ↔ DOMAIN MAPPERS

## `data/local/mapper/Mappers.kt`
```kotlin
package com.openjugg.data.local.mapper

import com.openjugg.data.local.entity.*
import com.openjugg.domain.model.*

// ─── UserProfile ─────────────────────────────────────────

fun UserProfileEntity.toDomain() = UserProfile(
    id = id, name = name, age = age, gender = gender,
    bodyweightKg = bodyweightKg, heightCm = heightCm,
    experienceLevel = experienceLevel, trainingGoal = trainingGoal,
    daysPerWeek = daysPerWeek, meetDate = meetDate,
    squatMax = squatMax, benchMax = benchMax, deadliftMax = deadliftMax,
    squatWeakPoints = squatWeakPoints, benchWeakPoints = benchWeakPoints,
    deadliftWeakPoints = deadliftWeakPoints
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id, name = name, age = age, gender = gender,
    bodyweightKg = bodyweightKg, heightCm = heightCm,
    experienceLevel = experienceLevel, trainingGoal = trainingGoal,
    daysPerWeek = daysPerWeek, meetDate = meetDate,
    squatMax = squatMax, benchMax = benchMax, deadliftMax = deadliftMax,
    squatWeakPoints = squatWeakPoints, benchWeakPoints = benchWeakPoints,
    deadliftWeakPoints = deadliftWeakPoints
)

// ─── Exercise ────────────────────────────────────────────

fun ExerciseEntity.toDomain() = Exercise(
    id = id, name = name, liftType = liftType, category = category,
    primaryMuscles = primaryMuscles, secondaryMuscles = secondaryMuscles,
    equipment = equipment, description = description, cues = cues,
    commonMistakes = commonMistakes, addressesWeakPoints = addressesWeakPoints
)

// ─── LoggedSet ───────────────────────────────────────────

fun LoggedSetEntity.toDomain() = LoggedSet(
    id = id, programmedExerciseId = programmedExerciseId,
    setNumber = setNumber, weightKg = weightKg,
    reps = reps, rpe = rpe, skipped = skipped
)

fun LoggedSet.toEntity() = LoggedSetEntity(
    id = id, programmedExerciseId = programmedExerciseId,
    setNumber = setNumber, weightKg = weightKg,
    reps = reps, rpe = rpe, skipped = skipped
)

// ─── ProgrammedExercise ──────────────────────────────────

fun ProgrammedExerciseEntity.toDomain(loggedSets: List<LoggedSet> = emptyList()) = ProgrammedExercise(
    id = id, sessionId = sessionId, exerciseId = exerciseId,
    exerciseName = exerciseName, order = order,
    targetSets = targetSets, targetReps = targetReps,
    targetRpe = targetRpe, suggestedWeightKg = suggestedWeightKg,
    isAmrap = isAmrap, loggedSets = loggedSets
)

fun ProgrammedExercise.toEntity() = ProgrammedExerciseEntity(
    id = id, sessionId = sessionId, exerciseId = exerciseId,
    exerciseName = exerciseName, order = order,
    targetSets = targetSets, targetReps = targetReps,
    targetRpe = targetRpe, suggestedWeightKg = suggestedWeightKg,
    isAmrap = isAmrap
)

// ─── TrainingSession ─────────────────────────────────────

fun TrainingSessionEntity.toDomain(exercises: List<ProgrammedExercise> = emptyList()) = TrainingSession(
    id = id, weekId = weekId, dayOfWeek = dayOfWeek, label = label,
    readinessScore = readinessScore, readinessInputs = readinessInputs,
    sessionDifficulty = sessionDifficulty, completed = completed,
    exercises = exercises
)

fun TrainingSession.toEntity() = TrainingSessionEntity(
    id = id, weekId = weekId, dayOfWeek = dayOfWeek, label = label,
    readinessScore = readinessScore, readinessInputs = readinessInputs,
    sessionDifficulty = sessionDifficulty, completed = completed
)
```

---

# 5. REPOSITORY LAYER

## `domain/repository/IUserRepository.kt`
```kotlin
package com.openjugg.domain.repository

import com.openjugg.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getActiveProfile(): Flow<UserProfile?>
    suspend fun getActiveProfileOnce(): UserProfile?
    suspend fun saveProfile(profile: UserProfile): Long
    suspend fun updateProfile(profile: UserProfile)
}
```

## `domain/repository/IExerciseRepository.kt`
```kotlin
package com.openjugg.domain.repository

import com.openjugg.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getAllExercisesOnce(): List<Exercise>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun getByLiftType(liftType: LiftType): List<Exercise>
    suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<Exercise>
    suspend fun seedIfEmpty()
}
```

## `domain/repository/IProgramRepository.kt`
```kotlin
package com.openjugg.domain.repository

import com.openjugg.domain.model.*

interface IProgramRepository {
    suspend fun saveFullProgram(program: Program): Long
    suspend fun getActiveProgram(userId: Long): Program?
    suspend fun getSessionById(id: Long): TrainingSession?
    suspend fun updateSession(session: TrainingSession)
    suspend fun updateProgrammedExercise(exercise: ProgrammedExercise)
    suspend fun logSet(set: LoggedSet): Long
    suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSet>
    suspend fun getExerciseHistory(exerciseId: Long): List<LoggedSet>
}
```

## `data/repository/UserRepositoryImpl.kt`
```kotlin
package com.openjugg.data.repository

import com.openjugg.data.local.dao.UserProfileDao
import com.openjugg.data.local.mapper.toDomain
import com.openjugg.data.local.mapper.toEntity
import com.openjugg.domain.model.UserProfile
import com.openjugg.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : IUserRepository {

    override fun getActiveProfile(): Flow<UserProfile?> =
        dao.getActiveProfile().map { it?.toDomain() }

    override suspend fun getActiveProfileOnce(): UserProfile? =
        dao.getActiveProfileOnce()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile): Long =
        dao.insert(profile.toEntity())

    override suspend fun updateProfile(profile: UserProfile) =
        dao.update(profile.toEntity())
}
```

## `data/repository/ExerciseRepositoryImpl.kt`
```kotlin
package com.openjugg.data.repository

import com.openjugg.data.local.dao.ExerciseDao
import com.openjugg.data.local.mapper.toDomain
import com.openjugg.data.seed.ExerciseDatabaseSeeder
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val seeder: ExerciseDatabaseSeeder
) : IExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllExercisesOnce(): List<Exercise> =
        dao.getAllOnce().map { it.toDomain() }

    override suspend fun getExerciseById(id: Long): Exercise? =
        dao.getById(id)?.toDomain()

    override suspend fun getByLiftType(liftType: LiftType): List<Exercise> =
        dao.getByLiftType(liftType).map { it.toDomain() }

    override suspend fun getByLiftTypeAndCategory(liftType: LiftType, category: ExerciseCategory): List<Exercise> =
        dao.getByLiftTypeAndCategory(liftType, category).map { it.toDomain() }

    override suspend fun seedIfEmpty() = seeder.seedIfEmpty()
}
```

## `data/repository/ProgramRepositoryImpl.kt`
```kotlin
package com.openjugg.data.repository

import com.openjugg.data.local.dao.ProgramDao
import com.openjugg.data.local.entity.*
import com.openjugg.data.local.mapper.*
import com.openjugg.domain.model.*
import com.openjugg.domain.repository.IProgramRepository
import javax.inject.Inject

class ProgramRepositoryImpl @Inject constructor(
    private val dao: ProgramDao
) : IProgramRepository {

    /**
     * Persists the entire generated program tree:
     * Program → Phases → Weeks → Sessions → ProgrammedExercises
     */
    override suspend fun saveFullProgram(program: Program): Long {
        val programId = dao.insertProgram(
            ProgramEntity(
                userId = program.userId,
                name = program.name,
                goal = program.goal,
                startDate = program.startDate,
                endDate = program.endDate
            )
        )

        for (phase in program.phases) {
            val phaseId = dao.insertPhase(
                PhaseEntity(
                    programId = programId,
                    type = phase.type,
                    weekCount = phase.weekCount,
                    order = phase.order
                )
            )

            for (week in phase.weeks) {
                val weekId = dao.insertWeek(
                    TrainingWeekEntity(phaseId = phaseId, weekNumber = week.weekNumber)
                )

                for (session in week.sessions) {
                    val sessionId = dao.insertSession(
                        TrainingSessionEntity(
                            weekId = weekId,
                            dayOfWeek = session.dayOfWeek,
                            label = session.label,
                            readinessScore = null,
                            readinessInputs = null,
                            sessionDifficulty = null,
                            completed = false
                        )
                    )

                    for (exercise in session.exercises) {
                        dao.insertProgrammedExercise(
                            ProgrammedExerciseEntity(
                                sessionId = sessionId,
                                exerciseId = exercise.exerciseId,
                                exerciseName = exercise.exerciseName,
                                order = exercise.order,
                                targetSets = exercise.targetSets,
                                targetReps = exercise.targetReps,
                                targetRpe = exercise.targetRpe,
                                suggestedWeightKg = exercise.suggestedWeightKg,
                                isAmrap = exercise.isAmrap
                            )
                        )
                    }
                }
            }
        }

        return programId
    }

    /**
     * Reconstructs the full program tree from the database.
     */
    override suspend fun getActiveProgram(userId: Long): Program? {
        val programEntity = dao.getActiveProgramForUser(userId) ?: return null

        val phases = dao.getPhasesForProgram(programEntity.id).map { phaseEntity ->
            val weeks = dao.getWeeksForPhase(phaseEntity.id).map { weekEntity ->
                val sessions = dao.getSessionsForWeek(weekEntity.id).map { sessionEntity ->
                    val exercises = dao.getExercisesForSession(sessionEntity.id).map { exEntity ->
                        val sets = dao.getSetsForProgrammedExercise(exEntity.id).map { it.toDomain() }
                        exEntity.toDomain(sets)
                    }
                    sessionEntity.toDomain(exercises)
                }
                TrainingWeek(
                    id = weekEntity.id,
                    phaseId = weekEntity.phaseId,
                    weekNumber = weekEntity.weekNumber,
                    sessions = sessions
                )
            }
            Phase(
                id = phaseEntity.id,
                programId = phaseEntity.programId,
                type = phaseEntity.type,
                weekCount = phaseEntity.weekCount,
                order = phaseEntity.order,
                weeks = weeks
            )
        }

        return Program(
            id = programEntity.id,
            userId = programEntity.userId,
            name = programEntity.name,
            goal = programEntity.goal,
            startDate = programEntity.startDate,
            endDate = programEntity.endDate,
            phases = phases
        )
    }

    override suspend fun getSessionById(id: Long): TrainingSession? {
        val entity = dao.getSessionById(id) ?: return null
        val exercises = dao.getExercisesForSession(id).map { exEntity ->
            val sets = dao.getSetsForProgrammedExercise(exEntity.id).map { it.toDomain() }
            exEntity.toDomain(sets)
        }
        return entity.toDomain(exercises)
    }

    override suspend fun updateSession(session: TrainingSession) =
        dao.updateSession(session.toEntity())

    override suspend fun updateProgrammedExercise(exercise: ProgrammedExercise) =
        dao.updateProgrammedExercise(exercise.toEntity())

    override suspend fun logSet(set: LoggedSet): Long =
        dao.insertLoggedSet(set.toEntity())

    override suspend fun getSetsForProgrammedExercise(progExId: Long): List<LoggedSet> =
        dao.getSetsForProgrammedExercise(progExId).map { it.toDomain() }

    override suspend fun getExerciseHistory(exerciseId: Long): List<LoggedSet> =
        dao.getHistoryForExercise(exerciseId).map { it.toDomain() }
}
```

---

# 6. 🧠 THE PROGRAMMING ENGINE (All Algorithms)

This is the brain. Everything here is **pure Kotlin** with no Android dependencies.

## `domain/engine/OneRepMaxEstimator.kt`
```kotlin
package com.openjugg.domain.engine

import kotlin.math.roundToInt

/**
 * Estimates 1RM from weight × reps using multiple validated formulas.
 * Also calculates the weight needed for a given % of 1RM and target RPE.
 *
 * RPE-to-reps-in-reserve mapping:
 *   RPE 10 = 0 RIR (true max)
 *   RPE 9  = 1 RIR
 *   RPE 8  = 2 RIR
 *   RPE 7  = 3 RIR
 *   RPE 6  = 4 RIR
 */
object OneRepMaxEstimator {

    // ─── 1RM Estimation ──────────────────────────────────

    /** Epley formula: 1RM = w × (1 + r/30) */
    fun epley(weight: Float, reps: Int): Float =
        if (reps == 1) weight else weight * (1f + reps / 30f)

    /** Brzycki formula: 1RM = w × 36 / (37 - r) */
    fun brzycki(weight: Float, reps: Int): Float =
        if (reps == 1) weight else weight * 36f / (37f - reps)

    /**
     * Average of Epley and Brzycki for a more robust estimate.
     * Used as the primary estimator throughout the app.
     */
    fun estimate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0) return 0f
        if (reps == 1) return weight
        return (epley(weight, reps) + brzycki(weight, reps)) / 2f
    }

    /**
     * Estimate 1RM from a set logged at a specific RPE.
     * We convert RPE to effective reps:
     *   effectiveReps = actualReps + (10 - RPE)
     * Then use the standard formula.
     *
     * Example: 5 reps @ RPE 8 → effective reps = 5 + 2 = 7 → estimate 1RM from 7-rep set.
     */
    fun estimate1RMFromRPE(weight: Float, reps: Int, rpe: Float): Float {
        val repsInReserve = 10f - rpe
        val effectiveReps = reps + repsInReserve.toInt()
        return estimate1RM(weight, effectiveReps)
    }

    // ─── Weight Calculation ──────────────────────────────

    /**
     * Given a 1RM and a target percentage, return the working weight.
     * Result is rounded to nearest 2.5 kg (standard plate increments).
     */
    fun weightForPercentage(oneRepMax: Float, percentage: Float): Float {
        val raw = oneRepMax * percentage
        return roundToNearest(raw, 2.5f)
    }

    /**
     * Given a 1RM, target reps, and target RPE, calculate the suggested working weight.
     *
     * Logic: At RPE [targetRpe] for [targetReps], the effective max reps = targetReps + RIR.
     * We invert the Epley formula:
     *   weight = 1RM / (1 + effectiveReps / 30)
     */
    fun weightForRepsAndRPE(oneRepMax: Float, targetReps: Int, targetRpe: Float): Float {
        val rir = 10f - targetRpe
        val effectiveReps = targetReps + rir.toInt()
        if (effectiveReps <= 0) return oneRepMax
        val raw = oneRepMax / (1f + effectiveReps / 30f)
        return roundToNearest(raw, 2.5f)
    }

    // ─── RPE / Percentage Mapping ────────────────────────

    /**
     * Converts a phase-level intensity percentage + target RPE into a
     * practical working percentage of 1RM.
     *
     * This is a lookup table based on commonly used RPE charts:
     * Rows = reps (1-12), Columns = RPE (6-10)
     * Values = % of 1RM
     */
    private val rpePercentageChart: Map<Int, Map<Float, Float>> = mapOf(
        1  to mapOf(10f to 1.00f, 9.5f to 0.978f, 9f to 0.955f, 8.5f to 0.939f, 8f to 0.922f, 7.5f to 0.906f, 7f to 0.890f, 6.5f to 0.874f, 6f to 0.858f),
        2  to mapOf(10f to 0.955f, 9.5f to 0.939f, 9f to 0.922f, 8.5f to 0.906f, 8f to 0.890f, 7.5f to 0.874f, 7f to 0.858f, 6.5f to 0.842f, 6f to 0.826f),
        3  to mapOf(10f to 0.922f, 9.5f to 0.906f, 9f to 0.890f, 8.5f to 0.874f, 8f to 0.858f, 7.5f to 0.842f, 7f to 0.826f, 6.5f to 0.811f, 6f to 0.795f),
        4  to mapOf(10f to 0.890f, 9.5f to 0.874f, 9f to 0.858f, 8.5f to 0.842f, 8f to 0.826f, 7.5f to 0.811f, 7f to 0.795f, 6.5f to 0.779f, 6f to 0.763f),
        5  to mapOf(10f to 0.863f, 9.5f to 0.847f, 9f to 0.832f, 8.5f to 0.816f, 8f to 0.800f, 7.5f to 0.784f, 7f to 0.769f, 6.5f to 0.753f, 6f to 0.737f),
        6  to mapOf(10f to 0.837f, 9.5f to 0.822f, 9f to 0.807f, 8.5f to 0.791f, 8f to 0.775f, 7.5f to 0.760f, 7f to 0.744f, 6.5f to 0.728f, 6f to 0.713f),
        7  to mapOf(10f to 0.811f, 9.5f to 0.797f, 9f to 0.782f, 8.5f to 0.767f, 8f to 0.752f, 7.5f to 0.737f, 7f to 0.723f, 6.5f to 0.708f, 6f to 0.694f),
        8  to mapOf(10f to 0.786f, 9.5f to 0.773f, 9f to 0.760f, 8.5f to 0.746f, 8f to 0.732f, 7.5f to 0.718f, 7f to 0.704f, 6.5f to 0.690f, 6f to 0.676f),
        9  to mapOf(10f to 0.765f, 9.5f to 0.752f, 9f to 0.739f, 8.5f to 0.726f, 8f to 0.713f, 7.5f to 0.700f, 7f to 0.687f, 6.5f to 0.674f, 6f to 0.661f),
        10 to mapOf(10f to 0.744f, 9.5f to 0.732f, 9f to 0.720f, 8.5f to 0.707f, 8f to 0.695f, 7.5f to 0.683f, 7f to 0.670f, 6.5f to 0.658f, 6f to 0.646f),
        11 to mapOf(10f to 0.723f, 9.5f to 0.712f, 9f to 0.700f, 8.5f to 0.689f, 8f to 0.677f, 7.5f to 0.666f, 7f to 0.654f, 6.5f to 0.643f, 6f to 0.631f),
        12 to mapOf(10f to 0.703f, 9.5f to 0.694f, 9f to 0.684f, 8.5f to 0.674f, 8f to 0.664f, 7.5f to 0.654f, 7f to 0.644f, 6.5f to 0.634f, 6f to 0.624f)
    )

    /**
     * Look up the percentage of 1RM for a given rep count and RPE.
     * Falls back to Epley-based inverse calculation if outside chart range.
     */
    fun percentageFor(reps: Int, rpe: Float): Float {
        val clampedReps = reps.coerceIn(1, 12)
        val clampedRpe = rpe.coerceIn(6f, 10f)

        // Try exact lookup
        rpePercentageChart[clampedReps]?.get(clampedRpe)?.let { return it }

        // Interpolate between nearest RPE values
        val repRow = rpePercentageChart[clampedReps] ?: return weightForRepsAndRPE(1f, reps, rpe)
        val sortedKeys = repRow.keys.sorted()
        val lower = sortedKeys.lastOrNull { it <= clampedRpe } ?: sortedKeys.first()
        val upper = sortedKeys.firstOrNull { it >= clampedRpe } ?: sortedKeys.last()

        if (lower == upper) return repRow[lower]!!

        val lowerVal = repRow[lower]!!
        val upperVal = repRow[upper]!!
        val ratio = (clampedRpe - lower) / (upper - lower)
        return lowerVal + ratio * (upperVal - lowerVal)
    }

    /**
     * Get suggested weight from 1RM using the RPE chart.
     */
    fun suggestedWeight(oneRepMax: Float, reps: Int, rpe: Float): Float {
        val pct = percentageFor(reps, rpe)
        return roundToNearest(oneRepMax * pct, 2.5f)
    }

    // ─── Utility ─────────────────────────────────────────

    fun roundToNearest(value: Float, increment: Float): Float {
        return (Math.round(value / increment) * increment)
    }
}
```

## `domain/engine/VolumeCalculator.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Calculates Minimum Effective Volume (MEV) and Maximum Recoverable Volume (MRV)
 * per lift per week, measured in hard working sets.
 *
 * Based on sports science literature and practical coaching guidelines:
 *   - Beginners need less volume and recover slower from relative intensity
 *   - Advanced lifters need more volume but can recover from more
 *   - Age, gender, and bodyweight influence recovery capacity
 *   - Each phase has different volume targets relative to MEV and MRV
 *
 * All values are in SETS PER WEEK for that specific lift (not muscle group).
 */
object VolumeCalculator {

    /**
     * Volume landmarks per lift per week.
     */
    data class VolumeLandmarks(
        val mev: Int,   // Minimum Effective Volume (sets/week) — minimum to progress
        val mrv: Int,   // Maximum Recoverable Volume (sets/week) — maximum before overtraining
        val mav: Int    // Maximum Adaptive Volume — the "sweet spot" between MEV and MRV
    )

    /**
     * Base volume ranges per experience level (sets per lift per week).
     * These are for the main competition lift (squat/bench/deadlift) only.
     */
    private data class BaseVolume(val mev: Int, val mrv: Int)

    private val baseVolumes = mapOf(
        ExperienceLevel.BEGINNER     to BaseVolume(mev = 6,  mrv = 12),
        ExperienceLevel.INTERMEDIATE to BaseVolume(mev = 8,  mrv = 16),
        ExperienceLevel.ADVANCED     to BaseVolume(mev = 10, mrv = 20),
        ExperienceLevel.ELITE        to BaseVolume(mev = 12, mrv = 24)
    )

    /**
     * Calculate volume landmarks for a given user and lift.
     *
     * Modifiers:
     *   - Age > 35: MRV decreases by 1 set per 5 years over 35
     *   - Age < 25: MRV increases by 1 (young recovery advantage)
     *   - Deadlift: MRV reduced by ~15% (higher systemic fatigue per set)
     *   - Female: Typically can handle slightly higher relative volume (+1 MEV, +2 MRV)
     */
    fun calculate(user: UserProfile, liftType: LiftType): VolumeLandmarks {
        val base = baseVolumes[user.experienceLevel]
            ?: baseVolumes[ExperienceLevel.INTERMEDIATE]!!

        var mev = base.mev
        var mrv = base.mrv

        // ─── Age modifier ────────────────────────────────
        if (user.age > 35) {
            val yearsOver35 = (user.age - 35) / 5
            mrv -= yearsOver35
        } else if (user.age < 25) {
            mrv += 1
        }

        // ─── Gender modifier ─────────────────────────────
        if (user.gender == Gender.FEMALE) {
            mev += 1
            mrv += 2
        }

        // ─── Lift-specific modifier ──────────────────────
        when (liftType) {
            LiftType.DEADLIFT -> {
                // Deadlifts are more systemically fatiguing
                mev = (mev * 0.85f).toInt().coerceAtLeast(4)
                mrv = (mrv * 0.85f).toInt()
            }
            LiftType.BENCH -> {
                // Bench tends to tolerate slightly more volume
                mrv += 1
            }
            LiftType.SQUAT -> { /* baseline */ }
        }

        // Ensure minimums
        mev = mev.coerceAtLeast(4)
        mrv = mrv.coerceAtLeast(mev + 4)

        // MAV = midpoint between MEV and MRV
        val mav = (mev + mrv) / 2

        return VolumeLandmarks(mev = mev, mrv = mrv, mav = mav)
    }

    /**
     * Determine the target sets for a specific phase of training.
     *
     *   Hypertrophy:  Start at MAV, ramp toward MRV over the phase weeks
     *   Strength:     Start at MEV + 2, stay around low-to-moderate volume
     *   Peaking:      At or slightly above MEV (minimal volume, maximal intensity)
     *   Deload:       50% of MEV
     */
    fun setsForPhaseWeek(
        landmarks: VolumeLandmarks,
        phaseType: PhaseType,
        weekInPhase: Int,     // 1-indexed
        totalWeeksInPhase: Int
    ): Int {
        val progress = weekInPhase.toFloat() / totalWeeksInPhase.toFloat()  // 0.0 → 1.0

        return when (phaseType) {
            PhaseType.HYPERTROPHY -> {
                // Ramp from MAV toward MRV
                val startVol = landmarks.mav
                val endVol = landmarks.mrv
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.STRENGTH -> {
                // Moderate volume, slight ramp down as intensity increases
                val startVol = landmarks.mav - 1
                val endVol = landmarks.mev + 2
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.PEAKING -> {
                // Low volume, decreasing toward the end
                val startVol = landmarks.mev + 1
                val endVol = landmarks.mev
                (startVol + (endVol - startVol) * progress).toInt()
            }
            PhaseType.DELOAD -> {
                // Half of MEV
                (landmarks.mev / 2).coerceAtLeast(2)
            }
        }
    }
}
```

## `domain/engine/PeriodizationPlanner.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Determines the phase structure (number & length of each phase) for the program.
 *
 * Phase ordering follows classical block periodization:
 *   Hypertrophy → Strength → Peaking → Deload
 *
 * If a meet date is provided, the planner works backward from the meet to allocate phases.
 * If no meet date, it creates a general development program.
 */
object PeriodizationPlanner {

    data class PhaseTemplate(
        val type: PhaseType,
        val weeks: Int,
        val order: Int
    )

    /**
     * Plan the phase structure.
     *
     * @param user          The user profile
     * @param totalWeeks    Total available weeks (calculated from meet date, or default)
     * @return              Ordered list of phase templates
     */
    fun planPhases(user: UserProfile, totalWeeks: Int? = null): List<PhaseTemplate> {
        val available = totalWeeks ?: defaultProgramLength(user.experienceLevel)
        return allocatePhases(user.experienceLevel, user.trainingGoal, available)
    }

    /**
     * Default program length if no meet date is set.
     */
    private fun defaultProgramLength(level: ExperienceLevel): Int = when (level) {
        ExperienceLevel.BEGINNER     -> 12  // 12-week cycle
        ExperienceLevel.INTERMEDIATE -> 16  // 16-week cycle
        ExperienceLevel.ADVANCED     -> 16  // 16-week cycle
        ExperienceLevel.ELITE        -> 20  // 20-week cycle
    }

    /**
     * Allocate phases based on experience, goal, and total time.
     *
     * Ratios (approximate):
     *   Beginner:     50% Hypertrophy, 30% Strength, 10% Peak, 10% Deload
     *   Intermediate: 40% Hypertrophy, 30% Strength, 15% Peak, 15% Deload
     *   Advanced:     35% Hypertrophy, 30% Strength, 20% Peak, 15% Deload
     *   Elite:        30% Hypertrophy, 30% Strength, 25% Peak, 15% Deload
     *
     * For Powerbuilding: More hypertrophy, less peaking
     */
    private fun allocatePhases(
        level: ExperienceLevel,
        goal: TrainingGoal,
        totalWeeks: Int
    ): List<PhaseTemplate> {

        // Base ratios by experience
        var hypertrophyRatio: Float
        var strengthRatio: Float
        var peakingRatio: Float
        val deloadWeeks: Int

        when (level) {
            ExperienceLevel.BEGINNER -> {
                hypertrophyRatio = 0.50f; strengthRatio = 0.30f; peakingRatio = 0.10f
                deloadWeeks = 1
            }
            ExperienceLevel.INTERMEDIATE -> {
                hypertrophyRatio = 0.40f; strengthRatio = 0.30f; peakingRatio = 0.15f
                deloadWeeks = 1
            }
            ExperienceLevel.ADVANCED -> {
                hypertrophyRatio = 0.35f; strengthRatio = 0.30f; peakingRatio = 0.20f
                deloadWeeks = 1
            }
            ExperienceLevel.ELITE -> {
                hypertrophyRatio = 0.30f; strengthRatio = 0.30f; peakingRatio = 0.25f
                deloadWeeks = 1
            }
        }

        // Adjust for goal
        when (goal) {
            TrainingGoal.POWERBUILDING -> {
                hypertrophyRatio += 0.10f
                peakingRatio -= 0.05f
                strengthRatio -= 0.05f
            }
            TrainingGoal.POWER_COMBO -> {
                hypertrophyRatio += 0.05f
                peakingRatio -= 0.05f
            }
            TrainingGoal.POWERLIFTING -> { /* default ratios */ }
        }

        val remainingWeeks = totalWeeks - deloadWeeks
        val hypertrophyWeeks = (remainingWeeks * hypertrophyRatio).toInt().coerceAtLeast(2)
        val strengthWeeks = (remainingWeeks * strengthRatio).toInt().coerceAtLeast(2)
        val peakingWeeks = (remainingWeeks - hypertrophyWeeks - strengthWeeks).coerceAtLeast(1)

        // Build phase list with deload inserted between major blocks
        val phases = mutableListOf<PhaseTemplate>()
        var order = 0

        // Hypertrophy block
        phases.add(PhaseTemplate(PhaseType.HYPERTROPHY, hypertrophyWeeks, order++))
        // Deload after hypertrophy
        phases.add(PhaseTemplate(PhaseType.DELOAD, 1, order++))

        // Strength block
        phases.add(PhaseTemplate(PhaseType.STRENGTH, strengthWeeks, order++))

        // Peaking block (no deload between strength and peaking — intensity stays high)
        phases.add(PhaseTemplate(PhaseType.PEAKING, peakingWeeks, order++))

        // Final deload / taper before meet
        if (deloadWeeks > 0) {
            phases.add(PhaseTemplate(PhaseType.DELOAD, 1, order++))
        }

        return phases
    }

    /**
     * Calculate total weeks from now until meet date.
     */
    fun weeksUntilMeet(nowMillis: Long, meetDateMillis: Long): Int {
        val diff = meetDateMillis - nowMillis
        return (diff / (7L * 24 * 60 * 60 * 1000)).toInt().coerceAtLeast(4)
    }
}
```

## `domain/engine/FrequencyDistributor.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.LiftType

/**
 * Distributes SBD (Squat, Bench, Deadlift) training across the user's available
 * training days per week.
 *
 * Principles:
 *   - Bench can be trained more frequently (2-4x/week) due to lower systemic fatigue
 *   - Squat and deadlift share lower body demand → spread them apart
 *   - Never squat and deadlift heavy on the same day
 *   - Each session gets a "primary" lift and may get a secondary lift
 */
object FrequencyDistributor {

    /**
     * Represents what lifts go on a given training day.
     * @param dayOfWeek  1=Monday ... 7=Sunday
     * @param primary    The main lift for this day
     * @param secondary  Optional secondary lift (lighter or variation)
     * @param label      Human-friendly name
     */
    data class DaySlot(
        val dayOfWeek: Int,
        val primary: LiftType,
        val secondary: LiftType? = null,
        val label: String
    )

    /**
     * Generate the weekly training template.
     *
     * @param daysPerWeek  3-6 training days
     * @return             List of DaySlots mapping lifts to days
     */
    fun distribute(daysPerWeek: Int): List<DaySlot> {
        return when (daysPerWeek.coerceIn(3, 6)) {
            3 -> threeDaySplit()
            4 -> fourDaySplit()
            5 -> fiveDaySplit()
            6 -> sixDaySplit()
            else -> fourDaySplit()
        }
    }

    /**
     * 3-Day Split: Classic powerlifting layout
     *   Day 1 (Mon): Squat
     *   Day 2 (Wed): Bench
     *   Day 3 (Fri): Deadlift
     *
     * Frequency: S=1, B=1, D=1
     */
    private fun threeDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT, null, "Squat Day"),
        DaySlot(3, LiftType.BENCH, null, "Bench Day"),
        DaySlot(5, LiftType.DEADLIFT, null, "Deadlift Day")
    )

    /**
     * 4-Day Split: Upper/Lower hybrid
     *   Day 1 (Mon): Squat (primary) + Bench (light)
     *   Day 2 (Tue): Bench (primary)
     *   Day 3 (Thu): Deadlift (primary) + Bench (variation)
     *   Day 4 (Fri): Squat (variation)
     *
     * Frequency: S=2, B=3, D=1
     */
    private fun fourDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    LiftType.BENCH,    "Squat + Bench"),
        DaySlot(2, LiftType.BENCH,    null,               "Bench Day"),
        DaySlot(4, LiftType.DEADLIFT, LiftType.BENCH,    "Deadlift + Bench"),
        DaySlot(5, LiftType.SQUAT,    null,               "Squat Variation")
    )

    /**
     * 5-Day Split: High frequency
     *   Day 1 (Mon): Squat (primary)
     *   Day 2 (Tue): Bench (primary)
     *   Day 3 (Wed): Deadlift (primary)
     *   Day 4 (Thu): Bench (variation)
     *   Day 5 (Fri): Squat (variation) + Deadlift (light accessory)
     *
     * Frequency: S=2, B=2, D=2
     */
    private fun fiveDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    null,               "Squat Day"),
        DaySlot(2, LiftType.BENCH,    null,               "Bench Day"),
        DaySlot(3, LiftType.DEADLIFT, null,               "Deadlift Day"),
        DaySlot(4, LiftType.BENCH,    null,               "Bench Variation"),
        DaySlot(5, LiftType.SQUAT,    LiftType.DEADLIFT,  "Squat + Light DL")
    )

    /**
     * 6-Day Split: Very high frequency (advanced)
     *   Day 1 (Mon): Squat (heavy)
     *   Day 2 (Tue): Bench (heavy)
     *   Day 3 (Wed): Deadlift (heavy)
     *   Day 4 (Thu): Squat (variation)
     *   Day 5 (Fri): Bench (variation)
     *   Day 6 (Sat): Deadlift (variation)
     *
     * Frequency: S=2, B=2, D=2
     */
    private fun sixDaySplit() = listOf(
        DaySlot(1, LiftType.SQUAT,    null, "Squat (Heavy)"),
        DaySlot(2, LiftType.BENCH,    null, "Bench (Heavy)"),
        DaySlot(3, LiftType.DEADLIFT, null, "Deadlift (Heavy)"),
        DaySlot(4, LiftType.SQUAT,    null, "Squat (Variation)"),
        DaySlot(5, LiftType.BENCH,    null, "Bench (Variation)"),
        DaySlot(6, LiftType.DEADLIFT, null, "Deadlift (Variation)")
    )

    /**
     * Count how many times per week a given lift appears (as primary or secondary).
     */
    fun frequencyOf(slots: List<DaySlot>, liftType: LiftType): Int {
        return slots.count { it.primary == liftType || it.secondary == liftType }
    }
}
```

## `domain/engine/ExerciseSelector.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Selects exercises for each training session based on:
 *   1. The lift type for that day (Squat/Bench/Deadlift)
 *   2. Whether it's a primary day or variation day
 *   3. The user's weak points
 *   4. The current training phase
 *   5. Available exercises in the library
 *
 * Selection priority:
 *   - Primary day → competition lift first, then weak-point variation, then accessories
 *   - Variation day → weak-point variation first, then accessories
 *   - Hypertrophy → more accessories
 *   - Peaking → competition lift only, minimal accessories
 */
object ExerciseSelector {

    data class SelectedExercise(
        val exercise: Exercise,
        val isPrimary: Boolean   // Is this the main lift or an accessory?
    )

    /**
     * Select exercises for a single session slot.
     *
     * @param liftType        The main lift for this slot
     * @param isVariationDay  Is this a "secondary" day for this lift?
     * @param phaseType       Current phase
     * @param weakPoints      User's weak points for this lift
     * @param exerciseLibrary All available exercises
     * @param totalSets       Total sets budgeted for this lift this session
     * @return                Ordered list of selected exercises with metadata
     */
    fun selectForSession(
        liftType: LiftType,
        isVariationDay: Boolean,
        phaseType: PhaseType,
        weakPoints: List<WeakPoint>,
        exerciseLibrary: List<Exercise>,
        totalSets: Int
    ): List<SelectedExercise> {

        val liftExercises = exerciseLibrary.filter { it.liftType == liftType }
        val primary = liftExercises.filter { it.category == ExerciseCategory.PRIMARY }
        val variations = liftExercises.filter { it.category == ExerciseCategory.VARIATION }
        val accessories = liftExercises.filter { it.category == ExerciseCategory.ACCESSORY }

        // Score variations by how many of the user's weak points they address
        val scoredVariations = variations
            .map { ex -> ex to ex.addressesWeakPoints.count { it in weakPoints } }
            .sortedByDescending { it.second }

        val scoredAccessories = accessories
            .map { ex -> ex to ex.addressesWeakPoints.count { it in weakPoints } }
            .sortedByDescending { it.second }

        val result = mutableListOf<SelectedExercise>()

        when {
            // ─── PEAKING: Competition lift only ──────────
            phaseType == PhaseType.PEAKING -> {
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }
            }

            // ─── DELOAD: Competition lift, light ─────────
            phaseType == PhaseType.DELOAD -> {
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }
                // Maybe one light accessory
                scoredAccessories.firstOrNull()?.let {
                    result.add(SelectedExercise(it.first, isPrimary = false))
                }
            }

            // ─── VARIATION DAY ───────────────────────────
            isVariationDay -> {
                // Lead with best variation for weak points
                val bestVariation = scoredVariations.firstOrNull()
                if (bestVariation != null) {
                    result.add(SelectedExercise(bestVariation.first, isPrimary = true))
                } else {
                    // Fallback to competition lift
                    primary.firstOrNull()?.let {
                        result.add(SelectedExercise(it, isPrimary = true))
                    }
                }

                // Add accessories based on remaining set budget
                val accessorySets = (totalSets - estimateSetsForExercise(result.size, totalSets))
                    .coerceAtLeast(0)
                addAccessories(scoredAccessories, result, accessorySets, phaseType)
            }

            // ─── PRIMARY DAY (Hypertrophy or Strength) ──
            else -> {
                // Competition lift first
                primary.firstOrNull()?.let {
                    result.add(SelectedExercise(it, isPrimary = true))
                }

                // Add a variation if in hypertrophy (more volume to distribute)
                if (phaseType == PhaseType.HYPERTROPHY && scoredVariations.isNotEmpty()) {
                    result.add(SelectedExercise(scoredVariations.first().first, isPrimary = false))
                }

                // Fill remaining with accessories
                val usedSets = result.size * 3 // rough estimate: 3 sets each
                val remainingSets = (totalSets - usedSets).coerceAtLeast(0)
                addAccessories(scoredAccessories, result, remainingSets, phaseType)
            }
        }

        return result
    }

    /**
     * Add accessory exercises up to the set budget.
     */
    private fun addAccessories(
        scoredAccessories: List<Pair<Exercise, Int>>,
        result: MutableList<SelectedExercise>,
        availableSets: Int,
        phaseType: PhaseType
    ) {
        val maxAccessories = when (phaseType) {
            PhaseType.HYPERTROPHY -> 3
            PhaseType.STRENGTH    -> 2
            PhaseType.PEAKING     -> 0
            PhaseType.DELOAD      -> 1
        }

        val setsPerAccessory = 3
        val accessoryCount = (availableSets / setsPerAccessory).coerceAtMost(maxAccessories)

        val alreadySelected = result.map { it.exercise.id }.toSet()
        scoredAccessories
            .filter { it.first.id !in alreadySelected }
            .take(accessoryCount)
            .forEach { result.add(SelectedExercise(it.first, isPrimary = false)) }
    }

    /**
     * Rough estimate: primary exercise gets ~40% of total sets, variations get ~30%, rest is accessories.
     */
    private fun estimateSetsForExercise(exerciseIndex: Int, totalSets: Int): Int {
        return when (exerciseIndex) {
            0 -> (totalSets * 0.40f).toInt().coerceAtLeast(3)
            1 -> (totalSets * 0.30f).toInt().coerceAtLeast(2)
            else -> 3
        }
    }
}
```

## `domain/engine/ReadinessAdjuster.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Pre-session readiness system.
 *
 * Takes the user's subjective readiness inputs and produces adjustments
 * to the day's planned training:
 *   - Weight adjustment (as a multiplier, e.g. 0.90 = reduce by 10%)
 *   - Set adjustment (how many sets to add or remove from accessories)
 *   - Whether to skip variation work entirely
 *
 * Readiness composite score (1-5):
 *   = (sleep + nutrition + motivation + (6 - overallSoreness)) / 4
 *
 * Note: soreness is inverted because 5 = extremely sore = bad.
 */
object ReadinessAdjuster {

    data class Adjustments(
        val weightMultiplier: Float,        // e.g., 1.0 = no change, 0.90 = -10%
        val accessorySetDelta: Int,         // e.g., -1 = drop 1 set from each accessory
        val skipVariations: Boolean,        // If true, only do competition lift
        val compositeScore: Float,          // For logging/display
        val message: String                 // User-facing feedback
    )

    /**
     * Calculate readiness adjustments.
     */
    fun assess(inputs: ReadinessInputs): Adjustments {
        // Invert soreness: 1 (fresh) → 5, 5 (destroyed) → 1
        val invertedSoreness = 6f - inputs.overallSoreness

        val composite = (inputs.sleep + inputs.nutrition + inputs.motivation + invertedSoreness) / 4f

        return when {
            // ─── Very low readiness (1.0 - 2.0) ─────────
            composite < 2.0f -> Adjustments(
                weightMultiplier = 0.85f,
                accessorySetDelta = -2,
                skipVariations = true,
                compositeScore = composite,
                message = "Very low readiness. Significantly reducing load and volume. Focus on movement quality today."
            )

            // ─── Low readiness (2.0 - 2.75) ─────────────
            composite < 2.75f -> Adjustments(
                weightMultiplier = 0.90f,
                accessorySetDelta = -1,
                skipVariations = false,
                compositeScore = composite,
                message = "Below average readiness. Slightly reducing load. Listen to your body."
            )

            // ─── Normal readiness (2.75 - 3.75) ─────────
            composite < 3.75f -> Adjustments(
                weightMultiplier = 1.0f,
                accessorySetDelta = 0,
                skipVariations = false,
                compositeScore = composite,
                message = "Normal readiness. Proceeding as planned."
            )

            // ─── High readiness (3.75 - 4.5) ────────────
            composite < 4.5f -> Adjustments(
                weightMultiplier = 1.0f,  // Don't auto-increase weight; let RPE guide it
                accessorySetDelta = 0,
                skipVariations = false,
                compositeScore = composite,
                message = "Feeling good! You may push intensity if RPE allows."
            )

            // ─── Excellent readiness (4.5 - 5.0) ────────
            else -> Adjustments(
                weightMultiplier = 1.02f,  // Tiny bump — confidence boost
                accessorySetDelta = 1,     // Can handle extra accessory volume
                skipVariations = false,
                compositeScore = composite,
                message = "Excellent readiness! Slight weight increase. Consider an extra set on accessories."
            )
        }
    }

    /**
     * Check if a specific muscle group is too sore to train.
     * Used to further reduce volume for exercises targeting that muscle.
     *
     * @return multiplier for sets on exercises targeting that muscle (0.5 = halve sets, 1.0 = no change)
     */
    fun muscleGroupSorenessMultiplier(inputs: ReadinessInputs, liftType: LiftType): Float {
        val relevantSoreness = when (liftType) {
            LiftType.SQUAT -> maxOf(inputs.quadSoreness, inputs.hamstringSoreness)
            LiftType.BENCH -> maxOf(inputs.chestSoreness, inputs.shoulderSoreness)
            LiftType.DEADLIFT -> maxOf(inputs.hamstringSoreness, inputs.backSoreness)
        }

        return when {
            relevantSoreness >= 5 -> 0.50f   // Extremely sore → halve volume
            relevantSoreness >= 4 -> 0.75f   // Very sore → reduce by 25%
            else -> 1.0f                      // Manageable
        }
    }
}
```

## `domain/engine/RpeAutoRegulator.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.LoggedSet
import com.openjugg.domain.model.ProgrammedExercise

/**
 * Intra-session and session-to-session auto-regulation based on RPE feedback.
 *
 * This is the real-time adjustment system that makes every set responsive:
 *   - After logging a set, it decides what to do for the next set
 *   - After a session, it adjusts future sessions
 *   - Tracks RPE drift to detect fatigue accumulation
 */
object RpeAutoRegulator {

    /**
     * Decision after logging a single set within a workout.
     */
    data class IntraSetDecision(
        val adjustedWeightKg: Float,     // Suggested weight for the next set
        val dropRemainingSet: Boolean,   // Should we remove the next set entirely?
        val message: String
    )

    /**
     * Analyze a just-logged set and decide what to do for the NEXT set.
     *
     * Rules:
     *   1. If RPE >= 10 (failure) → drop all remaining sets for safety
     *   2. If RPE >= 9.5 and target was < 9 → drop remaining sets
     *   3. If RPE is > 1 above target → reduce weight by 5% for next set
     *   4. If RPE is > 0.5 above target → reduce weight by 2.5%
     *   5. If RPE is > 1 below target → increase weight by 2.5-5% for next set
     *   6. If RPE is on target (± 0.5) → keep weight the same
     */
    fun decideAfterSet(
        loggedSet: LoggedSet,
        targetRpe: Float,
        currentSuggestedWeight: Float,
        remainingSets: Int
    ): IntraSetDecision {
        val rpeDiff = loggedSet.rpe - targetRpe  // positive = harder than expected

        return when {
            // Hit failure or very close → STOP
            loggedSet.rpe >= 10f -> IntraSetDecision(
                adjustedWeightKg = currentSuggestedWeight,
                dropRemainingSet = true,
                message = "RPE 10 reached. Dropping remaining sets for recovery."
            )

            // Way too hard (1.5+ over target) → drop remaining
            rpeDiff >= 1.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 0.92f, 2.5f
                ),
                dropRemainingSet = remainingSets <= 1,
                message = "Significantly over target RPE. ${if (remainingSets <= 1) "Stopping." else "Reducing weight 8%."}"
            )

            // Too hard (0.5-1.5 over) → reduce weight
            rpeDiff >= 0.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * (1f - rpeDiff * 0.025f), 2.5f
                ),
                dropRemainingSet = false,
                message = "Slightly over target RPE. Reducing weight ${(rpeDiff * 2.5f).toInt()}%."
            )

            // Too easy (1+ under target) → can increase weight
            rpeDiff <= -1.0f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 1.05f, 2.5f
                ),
                dropRemainingSet = false,
                message = "Under target RPE. Consider increasing weight ~5%."
            )

            // Slightly easy (0.5-1 under) → small bump
            rpeDiff <= -0.5f -> IntraSetDecision(
                adjustedWeightKg = OneRepMaxEstimator.roundToNearest(
                    currentSuggestedWeight * 1.025f, 2.5f
                ),
                dropRemainingSet = false,
                message = "Slightly under target RPE. Small weight increase suggested."
            )

            // On target
            else -> IntraSetDecision(
                adjustedWeightKg = currentSuggestedWeight,
                dropRemainingSet = false,
                message = "Right on target. Keep the weight."
            )
        }
    }

    /**
     * Post-session analysis: Determine adjustments for NEXT session
     * of the same exercise.
     *
     * @param loggedSets    All sets logged for this exercise in this session
     * @param targetRpe     The target RPE for the exercise
     * @param currentWeight The weight used (average or programmed)
     * @return              Weight adjustment multiplier for next session (e.g., 1.025 = +2.5%)
     */
    data class SessionToSessionAdjustment(
        val weightMultiplier: Float,
        val setsAdjustment: Int,         // -1, 0, or +1
        val estimated1RM: Float,         // Updated e1RM from this session's data
        val message: String
    )

    fun analyzeSession(
        loggedSets: List<LoggedSet>,
        targetRpe: Float,
        oneRepMax: Float
    ): SessionToSessionAdjustment {
        if (loggedSets.isEmpty()) return SessionToSessionAdjustment(1.0f, 0, oneRepMax, "No data.")

        // Filter out skipped sets
        val completedSets = loggedSets.filter { !it.skipped }
        if (completedSets.isEmpty()) return SessionToSessionAdjustment(1.0f, 0, oneRepMax, "All sets skipped.")

        val avgRpe = completedSets.map { it.rpe }.average().toFloat()
        val rpeDiff = avgRpe - targetRpe

        // Estimate 1RM from the best set (highest estimated 1RM)
        val best1RM = completedSets.maxOf { set ->
            OneRepMaxEstimator.estimate1RMFromRPE(set.weightKg, set.reps, set.rpe)
        }

        val weightMultiplier: Float
        val setsAdj: Int
        val msg: String

        when {
            // Average RPE way over target → reduce load next time
            rpeDiff >= 1.5f -> {
                weightMultiplier = 0.95f
                setsAdj = -1
                msg = "Last session was very hard (avg RPE ${String.format("%.1f", avgRpe)}). Reducing load 5% and dropping 1 set."
            }
            rpeDiff >= 0.5f -> {
                weightMultiplier = 0.975f
                setsAdj = 0
                msg = "Last session was harder than target. Slight load reduction."
            }
            rpeDiff <= -1.0f -> {
                weightMultiplier = 1.05f
                setsAdj = 0
                msg = "Last session felt easy (avg RPE ${String.format("%.1f", avgRpe)}). Increasing load 5%."
            }
            rpeDiff <= -0.5f -> {
                weightMultiplier = 1.025f
                setsAdj = 0
                msg = "Room to grow. Small load increase."
            }
            else -> {
                weightMultiplier = 1.0f
                setsAdj = 0
                msg = "Right on target. Maintaining current load."
            }
        }

        return SessionToSessionAdjustment(
            weightMultiplier = weightMultiplier,
            setsAdjustment = setsAdj,
            estimated1RM = best1RM,
            message = msg
        )
    }
}
```

## `domain/engine/LoadProgressionManager.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * Manages progressive overload across weeks within a phase.
 *
 * Each phase type has a different intensity and rep scheme progression:
 *
 *   HYPERTROPHY:
 *     Reps:      8-12
 *     Intensity: 60-72% 1RM
 *     RPE:       6-8
 *     Progression: Volume increases (more sets), intensity stays moderate
 *
 *   STRENGTH:
 *     Reps:      3-6
 *     Intensity: 75-85% 1RM
 *     RPE:       7-9
 *     Progression: Intensity increases, volume decreases slightly
 *
 *   PEAKING:
 *     Reps:      1-3
 *     Intensity: 85-95%+ 1RM
 *     RPE:       8-9.5
 *     Progression: Intensity increases to near-max, volume drops significantly
 *
 *   DELOAD:
 *     Reps:      5-8
 *     Intensity: 50-60% 1RM
 *     RPE:       5-6
 *     Flat (no progression — recovery week)
 */
object LoadProgressionManager {

    /**
     * Parameters for a specific exercise in a specific week.
     */
    data class WeeklyPrescription(
        val targetReps: Int,
        val targetRpe: Float,
        val intensityPercentage: Float,   // of 1RM
        val targetSets: Int
    )

    /**
     * Calculate the prescription for a given exercise on a given week.
     *
     * @param phaseType         Current phase
     * @param weekInPhase       1-indexed week number within the phase
     * @param totalWeeksInPhase Total weeks in this phase
     * @param isPrimaryLift     Is this the competition lift (vs. variation/accessory)?
     * @param totalWeekSets     Total sets allocated for this lift this week (from VolumeCalculator)
     * @param exercisesInSession Number of exercises in this session for this lift type
     */
    fun prescribe(
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        isPrimaryLift: Boolean,
        totalWeekSets: Int,
        exercisesInSession: Int
    ): WeeklyPrescription {
        // Progress ratio: 0.0 at start → 1.0 at end of phase
        val progress = (weekInPhase - 1).toFloat() / maxOf(1, totalWeeksInPhase - 1).toFloat()

        return when (phaseType) {
            PhaseType.HYPERTROPHY -> hypertrophyPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.STRENGTH    -> strengthPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.PEAKING     -> peakingPrescription(progress, isPrimaryLift, totalWeekSets, exercisesInSession)
            PhaseType.DELOAD      -> deloadPrescription(isPrimaryLift, totalWeekSets, exercisesInSession)
        }
    }

    // ─── HYPERTROPHY ─────────────────────────────────────

    private fun hypertrophyPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            // Primary: 10 → 8 over the phase (slight decrease as volume ramps)
            lerp(10f, 8f, progress).toInt()
        } else {
            // Accessories: stay 10-12
            lerp(12f, 10f, progress).toInt()
        }

        val rpe = if (isPrimary) {
            lerp(6.5f, 8.0f, progress)  // Gradually harder
        } else {
            lerp(6.0f, 7.5f, progress)
        }

        // Intensity derived from RPE chart
        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)

        // Distribute total sets across exercises
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets
        )
    }

    // ─── STRENGTH ────────────────────────────────────────

    private fun strengthPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            lerp(6f, 3f, progress).toInt()   // 6 → 3 over the phase
        } else {
            lerp(8f, 6f, progress).toInt()   // Accessories: 8 → 6
        }

        val rpe = if (isPrimary) {
            lerp(7.0f, 9.0f, progress)
        } else {
            lerp(7.0f, 8.0f, progress)
        }

        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets
        )
    }

    // ─── PEAKING ─────────────────────────────────────────

    private fun peakingPrescription(
        progress: Float,
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        val reps = if (isPrimary) {
            lerp(3f, 1f, progress).toInt().coerceAtLeast(1)   // 3 → 1
        } else {
            lerp(5f, 3f, progress).toInt()
        }

        val rpe = if (isPrimary) {
            lerp(8.0f, 9.5f, progress)
        } else {
            lerp(7.0f, 8.0f, progress)
        }

        val intensity = OneRepMaxEstimator.percentageFor(reps, rpe)
        val sets = distributeSets(totalSets, exerciseCount, isPrimary)

        return WeeklyPrescription(
            targetReps = reps,
            targetRpe = roundRpe(rpe),
            intensityPercentage = intensity,
            targetSets = sets.coerceAtMost(if (isPrimary) 5 else 3)
        )
    }

    // ─── DELOAD ──────────────────────────────────────────

    private fun deloadPrescription(
        isPrimary: Boolean,
        totalSets: Int,
        exerciseCount: Int
    ): WeeklyPrescription {
        return WeeklyPrescription(
            targetReps = if (isPrimary) 5 else 8,
            targetRpe = if (isPrimary) 6.0f else 5.0f,
            intensityPercentage = if (isPrimary) 0.60f else 0.50f,
            targetSets = distributeSets(totalSets, exerciseCount, isPrimary).coerceAtMost(3)
        )
    }

    // ─── Utility ─────────────────────────────────────────

    /** Linear interpolation */
    private fun lerp(start: Float, end: Float, t: Float): Float =
        start + (end - start) * t.coerceIn(0f, 1f)

    /** Round RPE to nearest 0.5 */
    private fun roundRpe(rpe: Float): Float =
        (Math.round(rpe * 2) / 2.0f).coerceIn(5.0f, 10.0f)

    /**
     * Distribute total weekly sets across exercises in a session.
     * Primary lift gets ~50% of sets, remaining split among accessories.
     */
    private fun distributeSets(totalSets: Int, exerciseCount: Int, isPrimary: Boolean): Int {
        if (exerciseCount <= 1) return totalSets
        return if (isPrimary) {
            (totalSets * 0.50f).toInt().coerceAtLeast(3)
        } else {
            val remaining = totalSets - (totalSets * 0.50f).toInt()
            val accessoryCount = (exerciseCount - 1).coerceAtLeast(1)
            (remaining / accessoryCount).coerceAtLeast(2)
        }
    }
}
```

## `domain/engine/ProgramGenerator.kt`
```kotlin
package com.openjugg.domain.engine

import com.openjugg.domain.model.*

/**
 * The master orchestrator. Ties all engine components together to generate
 * a complete, personalized training program from a user profile.
 *
 * Flow:
 *   1. PeriodizationPlanner   → decide phase structure
 *   2. VolumeCalculator       → decide volume per lift per week
 *   3. FrequencyDistributor   → map lifts to training days
 *   4. ExerciseSelector       → choose exercises for each session
 *   5. LoadProgressionManager → assign reps, RPE, intensity per week
 *   6. OneRepMaxEstimator     → convert intensity to actual kg
 *   7. Assemble the full Program object
 */
class ProgramGenerator(
    private val exerciseLibrary: List<Exercise>
) {

    /**
     * Generate a complete program for the given user.
     *
     * @param user  Fully filled-out user profile
     * @return      A complete Program with all phases, weeks, sessions, and exercises populated
     */
    fun generate(user: UserProfile): Program {

        val nowMillis = System.currentTimeMillis()

        // ── Step 1: Determine phase structure ────────────
        val totalWeeks = if (user.meetDate != null) {
            PeriodizationPlanner.weeksUntilMeet(nowMillis, user.meetDate)
        } else null

        val phaseTemplates = PeriodizationPlanner.planPhases(user, totalWeeks)
        val programTotalWeeks = phaseTemplates.sumOf { it.weeks }

        // ── Step 2: Volume landmarks per lift ────────────
        val squatVolume = VolumeCalculator.calculate(user, LiftType.SQUAT)
        val benchVolume = VolumeCalculator.calculate(user, LiftType.BENCH)
        val deadliftVolume = VolumeCalculator.calculate(user, LiftType.DEADLIFT)

        val volumeMap = mapOf(
            LiftType.SQUAT to squatVolume,
            LiftType.BENCH to benchVolume,
            LiftType.DEADLIFT to deadliftVolume
        )

        // ── Step 3: Weekly day layout ────────────────────
        val daySlots = FrequencyDistributor.distribute(user.daysPerWeek)

        // For each lift, determine how many sessions per week it appears
        val freqMap = mapOf(
            LiftType.SQUAT to FrequencyDistributor.frequencyOf(daySlots, LiftType.SQUAT),
            LiftType.BENCH to FrequencyDistributor.frequencyOf(daySlots, LiftType.BENCH),
            LiftType.DEADLIFT to FrequencyDistributor.frequencyOf(daySlots, LiftType.DEADLIFT)
        )

        // ── Step 4-6: Build phases → weeks → sessions → exercises ──

        val maxes = mapOf(
            LiftType.SQUAT to user.squatMax,
            LiftType.BENCH to user.benchMax,
            LiftType.DEADLIFT to user.deadliftMax
        )

        val weakPointsMap = mapOf(
            LiftType.SQUAT to user.squatWeakPoints,
            LiftType.BENCH to user.benchWeakPoints,
            LiftType.DEADLIFT to user.deadliftWeakPoints
        )

        var globalWeekCounter = 0
        val phases = phaseTemplates.map { template ->

            val weeks = (1..template.weeks).map { weekNum ->
                globalWeekCounter++

                val sessions = daySlots.map { slot ->
                    buildSession(
                        slot = slot,
                        phaseType = template.type,
                        weekInPhase = weekNum,
                        totalWeeksInPhase = template.weeks,
                        maxes = maxes,
                        volumeMap = volumeMap,
                        freqMap = freqMap,
                        weakPointsMap = weakPointsMap
                    )
                }

                TrainingWeek(weekNumber = weekNum, sessions = sessions)
            }

            Phase(type = template.type, weekCount = template.weeks, order = template.order, weeks = weeks)
        }

        val endDate = nowMillis + (programTotalWeeks * 7L * 24 * 60 * 60 * 1000)

        return Program(
            userId = user.id,
            name = "${user.trainingGoal.name} Program — ${programTotalWeeks} Weeks",
            goal = user.trainingGoal,
            startDate = nowMillis,
            endDate = endDate,
            phases = phases
        )
    }

    /**
     * Build a single training session.
     */
    private fun buildSession(
        slot: FrequencyDistributor.DaySlot,
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        maxes: Map<LiftType, Float>,
        volumeMap: Map<LiftType, VolumeCalculator.VolumeLandmarks>,
        freqMap: Map<LiftType, Int>,
        weakPointsMap: Map<LiftType, List<WeakPoint>>
    ): TrainingSession {

        val allExercises = mutableListOf<ProgrammedExercise>()
        var exerciseOrder = 0

        // Build exercises for the PRIMARY lift of this day
        val primaryExercises = buildExercisesForLift(
            liftType = slot.primary,
            isVariationDay = false,  // Primary day
            phaseType = phaseType,
            weekInPhase = weekInPhase,
            totalWeeksInPhase = totalWeeksInPhase,
            oneRepMax = maxes[slot.primary] ?: 100f,
            volumeLandmarks = volumeMap[slot.primary]!!,
            frequency = freqMap[slot.primary] ?: 1,
            weakPoints = weakPointsMap[slot.primary] ?: emptyList(),
            startOrder = exerciseOrder
        )
        allExercises.addAll(primaryExercises)
        exerciseOrder += primaryExercises.size

        // Build exercises for the SECONDARY lift (if present — it's a variation/lighter day)
        slot.secondary?.let { secondaryLift ->
            val secondaryExercises = buildExercisesForLift(
                liftType = secondaryLift,
                isVariationDay = true,  // Secondary = variation day
                phaseType = phaseType,
                weekInPhase = weekInPhase,
                totalWeeksInPhase = totalWeeksInPhase,
                oneRepMax = maxes[secondaryLift] ?: 100f,
                volumeLandmarks = volumeMap[secondaryLift]!!,
                frequency = freqMap[secondaryLift] ?: 1,
                weakPoints = weakPointsMap[secondaryLift] ?: emptyList(),
                startOrder = exerciseOrder
            )
            allExercises.addAll(secondaryExercises)
        }

        return TrainingSession(
            dayOfWeek = slot.dayOfWeek,
            label = slot.label,
            exercises = allExercises
        )
    }

    /**
     * Build the exercise list for a single lift within a session.
     */
    private fun buildExercisesForLift(
        liftType: LiftType,
        isVariationDay: Boolean,
        phaseType: PhaseType,
        weekInPhase: Int,
        totalWeeksInPhase: Int,
        oneRepMax: Float,
        volumeLandmarks: VolumeCalculator.VolumeLandmarks,
        frequency: Int,
        weakPoints: List<WeakPoint>,
        startOrder: Int
    ): List<ProgrammedExercise> {

        // Total weekly sets for this lift
        val weeklyTotalSets = VolumeCalculator.setsForPhaseWeek(
            volumeLandmarks, phaseType, weekInPhase, totalWeeksInPhase
        )

        // Sets for THIS session = weekly total / frequency
        val sessionSets = (weeklyTotalSets.toFloat() / frequency.coerceAtLeast(1)).toInt().coerceAtLeast(3)

        // Select exercises
        val selectedExercises = ExerciseSelector.selectForSession(
            liftType = liftType,
            isVariationDay = isVariationDay,
            phaseType = phaseType,
            weakPoints = weakPoints,
            exerciseLibrary = exerciseLibrary,
            totalSets = sessionSets
        )

        // Prescribe load for each
        return selectedExercises.mapIndexed { index, selected ->
            val prescription = LoadProgressionManager.prescribe(
                phaseType = phaseType,
                weekInPhase = weekInPhase,
                totalWeeksInPhase = totalWeeksInPhase,
                isPrimaryLift = selected.isPrimary,
                totalWeekSets = weeklyTotalSets,
                exercisesInSession = selectedExercises.size
            )

            // For the competition lift, use full 1RM.
            // For variations, use 90% of 1RM as a proxy (variations are typically lighter).
            // For accessories, use 70% of 1RM.
            val effectiveMax = when (selected.exercise.category) {
                ExerciseCategory.PRIMARY   -> oneRepMax
                ExerciseCategory.VARIATION -> oneRepMax * 0.90f
                ExerciseCategory.ACCESSORY -> oneRepMax * 0.70f
            }

            val suggestedWeight = OneRepMaxEstimator.suggestedWeight(
                effectiveMax, prescription.targetReps, prescription.targetRpe
            )

            ProgrammedExercise(
                exerciseId = selected.exercise.id,
                exerciseName = selected.exercise.name,
                order = startOrder + index,
                targetSets = prescription.targetSets,
                targetReps = prescription.targetReps,
                targetRpe = prescription.targetRpe,
                suggestedWeightKg = suggestedWeight,
                isAmrap = phaseType == PhaseType.HYPERTROPHY && index == 0 && !isVariationDay
                // Last set AMRAP on primary lift during hypertrophy
            )
        }
    }
}
```

---

# 7. DEPENDENCY INJECTION

## `di/DatabaseModule.kt`
```kotlin
package com.openjugg.di

import android.content.Context
import androidx.room.Room
import com.openjugg.data.local.OpenJuggDatabase
import com.openjugg.data.local.dao.*
import com.openjugg.data.seed.ExerciseDatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenJuggDatabase {
        return Room.databaseBuilder(
            context,
            OpenJuggDatabase::class.java,
            "openjugg.db"
        ).build()
    }

    @Provides fun provideUserProfileDao(db: OpenJuggDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideExerciseDao(db: OpenJuggDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideProgramDao(db: OpenJuggDatabase): ProgramDao = db.programDao()

    @Provides
    @Singleton
    fun provideExerciseSeeder(dao: ExerciseDao): ExerciseDatabaseSeeder =
        ExerciseDatabaseSeeder(dao)
}
```

## `di/RepositoryModule.kt`
```kotlin
package com.openjugg.di

import com.openjugg.data.repository.*
import com.openjugg.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): IUserRepository

    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): IExerciseRepository

    @Binds @Singleton
    abstract fun bindProgramRepository(impl: ProgramRepositoryImpl): IProgramRepository
}
```

---

# 8. HOW IT ALL WORKS TOGETHER

Here's the data flow at a high level:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USER ONBOARDING                              │
│  Name, Age, Gender, Weight, Maxes, Weak Points, Days/Week, Goal    │
└──────────────────────────────┬──────────────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PROGRAM GENERATOR                                 │
│                                                                      │
│  ┌──────────────────────┐   ┌───────────────────────┐               │
│  │ PeriodizationPlanner │──▶│ Phase Structure        │               │
│  │ (How many weeks of   │   │ Hyp(6w)→Deload(1w)→   │               │
│  │  each phase?)        │   │ Str(4w)→Peak(3w)→     │               │
│  └──────────────────────┘   │ Deload(1w)             │               │
│                              └───────────┬───────────┘               │
│  ┌──────────────────────┐                │                           │
│  │ VolumeCalculator     │◀───────────────┤                           │
│  │ (MEV/MAV/MRV per     │   ┌────────────▼──────────┐               │
│  │  lift per week)      │──▶│ FrequencyDistributor   │               │
│  └──────────────────────┘   │ (Which lifts on which  │               │
│                              │  days?)                │               │
│  ┌──────────────────────┐   └────────────┬───────────┘               │
│  │ ExerciseSelector     │◀───────────────┤                           │
│  │ (Pick exercises      │   ┌────────────▼──────────┐               │
│  │  based on weak pts)  │──▶│ LoadProgressionManager │               │
│  └──────────────────────┘   │ (Reps, RPE, intensity  │               │
│                              │  per week)             │               │
│  ┌──────────────────────┐   └────────────┬───────────┘               │
│  │ OneRepMaxEstimator   │◀───────────────┘                           │
│  │ (Convert % to kg)    │                                            │
│  └──────────┬───────────┘                                            │
└─────────────┼────────────────────────────────────────────────────────┘
              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      COMPLETE PROGRAM                                │
│  Program → Phases → Weeks → Sessions → Exercises (with kg/reps/RPE) │
│                         │                                            │
│                         ▼  Saved to Room DB                          │
└─────────────────────────────────────────────────────────────────────┘
              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     DURING WORKOUT                                   │
│                                                                      │
│  ┌──────────────────┐    ┌──────────────────────┐                   │
│  │ ReadinessAdjuster│───▶│ Pre-session: adjust   │                   │
│  │ (Sleep/Nutrition/ │   │ weight & volume based │                   │
│  │  Motivation/      │   │ on how you feel today │                   │
│  │  Soreness)        │   └──────────┬───────────┘                   │
│  └──────────────────┘              │                                 │
│                                     ▼                                │
│  ┌──────────────────┐    ┌──────────────────────┐                   │
│  │ RpeAutoRegulator  │───▶│ After each set:      │                   │
│  │ (Logged RPE vs    │   │ adjust next set's    │                   │
│  │  target RPE)      │   │ weight, or drop sets │                   │
│  └──────────────────┘   └──────────┬───────────┘                   │
│                                     ▼                                │
│                          ┌──────────────────────┐                   │
│                          │ Post-session: rate    │                   │
│                          │ difficulty, update    │                   │
│                          │ e1RM, adjust next     │                   │
│                          │ session's load        │                   │
│                          └──────────────────────┘                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

That is the complete local backend, exercise library, and full algorithm suite. Every engine component is pure Kotlin and independently unit-testable. The data layer uses Room for fully offline storage. Want me to build the UI layer next (Jetpack Compose screens for onboarding, dashboard, and the active workout view)?