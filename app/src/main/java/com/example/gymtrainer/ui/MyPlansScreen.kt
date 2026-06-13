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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.GymViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyPlansScreen(
    vm: GymViewModel,
    onStartSession: (Long) -> Unit,
    onBuildNew: () -> Unit
) {
    val plans by vm.plans.collectAsState()
    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Workouts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))

        if (plans.isEmpty()) {
            Text(
                "No custom plans yet. Build one to start logging sets.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBuildNew, modifier = Modifier.fillMaxWidth()) {
                Text("Open Workout Builder")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(plans) { plan ->
                    val detailFlow = androidx.compose.runtime.remember(plan.id) { vm.planExercises(plan.id) }
                    val details by detailFlow.collectAsState(initial = emptyList())
                    Card {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Created ${fmt.format(Date(plan.createdAt))} - ${details.size} exercises",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                IconButton(onClick = { vm.deletePlan(plan.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete plan")
                                }
                            }
                            details.forEach { d ->
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)) {
                                    com.example.gymtrainer.ui.components.ExerciseIllustration(d.imageKey, size = 32.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "${d.exerciseName}  (${d.targetSubMuscle})  ${d.targetSets}x${d.targetReps}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Button(onClick = { onStartSession(plan.id) }, modifier = Modifier.weight(1f)) {
                                    Text("Start session")
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = onBuildNew, modifier = Modifier.fillMaxWidth()) {
                        Text("Build another plan")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
