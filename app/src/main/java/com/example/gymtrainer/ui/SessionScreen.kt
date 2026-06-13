package com.example.gymtrainer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.GymViewModel
import com.example.gymtrainer.data.PlanExerciseDetail
import com.example.gymtrainer.data.SetLog
import com.example.gymtrainer.ui.components.ExerciseIllustration

/**
 * Run a workout. Pick a plan -> this opens with its exercises collapsed.
 * Tap an exercise to expand it, then log the exact weight and reps for each set.
 * Every set is saved immediately and flows straight into the Progress tab.
 */
@Composable
fun SessionScreen(vm: GymViewModel, planId: Long, onFinished: () -> Unit) {
    LaunchedEffect(planId) { vm.startSession(planId) }
    val sessionId = vm.activeSessionId

    if (sessionId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val exercisesFlow = remember(planId) { vm.planExercises(planId) }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())
    val logsFlow = remember(sessionId) { vm.logsForSession(sessionId) }
    val logs by logsFlow.collectAsState(initial = emptyList())

    val weightInputs = remember { mutableStateMapOf<Long, String>() }
    val repsInputs = remember { mutableStateMapOf<Long, String>() }
    val expandedId = remember { mutableStateOf<Long?>(null) }

    val totalSets = exercises.sumOf { it.targetSets }
    val doneSets = logs.size

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Active Session", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Tap an exercise to open it and log every set.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            if (totalSets > 0) {
                LinearProgressIndicator(
                    progress = { (doneSets.toFloat() / totalSets).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("$doneSets of $totalSets target sets logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        items(exercises, key = { it.id }) { ex ->
            ExerciseLogCard(
                vm = vm,
                sessionId = sessionId,
                detail = ex,
                loggedSets = logs.filter { it.exerciseId == ex.exerciseId },
                expanded = expandedId.value == ex.id,
                onToggle = { expandedId.value = if (expandedId.value == ex.id) null else ex.id },
                weightText = weightInputs[ex.exerciseId] ?: "",
                repsText = repsInputs[ex.exerciseId] ?: "",
                onWeightChange = { weightInputs[ex.exerciseId] = it },
                onRepsChange = { repsInputs[ex.exerciseId] = it }
            )
        }

        item {
            Button(onClick = { vm.finishSession(onFinished) }, modifier = Modifier.fillMaxWidth()) {
                Text("Finish workout")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExerciseLogCard(
    vm: GymViewModel,
    sessionId: Long,
    detail: PlanExerciseDetail,
    loggedSets: List<SetLog>,
    expanded: Boolean,
    onToggle: () -> Unit,
    weightText: String,
    repsText: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit
) {
    val previousFlow = remember(detail.exerciseId, sessionId) {
        vm.previousLogs(detail.exerciseId, sessionId)
    }
    val previous by previousFlow.collectAsState(initial = emptyList())
    val complete = loggedSets.size >= detail.targetSets

    Card {
        Column {
            // header row (tap to expand)
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseIllustration(detail.imageKey, size = 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(detail.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${detail.muscleGroup} > ${detail.targetSubMuscle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text("${loggedSets.size} / ${detail.targetSets} sets  -  target ${detail.targetReps} reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                }
                if (complete) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Complete",
                        tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Open"
                )
            }

            AnimatedVisibility(expanded) {
                Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                    if (previous.isNotEmpty()) {
                        Text("Last session: " + previous.reversed()
                            .joinToString("  ") { "${fmt(it.weightKg)}kg x ${it.reps}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(6.dp))
                    }

                    loggedSets.forEach { s ->
                        Text("Set ${s.setNumber}:  ${fmt(s.weightKg)} kg  x  ${s.reps} reps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = weightText, onValueChange = onWeightChange,
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { onRepsChange(it.filter { c -> c.isDigit() }) },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val w = weightText.toDoubleOrNull()
                            val r = repsText.toIntOrNull()
                            if (w != null && r != null) {
                                vm.logSet(detail.exerciseId, loggedSets.size + 1, w, r)
                                onRepsChange("")
                            }
                        },
                        enabled = weightText.toDoubleOrNull() != null && repsText.toIntOrNull() != null,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Log set ${loggedSets.size + 1}") }
                }
            }
        }
    }
}

private fun fmt(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
