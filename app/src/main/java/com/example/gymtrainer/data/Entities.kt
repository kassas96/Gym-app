package com.example.gymtrainer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* ------------------------------------------------------------------
 * EXERCISE LIBRARY
 * MuscleGroup (Chest, Back, ...) 1 --- * Exercise
 * Each Exercise carries the specific sub-muscle it targets
 * (e.g. Incline Dumbbell Press -> "Upper Chest (Clavicular Head)").
 * ------------------------------------------------------------------ */

@Entity(tableName = "muscle_groups")
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = MuscleGroup::class,
        parentColumns = ["id"],
        childColumns = ["muscleGroupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("muscleGroupId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroupId: Long,
    /** The granular target area, e.g. "Upper Chest (Clavicular Head)" */
    val targetSubMuscle: String,
    val equipment: String,
    /** Drives the drawn illustration. One of: barbell, dumbbell, cable, machine, bodyweight, kettlebell, other */
    val imageKey: String = "other"
)

/* ------------------------------------------------------------------
 * CUSTOM WORKOUT PLANS (Manual Workout Builder)
 * WorkoutPlan 1 --- * PlanExercise (ordered, with target sets/reps)
 * ------------------------------------------------------------------ */

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "plan_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index("exerciseId")]
)
data class PlanExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: String // e.g. "8-12"
)

/* ------------------------------------------------------------------
 * PROGRESS TRACKING
 * WorkoutSession = one trip to the gym running a plan.
 * SetLog = one logged set: exact weight + exact reps, timestamped.
 * This is the historical record used for progressive-overload review.
 * ------------------------------------------------------------------ */

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(
        entity = WorkoutPlan::class,
        parentColumns = ["id"],
        childColumns = ["planId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("planId")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val startTime: Long,
    val endTime: Long? = null
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/* ------------------------------------------------------------------
 * GUIDED PLANS (pre-built training + nutrition programs)
 * GuidedPlan 1 --- * GuidedPlanDay (workout + daily macros + meals)
 * ------------------------------------------------------------------ */

/* ------------------------------------------------------------------
 * USER PROFILE (drives personalized nutrition targets)
 * Single-row table (id is always 1).
 * ------------------------------------------------------------------ */

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val sex: String,          // "Male" / "Female"
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val activity: String,     // see Nutrition.ActivityLevel labels
    val goal: String          // "Cut" / "Maintain" / "Bulk"
)

@Entity(tableName = "guided_plans")
data class GuidedPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val goal: String,
    val description: String,
    val durationWeeks: Int
)

@Entity(
    tableName = "guided_plan_days",
    foreignKeys = [ForeignKey(
        entity = GuidedPlan::class,
        parentColumns = ["id"],
        childColumns = ["guidedPlanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("guidedPlanId")]
)
data class GuidedPlanDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guidedPlanId: Long,
    val dayNumber: Int,
    val title: String,            // e.g. "Day 1 - Push"
    val workoutSummary: String,   // "||"-separated exercise lines
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val meals: String             // "||"-separated meal lines
)
