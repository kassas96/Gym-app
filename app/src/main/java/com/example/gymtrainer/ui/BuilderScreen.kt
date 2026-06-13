package com.example.gymtrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.DraftItem
import com.example.gymtrainer.GymViewModel
import com.example.gymtrainer.data.Exercise
import com.example.gymtrainer.data.MuscleGroup
import kotlinx.coroutines.flow.flowOf

/**
 * Manual Workout Builder.
 *
 * Cascading selection:
 *   1. Muscle group dropdown (Chest, Back, Legs, ...)
 *   2. Exercise dropdown, repopulated from the selected group
 * The targeted sub-muscle (e.g. "Upper Chest (Clavicular Head)") is shown
 * inside the exercise dropdown items AND in a highlight card once selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(vm: GymViewModel, onSaved: () -> Unit) {
    val groups by vm.muscleGroups.collectAsState()

    var planName by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var setsText by remember { mutableStateOf("3") }
    var repsText by remember { mutableStateOf("8-12") }
    val draft = remember { emptyList<DraftItem>().toMutableStateList() }

    val exercisesFlow = remember(selectedGroup) {
        selectedGroup?.let { vm.exercisesForGroup(it.id) } ?: flowOf(emptyList())
    }
    val exercises by exercisesFlow.collectAsState(initial = emptyList())

    var groupExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Workout Builder", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        item {
            OutlinedTextField(
                value = planName,
                onValueChange = { planName = it },
                label = { Text("Plan name (e.g. Push Day A)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ---- Step 1: muscle group ----
        item {
            ExposedDropdownMenuBox(
                expanded = groupExpanded,
                onExpandedChange = { groupExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGroup?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("1. Muscle group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = groupExpanded,
                    onDismissRequest = { groupExpanded = false }
                ) {
                    groups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.name) },
                            onClick = {
                                selectedGroup = g
                                selectedExercise = null // reset cascade
                                groupExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // ---- Step 2: exercise (populated from step 1, searchable) ----
        item {
            com.example.gymtrainer.ui.components.SearchableExerciseDropdown(
                label = if (selectedGroup == null) "2. Exercise (pick a group first)" else "2. Exercise",
                exercises = exercises,
                selected = selectedExercise,
                onSelected = { selectedExercise = it },
                enabled = selectedGroup != null
            )
        }

        // ---- Target sub-muscle highlight ----
        selectedExercise?.let { ex ->
            item {
                Card {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        com.example.gymtrainer.ui.components.ExerciseIllustration(ex.imageKey, size = 56.dp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("TARGET AREA", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(
                                ex.targetSubMuscle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${ex.name} - ${ex.equipment}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // ---- Sets / reps + add ----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = setsText,
                    onValueChange = { setsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Sets") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("Rep range") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Button(
                onClick = {
                    val ex = selectedExercise ?: return@Button
                    val grp = selectedGroup ?: return@Button
                    draft.add(
                        DraftItem(
                            exercise = ex,
                            muscleGroupName = grp.name,
                            sets = setsText.toIntOrNull() ?: 3,
                            reps = repsText.ifBlank { "8-12" }
                        )
                    )
                    selectedExercise = null
                },
                enabled = selectedExercise != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add exercise to plan") }
        }

        // ---- Draft list ----
        if (draft.isNotEmpty()) {
            item {
                Text("Plan so far (${draft.size} exercises)",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        items(draft) { d ->
            Card {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(d.exercise.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${d.muscleGroupName} > ${d.exercise.targetSubMuscle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${d.sets} sets x ${d.reps} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { draft.remove(d) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }

        // ---- Save ----
        item {
            Button(
                onClick = {
                    vm.savePlan(planName.ifBlank { "My Workout" }, draft.toList()) { onSaved() }
                },
                enabled = draft.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save workout plan") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
