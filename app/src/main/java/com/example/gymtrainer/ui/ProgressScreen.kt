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
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.GymViewModel
import com.example.gymtrainer.data.Exercise
import com.example.gymtrainer.ui.components.ChartPoint
import com.example.gymtrainer.ui.components.ExerciseIllustration
import com.example.gymtrainer.ui.components.LineChart
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Metric(val label: String) { WEIGHT("Top set weight"), VOLUME("Total volume") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(vm: GymViewModel) {
    val exercises by vm.allExercises.collectAsState()
    val groups by vm.muscleGroups.collectAsState()
    var selectedGroup by remember { mutableStateOf<com.example.gymtrainer.data.MuscleGroup?>(null) }
    var selected by remember { mutableStateOf<Exercise?>(null) }
    var groupExpanded by remember { mutableStateOf(false) }
    var metric by remember { mutableStateOf(Metric.WEIGHT) }

    // Exercises shown in the searchable dropdown, filtered by the chosen muscle group.
    val groupExercises = remember(selectedGroup, exercises) {
        if (selectedGroup == null) exercises
        else exercises.filter { it.muscleGroupId == selectedGroup!!.id }
    }

    val historyFlow = remember(selected) {
        selected?.let { vm.historyForExercise(it.id) } ?: flowOf(emptyList())
    }
    val history by historyFlow.collectAsState(initial = emptyList())

    val dailyFlow = remember(selected) {
        selected?.let { vm.dailyProgress(it.id) } ?: flowOf(emptyList())
    }
    val daily by dailyFlow.collectAsState(initial = emptyList())

    val dayFmt = SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault())
    val shortFmt = SimpleDateFormat("d MMM", Locale.getDefault())
    val grouped = history.groupBy { dayFmt.format(Date(it.timestamp)) }
    val best = history.maxWithOrNull(compareBy({ it.weightKg }, { it.reps }))

    val points = daily.map {
        ChartPoint(
            label = shortFmt.format(Date(it.firstTimestamp)),
            value = (if (metric == Metric.WEIGHT) it.maxWeight else it.totalVolume).toFloat()
        )
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                "Track progressive overload over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // 1) Muscle group filter (before the exercise picker)
        item {
            ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = it }) {
                OutlinedTextField(
                    value = selectedGroup?.name ?: "All muscles",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Muscle group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("All muscles") },
                        onClick = { selectedGroup = null; selected = null; groupExpanded = false }
                    )
                    groups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.name) },
                            onClick = { selectedGroup = g; selected = null; groupExpanded = false }
                        )
                    }
                }
            }
        }

        // 2) Searchable exercise picker (filtered by the group above)
        item {
            com.example.gymtrainer.ui.components.SearchableExerciseDropdown(
                label = "Exercise",
                exercises = groupExercises,
                selected = selected,
                onSelected = { selected = it }
            )
        }

        when {
            selected == null -> item {
                Text("Select an exercise to see your graph and history.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            history.isEmpty() -> item {
                Text("No sets logged yet for ${selected!!.name}. Run a session and they will appear here.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            else -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Metric.values().forEach { m ->
                            FilterChip(selected = metric == m, onClick = { metric = m }, label = { Text(m.label) })
                        }
                    }
                }
                item {
                    Card {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (metric == Metric.WEIGHT) "Heaviest set per session (kg)"
                                else "Total volume per session (kg)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LineChart(points = points, unitSuffix = "")
                            if (points.size < 2) {
                                Text(
                                    "Log this exercise on more than one day to see the trend line grow.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
                best?.let { b ->
                    item {
                        Card {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                ExerciseIllustration(selected!!.imageKey, size = 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("ALL-TIME BEST SET", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("${fmt(b.weightKg)} kg x ${b.reps} reps",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
                items(grouped.entries.toList()) { (day, sets) ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(day, fontWeight = FontWeight.Bold)
                            sets.sortedBy { it.setNumber }.forEach { s ->
                                Text("Set ${s.setNumber}:  ${fmt(s.weightKg)} kg x ${s.reps} reps",
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            val volume = sets.sumOf { it.weightKg * it.reps }
                            Text("Session volume: ${fmt(volume)} kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

private fun fmt(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.US, "%.1f", v)
