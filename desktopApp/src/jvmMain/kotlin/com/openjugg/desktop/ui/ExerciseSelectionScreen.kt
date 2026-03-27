@file:OptIn(ExperimentalMaterial3Api::class)

package com.openjugg.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openjugg.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionScreen(
    exercises: List<Exercise>,
    onBack: () -> Unit,
    onGenerate: (Set<Long>) -> Unit
) {
    // Variations and accessories can be toggled; primaries are always included
    val toggleableIds = remember {
        exercises.filter { it.category != ExerciseCategory.PRIMARY }.map { it.id }.toSet()
    }
    val selectedIds = remember { mutableStateOf(toggleableIds.toMutableSet()) }

    val allSelected = remember(selectedIds.value) {
        derivedStateOf { selectedIds.value.containsAll(toggleableIds) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Exercises") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        selectedIds.value = if (allSelected.value) {
                            mutableSetOf()
                        } else {
                            toggleableIds.toMutableSet()
                        }
                    }) {
                        Text(if (allSelected.value) "Deselect Accessories" else "Select All")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val primaryCount = exercises.count { it.category == ExerciseCategory.PRIMARY }
                    val selectedCount = primaryCount + selectedIds.value.size
                    Text(
                        "$selectedCount exercises selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = {
                        val primaryIds = exercises.filter { it.category == ExerciseCategory.PRIMARY }.map { it.id }.toSet()
                        onGenerate(primaryIds + selectedIds.value)
                    }) {
                        Text("Generate Program")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val byLift = exercises.groupBy { it.liftType }
            for (liftType in listOf(LiftType.SQUAT, LiftType.BENCH, LiftType.DEADLIFT)) {
                val liftExercises = byLift[liftType] ?: continue
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        liftType.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                for (category in listOf(ExerciseCategory.PRIMARY, ExerciseCategory.VARIATION, ExerciseCategory.ACCESSORY)) {
                    val catExercises = liftExercises.filter { it.category == category }
                    if (catExercises.isEmpty()) continue
                    item {
                        Text(
                            category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(catExercises) { exercise ->
                        val isPrimary = exercise.category == ExerciseCategory.PRIMARY
                        val isChecked = isPrimary || exercise.id in selectedIds.value
                        ExerciseRow(
                            exercise = exercise,
                            checked = isChecked,
                            enabled = !isPrimary,
                            onToggle = {
                                val updated = selectedIds.value.toMutableSet()
                                if (exercise.id in updated) updated.remove(exercise.id)
                                else updated.add(exercise.id)
                                selectedIds.value = updated
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled
        )
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.bodyMedium)
            if (exercise.addressesWeakPoints.isNotEmpty()) {
                Text(
                    "Targets: ${exercise.addressesWeakPoints.joinToString { it.name.lowercase().replace('_', ' ') }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!enabled) {
            Text("required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
