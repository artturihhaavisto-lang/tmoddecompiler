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