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
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.GymViewModel
import com.example.gymtrainer.data.Nutrition
import com.example.gymtrainer.data.UserProfile

/**
 * Personalized nutrition. The user enters sex, age, height, weight, activity
 * and goal; the app computes BMR, TDEE and macro targets (Mifflin-St Jeor)
 * and shows them. These targets also feed the Guided Plans screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: GymViewModel) {
    val saved by vm.profile.collectAsState()

    var sex by remember(saved) { mutableStateOf(saved?.sex ?: "Male") }
    var age by remember(saved) { mutableStateOf(saved?.age?.toString() ?: "") }
    var height by remember(saved) { mutableStateOf(saved?.heightCm?.toString() ?: "") }
    var weight by remember(saved) { mutableStateOf(saved?.weightKg?.toString() ?: "") }
    var activity by remember(saved) { mutableStateOf(saved?.activity ?: Nutrition.activityLevels[2].first) }
    var goal by remember(saved) { mutableStateOf(saved?.goal ?: "Maintain") }
    var activityExpanded by remember { mutableStateOf(false) }

    val targets = saved?.let { Nutrition.compute(it) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Nutrition", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(
                "Enter your details for accurate daily macro targets.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Male", "Female").forEach { s ->
                    FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s) })
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter(Char::isDigit) },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it.filter(Char::isDigit) },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            ExposedDropdownMenuBox(expanded = activityExpanded, onExpandedChange = { activityExpanded = it }) {
                OutlinedTextField(
                    value = activity, onValueChange = {}, readOnly = true,
                    label = { Text("Activity level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(activityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = activityExpanded, onDismissRequest = { activityExpanded = false }) {
                    Nutrition.activityLevels.forEach { (label, _) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { activity = label; activityExpanded = false })
                    }
                }
            }
        }
        item {
            Text("Goal", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Nutrition.goals.forEach { g ->
                    FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text(g) })
                }
            }
        }
        item {
            Button(
                onClick = {
                    val a = age.toIntOrNull(); val h = height.toIntOrNull(); val w = weight.toDoubleOrNull()
                    if (a != null && h != null && w != null) {
                        vm.saveProfile(UserProfile(1, sex, a, h, w, activity, goal))
                    }
                },
                enabled = age.toIntOrNull() != null && height.toIntOrNull() != null && weight.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (saved == null) "Calculate my plan" else "Update my plan") }
        }

        if (targets != null) {
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("YOUR DAILY TARGETS", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            "${targets.calories} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "for your ${saved!!.goal.lowercase()} goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Macro("Protein", "${targets.proteinG}g")
                            Macro("Carbs", "${targets.carbsG}g")
                            Macro("Fat", "${targets.fatG}g")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "BMR ${targets.bmr} kcal  -  Maintenance (TDEE) ${targets.tdee} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }
            item {
                Text(
                    "These targets now power the Guided Plans tab. Estimates based on the " +
                            "Mifflin-St Jeor equation; adjust based on real-world progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Macro(label: String, value: String) {
    Column {
        Text(value, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
