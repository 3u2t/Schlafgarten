package de.schlafgarten.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
fun GoalDialog(
    currentGoalMinutes: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var hours by remember { mutableFloatStateOf(currentGoalMinutes / 60f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schlafziel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Dein Ziel pro Nacht: ${formatHours((hours * 60).toDouble())}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Nächte mit mindestens dieser Dauer zählen für deine Streak und die Zielquote.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = hours,
                    onValueChange = { hours = it },
                    valueRange = 4f..10f,
                    steps = 11,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave((hours * 60).toLong()) }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
