package com.example.gymtrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Row returned for the plan editor / active session screens. */
data class PlanExerciseDetail(
    val id: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val targetSubMuscle: String,
    val muscleGroup: String,
    val imageKey: String,
    val targetSets: Int,
    val targetReps: String,
    val orderIndex: Int
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM muscle_groups ORDER BY name")
    fun muscleGroups(): Flow<List<MuscleGroup>>

    @Query("SELECT * FROM exercises WHERE muscleGroupId = :groupId ORDER BY name")
    fun exercisesForGroup(groupId: Long): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises ORDER BY name")
    fun allExercises(): Flow<List<Exercise>>
}

@Dao
interface PlanDao {
    @Insert
    suspend fun insertPlan(plan: WorkoutPlan): Long

    @Insert
    suspend fun insertPlanExercises(items: List<PlanExercise>)

    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun plans(): Flow<List<WorkoutPlan>>

    @Query(
        """
        SELECT pe.id, pe.exerciseId, e.name AS exerciseName, e.targetSubMuscle,
               mg.name AS muscleGroup, e.imageKey, pe.targetSets, pe.targetReps, pe.orderIndex
        FROM plan_exercises pe
        JOIN exercises e ON e.id = pe.exerciseId
        JOIN muscle_groups mg ON mg.id = e.muscleGroupId
        WHERE pe.planId = :planId
        ORDER BY pe.orderIndex
        """
    )
    fun planExercises(planId: Long): Flow<List<PlanExerciseDetail>>

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSetLog(log: SetLog)

    @Query("UPDATE workout_sessions SET endTime = :endTime WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, endTime: Long)

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY id")
    fun logsForSession(sessionId: Long): Flow<List<SetLog>>

    /** Most recent sets for this exercise from PREVIOUS sessions (progressive-overload reference). */
    @Query(
        """
        SELECT * FROM set_logs
        WHERE exerciseId = :exerciseId AND sessionId != :currentSessionId
        ORDER BY id DESC LIMIT 4
        """
    )
    fun previousLogsForExercise(exerciseId: Long, currentSessionId: Long): Flow<List<SetLog>>

    /** Full history for the Progress screen. */
    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun historyForExercise(exerciseId: Long): Flow<List<SetLog>>
}

@Dao
interface GuidedDao {
    @Query("SELECT * FROM guided_plans ORDER BY id")
    fun guidedPlans(): Flow<List<GuidedPlan>>

    @Query("SELECT * FROM guided_plan_days WHERE guidedPlanId = :planId ORDER BY dayNumber")
    fun days(planId: Long): Flow<List<GuidedPlanDay>>
}

/** One point on the progress graph: the heaviest set and total volume for a day. */
data class DailyProgress(
    val day: String,      // yyyy-MM-dd for sorting
    val maxWeight: Double,
    val totalVolume: Double,
    val firstTimestamp: Long
)

@Dao
interface ProfileDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun profile(): Flow<UserProfile?>

    /** Aggregates set logs into one row per calendar day for the chart. */
    @Query(
        """
        SELECT strftime('%Y-%m-%d', timestamp/1000, 'unixepoch', 'localtime') AS day,
               MAX(weightKg) AS maxWeight,
               SUM(weightKg * reps) AS totalVolume,
               MIN(timestamp) AS firstTimestamp
        FROM set_logs
        WHERE exerciseId = :exerciseId
        GROUP BY day
        ORDER BY firstTimestamp
        """
    )
    fun dailyProgress(exerciseId: Long): Flow<List<DailyProgress>>
}
