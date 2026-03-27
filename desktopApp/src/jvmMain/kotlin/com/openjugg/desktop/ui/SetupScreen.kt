@file:OptIn(ExperimentalMaterial3Api::class)

package com.openjugg.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openjugg.domain.model.*

@Composable
fun SetupScreen(onNext: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("25") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var bodyweight by remember { mutableStateOf("80") }
    var height by remember { mutableStateOf("175") }
    var experience by remember { mutableStateOf(ExperienceLevel.INTERMEDIATE) }
    var goal by remember { mutableStateOf(TrainingGoal.POWERLIFTING) }
    var daysPerWeek by remember { mutableStateOf("4") }
    var squatMax by remember { mutableStateOf("") }
    var benchMax by remember { mutableStateOf("") }
    var deadliftMax by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("OpenJugg", style = MaterialTheme.typography.headlineLarge)
        Text("AI-Powered Strength Program Generator", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "16-week Juggernaut Method — 10s → 8s → 5s → 3s waves",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.height(8.dp))

        SectionHeader("Profile")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.width(120.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = bodyweight,
                onValueChange = { bodyweight = it },
                label = { Text("Bodyweight (kg)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.weight(1f)
            )
        }

        EnumSelector("Gender", Gender.entries, gender) { gender = it }
        EnumSelector("Experience Level", ExperienceLevel.entries, experience) { experience = it }

        SectionHeader("Training")

        EnumSelector("Goal", TrainingGoal.entries, goal) { goal = it }

        OutlinedTextField(
            value = daysPerWeek,
            onValueChange = { daysPerWeek = it },
            label = { Text("Days Per Week (3–6)") },
            modifier = Modifier.width(200.dp)
        )

        SectionHeader("One-Rep Maxes (kg)")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = squatMax,
                onValueChange = { squatMax = it },
                label = { Text("Squat 1RM") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = benchMax,
                onValueChange = { benchMax = it },
                label = { Text("Bench 1RM") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = deadliftMax,
                onValueChange = { deadliftMax = it },
                label = { Text("Deadlift 1RM") },
                modifier = Modifier.weight(1f)
            )
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val profile = buildProfile(
                    name, age, gender, bodyweight, height,
                    experience, goal, daysPerWeek,
                    squatMax, benchMax, deadliftMax
                )
                if (profile == null) {
                    error = "Please fill in all required fields with valid numbers."
                } else {
                    error = null
                    onNext(profile)
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Next: Choose Exercises  →")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary)
    HorizontalDivider()
}

@Composable
private fun <T : Enum<T>> EnumSelector(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name.replace('_', ' '),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replace('_', ' ')) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

private fun buildProfile(
    name: String, age: String, gender: Gender,
    bodyweight: String, height: String,
    experience: ExperienceLevel, goal: TrainingGoal,
    daysPerWeek: String, squatMax: String,
    benchMax: String, deadliftMax: String
): UserProfile? {
    return try {
        val days = daysPerWeek.trim().toInt().coerceIn(3, 6)
        UserProfile(
            name = name.trim().ifEmpty { "Athlete" },
            age = age.trim().toInt(),
            gender = gender,
            bodyweightKg = bodyweight.trim().toFloat(),
            heightCm = height.trim().toFloat(),
            experienceLevel = experience,
            trainingGoal = goal,
            daysPerWeek = days,
            squatMax = squatMax.trim().toFloat(),
            benchMax = benchMax.trim().toFloat(),
            deadliftMax = deadliftMax.trim().toFloat()
        )
    } catch (_: Exception) {
        null
    }
}
