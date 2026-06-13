package com.example.gymtrainer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MuscleGroup::class, Exercise::class,
        WorkoutPlan::class, PlanExercise::class,
        WorkoutSession::class, SetLog::class,
        GuidedPlan::class, GuidedPlanDay::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun planDao(): PlanDao
    abstract fun sessionDao(): SessionDao
    abstract fun guidedDao(): GuidedDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun get(context: Context): AppDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDb::class.java, "gymtrainer.db"
                )
                    .addCallback(SeedCallback)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

private fun esc(s: String) = s.replace("'", "''")

private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        val groups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Glutes", "Full Body")
        groups.forEachIndexed { i, g ->
            db.execSQL("INSERT INTO muscle_groups (id, name) VALUES (${i + 1}, '${esc(g)}')")
        }

        data class Ex(val n: String, val g: Int, val sub: String, val eq: String, val img: String)
        val list = listOf(
            Ex("Flat Barbell Bench Press", 1, "Mid Chest (Sternal Head)", "Barbell", "barbell"),
            Ex("Incline Barbell Press", 1, "Upper Chest (Clavicular Head)", "Barbell", "barbell"),
            Ex("Decline Barbell Press", 1, "Lower Chest (Costal Head)", "Barbell", "barbell"),
            Ex("Flat Dumbbell Press", 1, "Mid Chest (Sternal Head)", "Dumbbell", "dumbbell"),
            Ex("Incline Dumbbell Press", 1, "Upper Chest (Clavicular Head)", "Dumbbell", "dumbbell"),
            Ex("Decline Dumbbell Press", 1, "Lower Chest (Costal Head)", "Dumbbell", "dumbbell"),
            Ex("Dumbbell Fly", 1, "Inner / Outer Chest", "Dumbbell", "dumbbell"),
            Ex("Incline Dumbbell Fly", 1, "Upper Chest (Clavicular Head)", "Dumbbell", "dumbbell"),
            Ex("Cable Crossover (High to Low)", 1, "Lower / Inner Chest", "Cable", "cable"),
            Ex("Cable Crossover (Low to High)", 1, "Upper Chest", "Cable", "cable"),
            Ex("Pec Deck Fly", 1, "Inner Chest (Sternal Fibers)", "Machine", "machine"),
            Ex("Machine Chest Press", 1, "Mid Chest", "Machine", "machine"),
            Ex("Chest Dips", 1, "Lower Chest (Costal Head)", "Bodyweight", "bodyweight"),
            Ex("Push-Up", 1, "Mid Chest", "Bodyweight", "bodyweight"),
            Ex("Incline Push-Up", 1, "Lower Chest", "Bodyweight", "bodyweight"),
            Ex("Smith Machine Bench Press", 1, "Mid Chest", "Machine", "machine"),

            Ex("Pull-Up", 2, "Lats (Latissimus Dorsi)", "Bodyweight", "bodyweight"),
            Ex("Chin-Up", 2, "Lats + Biceps", "Bodyweight", "bodyweight"),
            Ex("Lat Pulldown (Wide Grip)", 2, "Outer Lats", "Cable", "cable"),
            Ex("Lat Pulldown (Close Grip)", 2, "Inner Lats / Lower Lats", "Cable", "cable"),
            Ex("Straight-Arm Pulldown", 2, "Lats (Lower Fibers)", "Cable", "cable"),
            Ex("Bent-Over Barbell Row", 2, "Mid Back (Rhomboids + Lats)", "Barbell", "barbell"),
            Ex("Pendlay Row", 2, "Mid Back (Explosive)", "Barbell", "barbell"),
            Ex("T-Bar Row", 2, "Mid Back (Rhomboids)", "Barbell", "barbell"),
            Ex("Seated Cable Row", 2, "Mid Back (Rhomboids + Mid Traps)", "Cable", "cable"),
            Ex("Single-Arm Dumbbell Row", 2, "Lats + Mid Back", "Dumbbell", "dumbbell"),
            Ex("Chest-Supported Dumbbell Row", 2, "Mid Back (Rhomboids)", "Dumbbell", "dumbbell"),
            Ex("Face Pull", 2, "Upper Back (Rear Delts + Traps)", "Cable", "cable"),
            Ex("Conventional Deadlift", 2, "Lower Back (Erector Spinae)", "Barbell", "barbell"),
            Ex("Rack Pull", 2, "Upper Back + Traps", "Barbell", "barbell"),
            Ex("Barbell Shrug", 2, "Upper Traps", "Barbell", "barbell"),
            Ex("Dumbbell Shrug", 2, "Upper Traps", "Dumbbell", "dumbbell"),
            Ex("Inverted Row", 2, "Mid Back", "Bodyweight", "bodyweight"),
            Ex("Hyperextension", 2, "Lower Back (Erector Spinae)", "Bodyweight", "bodyweight"),

            Ex("Barbell Back Squat", 3, "Quadriceps + Glutes", "Barbell", "barbell"),
            Ex("Front Squat", 3, "Quadriceps (Anterior Emphasis)", "Barbell", "barbell"),
            Ex("Hack Squat", 3, "Quadriceps (Sweep)", "Machine", "machine"),
            Ex("Leg Press", 3, "Quadriceps", "Machine", "machine"),
            Ex("Bulgarian Split Squat", 3, "Quads + Glutes (Unilateral)", "Dumbbell", "dumbbell"),
            Ex("Goblet Squat", 3, "Quadriceps", "Dumbbell", "dumbbell"),
            Ex("Romanian Deadlift", 3, "Hamstrings + Glutes", "Barbell", "barbell"),
            Ex("Stiff-Leg Deadlift", 3, "Hamstrings", "Barbell", "barbell"),
            Ex("Leg Extension", 3, "Quadriceps (Rectus Femoris)", "Machine", "machine"),
            Ex("Lying Leg Curl", 3, "Hamstrings (Biceps Femoris)", "Machine", "machine"),
            Ex("Seated Leg Curl", 3, "Hamstrings", "Machine", "machine"),
            Ex("Walking Lunge", 3, "Quads + Glutes", "Dumbbell", "dumbbell"),
            Ex("Reverse Lunge", 3, "Quads + Glutes", "Dumbbell", "dumbbell"),
            Ex("Standing Calf Raise", 3, "Calves (Gastrocnemius)", "Machine", "machine"),
            Ex("Seated Calf Raise", 3, "Calves (Soleus)", "Machine", "machine"),
            Ex("Adductor Machine", 3, "Inner Thigh (Adductors)", "Machine", "machine"),
            Ex("Abductor Machine", 3, "Outer Hip (Abductors)", "Machine", "machine"),

            Ex("Overhead Barbell Press", 4, "Front Delts (Anterior Head)", "Barbell", "barbell"),
            Ex("Seated Dumbbell Shoulder Press", 4, "Front + Side Delts", "Dumbbell", "dumbbell"),
            Ex("Arnold Press", 4, "All Three Deltoid Heads", "Dumbbell", "dumbbell"),
            Ex("Dumbbell Lateral Raise", 4, "Side Delts (Lateral Head)", "Dumbbell", "dumbbell"),
            Ex("Cable Lateral Raise", 4, "Side Delts (Lateral Head)", "Cable", "cable"),
            Ex("Machine Lateral Raise", 4, "Side Delts", "Machine", "machine"),
            Ex("Front Raise", 4, "Front Delts (Anterior Head)", "Dumbbell", "dumbbell"),
            Ex("Reverse Pec Deck Fly", 4, "Rear Delts (Posterior Head)", "Machine", "machine"),
            Ex("Bent-Over Reverse Fly", 4, "Rear Delts", "Dumbbell", "dumbbell"),
            Ex("Upright Row", 4, "Side Delts + Traps", "Barbell", "barbell"),
            Ex("Cable Rear Delt Fly", 4, "Rear Delts", "Cable", "cable"),

            Ex("Barbell Curl", 5, "Biceps (Long + Short Head)", "Barbell", "barbell"),
            Ex("EZ-Bar Curl", 5, "Biceps (Both Heads)", "Barbell", "barbell"),
            Ex("Incline Dumbbell Curl", 5, "Biceps (Long Head)", "Dumbbell", "dumbbell"),
            Ex("Dumbbell Curl", 5, "Biceps", "Dumbbell", "dumbbell"),
            Ex("Preacher Curl", 5, "Biceps (Short Head)", "Barbell", "barbell"),
            Ex("Hammer Curl", 5, "Brachialis + Forearms", "Dumbbell", "dumbbell"),
            Ex("Concentration Curl", 5, "Biceps (Peak)", "Dumbbell", "dumbbell"),
            Ex("Cable Curl", 5, "Biceps (Constant Tension)", "Cable", "cable"),
            Ex("Triceps Rope Pushdown", 5, "Triceps (Lateral Head)", "Cable", "cable"),
            Ex("Triceps Bar Pushdown", 5, "Triceps (Lateral + Medial)", "Cable", "cable"),
            Ex("Skull Crusher", 5, "Triceps (Long Head)", "Barbell", "barbell"),
            Ex("Overhead Triceps Extension", 5, "Triceps (Long Head)", "Dumbbell", "dumbbell"),
            Ex("Close-Grip Bench Press", 5, "Triceps (All Heads)", "Barbell", "barbell"),
            Ex("Triceps Dips", 5, "Triceps (All Heads)", "Bodyweight", "bodyweight"),
            Ex("Wrist Curl", 5, "Forearm Flexors", "Dumbbell", "dumbbell"),
            Ex("Reverse Wrist Curl", 5, "Forearm Extensors", "Dumbbell", "dumbbell"),

            Ex("Hanging Leg Raise", 6, "Lower Abs", "Bodyweight", "bodyweight"),
            Ex("Captain's Chair Knee Raise", 6, "Lower Abs", "Bodyweight", "bodyweight"),
            Ex("Cable Crunch", 6, "Upper Abs (Rectus Abdominis)", "Cable", "cable"),
            Ex("Crunch", 6, "Upper Abs", "Bodyweight", "bodyweight"),
            Ex("Plank", 6, "Deep Core (Transverse Abdominis)", "Bodyweight", "bodyweight"),
            Ex("Side Plank", 6, "Obliques + Deep Core", "Bodyweight", "bodyweight"),
            Ex("Russian Twist", 6, "Obliques", "Bodyweight", "bodyweight"),
            Ex("Bicycle Crunch", 6, "Obliques + Upper Abs", "Bodyweight", "bodyweight"),
            Ex("Ab Wheel Rollout", 6, "Full Rectus Abdominis + Deep Core", "Other", "other"),
            Ex("Mountain Climber", 6, "Lower Abs + Cardio", "Bodyweight", "bodyweight"),

            Ex("Hip Thrust", 7, "Glutes (Gluteus Maximus)", "Barbell", "barbell"),
            Ex("Glute Bridge", 7, "Glutes (Gluteus Maximus)", "Bodyweight", "bodyweight"),
            Ex("Cable Kickback", 7, "Glutes (Gluteus Maximus)", "Cable", "cable"),
            Ex("Step-Up", 7, "Glutes + Quads", "Dumbbell", "dumbbell"),
            Ex("Sumo Deadlift", 7, "Glutes + Adductors", "Barbell", "barbell"),
            Ex("Glute-Ham Raise", 7, "Glutes + Hamstrings", "Bodyweight", "bodyweight"),

            Ex("Kettlebell Swing", 8, "Posterior Chain", "Kettlebell", "kettlebell"),
            Ex("Clean and Press", 8, "Full Body (Power)", "Barbell", "barbell"),
            Ex("Thruster", 8, "Legs + Shoulders", "Barbell", "barbell"),
            Ex("Burpee", 8, "Full Body (Cardio)", "Bodyweight", "bodyweight"),
            Ex("Farmer's Carry", 8, "Grip + Core + Traps", "Dumbbell", "dumbbell"),
            Ex("Battle Ropes", 8, "Shoulders + Cardio", "Other", "other")
        )
        list.forEach { e ->
            db.execSQL(
                "INSERT INTO exercises (name, muscleGroupId, targetSubMuscle, equipment, imageKey) " +
                        "VALUES ('${esc(e.n)}', ${e.g}, '${esc(e.sub)}', '${esc(e.eq)}', '${esc(e.img)}')"
            )
        }

        db.execSQL(
            "INSERT INTO guided_plans (id, name, goal, description, durationWeeks) VALUES " +
                    "(1, 'Lean Muscle Builder', 'Bulk', " +
                    "'A 4-day push/pull/legs/upper split. Progressive overload focus: add weight or reps every week. " +
                    "Set up your profile to get exact daily macros for this goal.', 8)"
        )
        db.execSQL(
            "INSERT INTO guided_plans (id, name, goal, description, durationWeeks) VALUES " +
                    "(2, 'Cut and Conditioning', 'Cut', " +
                    "'A 3-day full-body program with a controlled deficit and high protein to preserve muscle. " +
                    "Set up your profile for personalized cutting macros.', 6)"
        )

        fun day(planId: Int, n: Int, title: String, workout: String, meals: String) = db.execSQL(
            "INSERT INTO guided_plan_days " +
                    "(guidedPlanId, dayNumber, title, workoutSummary, calories, proteinG, carbsG, fatG, meals) " +
                    "VALUES ($planId, $n, '${esc(title)}', '${esc(workout)}', 0, 0, 0, 0, '${esc(meals)}')"
        )

        day(1, 1, "Day 1 - Push (Chest / Shoulders / Triceps)",
            "Flat Barbell Bench Press 4x6-8||Incline Dumbbell Press 3x8-12||Overhead Barbell Press 3x6-8||Dumbbell Lateral Raise 4x12-15||Triceps Rope Pushdown 3x10-12||Skull Crusher 3x8-12",
            "Breakfast: oats, whey, banana, peanut butter||Lunch: chicken breast, rice, mixed vegetables||Pre-workout: Greek yogurt with berries||Dinner: lean beef, potatoes, salad||Snack: cottage cheese with almonds")
        day(1, 2, "Day 2 - Pull (Back / Biceps)",
            "Conventional Deadlift 3x5||Pull-Up 4xAMRAP||Seated Cable Row 3x8-12||Face Pull 3x15||Barbell Curl 3x8-12||Hammer Curl 3x10-12",
            "Breakfast: egg omelette, wholegrain toast, fruit||Lunch: turkey, wholewheat pasta, tomato sauce||Pre-workout: rice cakes with jam||Dinner: salmon, quinoa, roasted vegetables||Snack: casein shake with banana")
        day(1, 3, "Day 3 - Legs",
            "Barbell Back Squat 4x6-8||Romanian Deadlift 3x8-10||Leg Press 3x10-12||Lying Leg Curl 3x10-12||Standing Calf Raise 4x12-15",
            "Breakfast: protein pancakes with berries||Lunch: chicken burrito bowl, rice, beans, avocado||Pre-workout: bagel with honey||Dinner: lean beef chili with rice||Snack: Greek yogurt with granola")
        day(1, 4, "Day 4 - Upper Body Volume",
            "Incline Barbell Press 3x8-10||Bent-Over Barbell Row 3x8-10||Seated Dumbbell Shoulder Press 3x10||Lat Pulldown (Wide Grip) 3x10-12||Cable Crossover (High to Low) 3x12-15||Preacher Curl 3x12",
            "Breakfast: oats with whey and blueberries||Lunch: tuna wrap with avocado||Pre-workout: banana and rice cakes||Dinner: chicken stir-fry with noodles||Snack: cottage cheese with pineapple")

        day(2, 1, "Day 1 - Full Body Strength",
            "Barbell Back Squat 4x5||Flat Barbell Bench Press 4x5||Bent-Over Barbell Row 3x6-8||Plank 3x60s||Incline treadmill walk 20 min",
            "Breakfast: egg scramble with spinach, 1 toast||Lunch: chicken salad, light dressing||Snack: whey shake and an apple||Dinner: white fish, potatoes, green beans")
        day(2, 2, "Day 2 - Full Body Hypertrophy",
            "Leg Press 3x10-12||Incline Dumbbell Press 3x10-12||Lat Pulldown (Close Grip) 3x10-12||Dumbbell Lateral Raise 3x15||Cable Crunch 3x15||Bike intervals 15 min",
            "Breakfast: Greek yogurt with berries||Lunch: turkey and rice bowl, vegetables||Snack: protein bar||Dinner: lean beef stir-fry, reduced rice")
        day(2, 3, "Day 3 - Full Body + Conditioning",
            "Romanian Deadlift 3x8||Seated Dumbbell Shoulder Press 3x8-10||Seated Cable Row 3x10||Walking Lunge 3x12||Hanging Leg Raise 3x12||Rower intervals 15 min",
            "Breakfast: protein oats (small)||Lunch: chicken and quinoa salad||Snack: cottage cheese||Dinner: salmon with large mixed salad")
    }
}
