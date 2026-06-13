package com.example.gymtrainer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrainer.data.Exercise

/**
 * An exercise picker you can type into. The field doubles as a search box:
 * typing filters by exercise name or target sub-muscle, and each result shows
 * its illustration. Clearing or editing the text reopens the filtered list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableExerciseDropdown(
    label: String,
    exercises: List<Exercise>,
    selected: Exercise?,
    onSelected: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    // Reflect external selection changes (e.g. group reset clears the field).
    LaunchedEffect(selected) { query = selected?.name ?: "" }

    val filtered = remember(query, exercises, selected) {
        if (query.isBlank() || query == selected?.name) exercises
        else exercises.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.targetSubMuscle.contains(query, ignoreCase = true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; expanded = true },
            enabled = enabled,
            singleLine = true,
            label = { Text(label) },
            placeholder = { Text("Type to search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                filtered.forEach { ex ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ExerciseIllustration(ex.imageKey, size = 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(ex.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        ex.targetSubMuscle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelected(ex)
                            query = ex.name
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
