package com.example.gymtrainer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.GymViewModel
import com.example.gymtrainer.data.GuidedPlan

/**
 * Guided Plans: pre-built programs combining structured workouts with
 * daily nutrition macros and example meals. Tap a plan to expand its days.
 */
@Composable
fun GuidedPlansScreen(vm: GymViewModel, onSetupProfile: () -> Unit) {
    val plans by vm.guidedPlans.collectAsState()
    val profile by vm.profile.collectAsState()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Guided Plans", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                "Full programs: training plus daily macros and meals.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (profile == null) {
            item {
                Card(Modifier.fillMaxWidth().clickable(onClick = onSetupProfile)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Personalize your nutrition",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Tap to enter your height, weight, age and goal. Macro targets below will then be calculated for you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }
        items(plans, key = { it.id }) { plan ->
            GuidedPlanCard(vm, plan, profile)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun GuidedPlanCard(vm: GymViewModel, plan: GuidedPlan, profile: com.example.gymtrainer.data.UserProfile?) {
    var expanded by remember { mutableStateOf(false) }
    val daysFlow = remember(plan.id) { vm.guidedDays(plan.id) }
    val days by daysFlow.collectAsState(initial = emptyList())

    // Personalized macros for THIS plan's goal (overrides the user's saved goal)
    val targets = profile?.let {
        com.example.gymtrainer.data.Nutrition.compute(it.copy(goal = plan.goal))
    }

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${plan.goal} - ${plan.durationWeeks} weeks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Text(
                if (expanded) "Tap to collapse" else "Tap to view the weekly schedule",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            AnimatedVisibility(expanded) {
                Column {
                    days.forEach { day ->
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text(day.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(6.dp))
                        Text("WORKOUT", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        day.workoutSummary.split("||").forEach {
                            Text("- $it", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("DAILY NUTRITION", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        if (targets != null) {
                            Row {
                                MacroChip("${targets.calories}", "kcal")
                                MacroChip("${targets.proteinG}g", "protein")
                                MacroChip("${targets.carbsG}g", "carbs")
                                MacroChip("${targets.fatG}g", "fat")
                            }
                        } else {
                            Text("Set up your profile in the Nutrition tab to see your personalized targets here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("MEALS", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        day.meals.split("||").forEach {
                            Text("- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(value: String, label: String) {
    Column(Modifier.padding(end = 18.dp)) {
        Text(value, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
