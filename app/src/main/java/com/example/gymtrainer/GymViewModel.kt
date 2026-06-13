package com.example.gymtrainer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrainer.data.AppDb
import com.example.gymtrainer.data.Exercise
import com.example.gymtrainer.data.PlanExercise
import com.example.gymtrainer.data.SetLog
import com.example.gymtrainer.data.WorkoutPlan
import com.example.gymtrainer.data.WorkoutSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A draft row in the Manual Workout Builder before the plan is saved. */
data class DraftItem(val exercise: Exercise, val muscleGroupName: String, val sets: Int, val reps: String)

class GymViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDb.get(app)

    // ---------------- Library ----------------
    val muscleGroups = db.exerciseDao().muscleGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allExercises = db.exerciseDao().allExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exercisesForGroup(groupId: Long) = db.exerciseDao().exercisesForGroup(groupId)

    // ---------------- Plans ----------------
    val plans = db.planDao().plans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun planExercises(planId: Long) = db.planDao().planExercises(planId)

    fun savePlan(name: String, items: List<DraftItem>, onSaved: () -> Unit) {
        viewModelScope.launch {
            val planId = db.planDao().insertPlan(WorkoutPlan(name = name))
            db.planDao().insertPlanExercises(
                items.mapIndexed { index, d ->
                    PlanExercise(
                        planId = planId,
                        exerciseId = d.exercise.id,
                        orderIndex = index,
                        targetSets = d.sets,
                        targetReps = d.reps
                    )
                }
            )
            onSaved()
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch { db.planDao().deletePlan(planId) }
    }

    // ---------------- Active session ----------------
    var activeSessionId by mutableStateOf<Long?>(null)
        private set

    fun startSession(planId: Long) {
        if (activeSessionId != null) return
        viewModelScope.launch {
            activeSessionId = db.sessionDao().insertSession(
                WorkoutSession(planId = planId, startTime = System.currentTimeMillis())
            )
        }
    }

    fun logSet(exerciseId: Long, setNumber: Int, weightKg: Double, reps: Int) {
        val sessionId = activeSessionId ?: return
        viewModelScope.launch {
            db.sessionDao().insertSetLog(
                SetLog(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = setNumber,
                    weightKg = weightKg,
                    reps = reps
                )
            )
        }
    }

    fun finishSession(onDone: () -> Unit) {
        val sessionId = activeSessionId ?: return onDone()
        viewModelScope.launch {
            db.sessionDao().finishSession(sessionId, System.currentTimeMillis())
            activeSessionId = null
            onDone()
        }
    }

    fun logsForSession(sessionId: Long) = db.sessionDao().logsForSession(sessionId)

    fun previousLogs(exerciseId: Long, currentSessionId: Long) =
        db.sessionDao().previousLogsForExercise(exerciseId, currentSessionId)

    // ---------------- Progress ----------------
    fun historyForExercise(exerciseId: Long) = db.sessionDao().historyForExercise(exerciseId)

    // ---------------- Guided plans ----------------
    val guidedPlans = db.guidedDao().guidedPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun guidedDays(planId: Long) = db.guidedDao().days(planId)

    // ---------------- Profile + nutrition ----------------
    val profile = db.profileDao().profile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun saveProfile(p: com.example.gymtrainer.data.UserProfile) {
        viewModelScope.launch { db.profileDao().upsert(p) }
    }

    // ---------------- Chart ----------------
    fun dailyProgress(exerciseId: Long) = db.profileDao().dailyProgress(exerciseId)
}
