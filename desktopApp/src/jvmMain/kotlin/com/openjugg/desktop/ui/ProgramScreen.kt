package com.openjugg.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openjugg.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(program: Program, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(program.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProgramSummaryCard(program)
            }
            items(program.phases) { phase ->
                PhaseCard(phase)
            }
        }
    }
}

@Composable
private fun ProgramSummaryCard(program: Program) {
    val fmt = SimpleDateFormat("MMM d, yyyy")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Program Overview", style = MaterialTheme.typography.titleMedium)
            Text("Goal: ${program.goal.name.replace('_', ' ')}")
            Text("Start: ${fmt.format(Date(program.startDate))}  →  End: ${fmt.format(Date(program.endDate))}")
            val totalWeeks = program.phases.sumOf { it.weekCount }
            Text("Duration: $totalWeeks weeks  •  ${program.phases.size} phases")
        }
    }
}

@Composable
private fun PhaseCard(phase: Phase) {
    var expanded by remember { mutableStateOf(phase.order == 0) }

    val phaseColor = when (phase.type) {
        PhaseType.HYPERTROPHY -> MaterialTheme.colorScheme.tertiary
        PhaseType.STRENGTH    -> MaterialTheme.colorScheme.primary
        PhaseType.PEAKING     -> MaterialTheme.colorScheme.error
        PhaseType.COMPETITION -> MaterialTheme.colorScheme.error
        PhaseType.DELOAD      -> MaterialTheme.colorScheme.secondary
    }

    val waveName = when (phase.type) {
        PhaseType.HYPERTROPHY -> "Wave 1 — 10s (55–65%)"
        PhaseType.STRENGTH    -> "Wave 2 — 8s (65–75%)"
        PhaseType.PEAKING     -> "Wave 3 — 5s (75–82.5%)"
        PhaseType.COMPETITION -> "Wave 4 — 3s (85–92.5%)"
        PhaseType.DELOAD      -> "Deload"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(phaseColor, shape = MaterialTheme.shapes.small)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        waveName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${phase.weekCount} weeks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null
                )
            }

            if (expanded) {
                HorizontalDivider()
                phase.weeks.forEach { week ->
                    WeekSection(week)
                }
            }
        }
    }
}

@Composable
private fun WeekSection(week: TrainingWeek) {
    var expanded by remember { mutableStateOf(week.weekNumber == 1) }

    Column {
        val weekType = when (week.weekNumber) {
            1 -> "Accumulation"
            2 -> "Intensification"
            3 -> "Realization"
            4 -> "Deload"
            else -> "Week ${week.weekNumber}"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Week ${week.weekNumber} — $weekType",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${week.sessions.size} sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        if (expanded) {
            week.sessions.forEach { session ->
                SessionRow(session)
            }
        }
    }
}

private val dayNames = mapOf(
    1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
    5 to "Fri", 6 to "Sat", 7 to "Sun"
)

@Composable
private fun SessionRow(session: TrainingSession) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dayNames[session.dayOfWeek] ?: "Day ${session.dayOfWeek}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(36.dp)
                )
                Text(
                    session.label.ifEmpty { "Session" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${session.exercises.size} exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (expanded) {
                session.exercises.forEach { exercise ->
                    ExerciseRow(exercise)
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(ex: ProgrammedExercise) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            ex.exerciseName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        val setsLabel = if (ex.isAmrap) "${ex.targetSets - 1}×${ex.targetReps} + AMRAP"
                        else "${ex.targetSets}×${ex.targetReps}"
        Text(
            setsLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            "RPE ${ex.targetRpe}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        Text(
            "${ex.suggestedWeightKg.toInt()} kg",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(60.dp)
        )
    }
}
