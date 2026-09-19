package de.schlafgarten.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.schlafgarten.app.data.SleepEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun EntryDialog(
    initial: SleepEntry?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (SleepEntry) -> Unit,
    onDelete: ((SleepEntry) -> Unit)? = null,
    defaultDate: LocalDate = LocalDate.now(),
) {
    val initialDate = initial?.wakeDate ?: defaultDate.toString()
    var date by remember { mutableStateOf(initialDate) }
    var bed by remember { mutableStateOf(formatTimeOrDefault(initial?.bedTime, "23:00")) }
    var wake by remember { mutableStateOf(formatTimeOrDefault(initial?.wakeTime, "07:00")) }
    var quality by remember { mutableIntStateOf(initial?.quality?.takeIf { it in 1..5 } ?: 0) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "Schlaf eintragen" else "Eintrag bearbeiten")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Aufwachsdatum (JJJJ-MM-TT)") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bed,
                        onValueChange = { bed = it },
                        label = { Text("Ins Bett (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = wake,
                        onValueChange = { wake = it },
                        label = { Text("Aufgestanden (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        "Qualität:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    (1..5).forEach { star ->
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "$star von 5 Sternen",
                            tint = if (star <= quality) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { quality = if (quality == star) 0 else star },
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 280) note = it },
                    label = { Text("Notiz (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    val (parseError, entry) = parseEntry(date, bed, wake)
                    if (parseError == null && entry != null) {
                        error = null
                        onSave(entry.copy(quality = quality, note = note.trim()))
                    } else {
                        error = parseError ?: "Ungültige Eingabe."
                    }
                },
            ) { Text("Speichern") }
        },
        dismissButton = {
            Row {
                if (initial != null && onDelete != null) {
                    TextButton(
                        enabled = !busy,
                        onClick = { onDelete(initial) },
                    ) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Abbrechen") }
            }
        },
    )
}

internal fun parseEntry(dateText: String, bedText: String, wakeText: String): Pair<String?, SleepEntry?> {
    val date = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull()
        ?: return "Datum muss im Format JJJJ-MM-TT sein, z.B. 2026-09-17." to null
    val bedTime = parseTime(bedText) ?: return "Bettgehzeit muss HH:MM sein, z.B. 23:15." to null
    val wakeTime = parseTime(wakeText) ?: return "Aufstehzeit muss HH:MM sein, z.B. 07:30." to null
    val now = LocalDateTime.now()
    var bed = LocalDateTime.of(date, bedTime)
    val wake = LocalDateTime.of(date, wakeTime)
    if (!wake.isAfter(bed)) bed = bed.minusDays(1)
    val error = de.schlafgarten.app.data.validateSleepTimes(bed, wake, now)
    if (error != null) return error to null
    return null to SleepEntry(
        wakeDate = date.toString(),
        bedTime = bed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        wakeTime = wake.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
}

private fun formatTimeOrDefault(isoDateTime: String?, default: String): String {
    if (isoDateTime == null) return default
    return runCatching {
        LocalDateTime.parse(isoDateTime).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrDefault(default)
}

private fun parseTime(text: String): LocalTime? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return LocalTime.of(h, m)
}
