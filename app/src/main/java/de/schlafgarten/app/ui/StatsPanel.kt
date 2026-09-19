package de.schlafgarten.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.schlafgarten.app.data.SleepEntry
import de.schlafgarten.app.data.SleepStats
import de.schlafgarten.app.data.averageMinutes
import de.schlafgarten.app.data.colorBand
import de.schlafgarten.app.data.durationMinutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, dd. MMM", Locale.GERMAN)

@Composable
fun StatsPanel(entries: List<SleepEntry>, goalMinutes: Long) {
    val today = LocalDate.now()
    val week = remember(entries) {
        entries.filter { LocalDate.parse(it.wakeDate).isAfter(today.minusDays(7)) }
    }
    val month = remember(entries) {
        entries.filter { LocalDate.parse(it.wakeDate).isAfter(today.minusDays(30)) }
    }
    val streak = remember(entries, goalMinutes) { SleepStats.currentStreak(entries, goalMinutes) }
    val best = remember(entries, goalMinutes) { SleepStats.longestStreak(entries, goalMinutes) }
    val hitRate = remember(entries, goalMinutes) { SleepStats.goalHitRate(entries, goalMinutes) }
    val total = remember(entries) { SleepStats.totalMinutes(entries) }
    val avgQuality = remember(entries) { SleepStats.averageQuality(entries) }
    val bestNight = remember(entries) { SleepStats.bestNight(entries) }
    val worstNight = remember(entries) { SleepStats.worstNight(entries) }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Streak",
                value = if (streak == 1) "1 Nacht" else "$streak Nächte",
                sub = "Ziel: ${formatHours(goalMinutes.toDouble())}",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Beste Streak",
                value = if (best == 1) "1 Nacht" else "$best Nächte",
                sub = "Zielquote 30 Tage: ${(hitRate * 100).toInt()} %",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Zielquote (30 Tage)", style = MaterialTheme.typography.labelMedium)
                Text("${(hitRate * 100).toInt()} %", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { hitRate.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Letzte 7 Tage",
                value = formatHours(averageMinutes(week)),
                sub = "${week.size} Nächte",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Letzte 30 Tage",
                value = formatHours(averageMinutes(month)),
                sub = "${month.size} Nächte",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Gesamt",
                value = "${total / 60} Std",
                sub = "${entries.size} Nächte erfasst",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Ø Qualität",
                value = if (avgQuality > 0) "%.1f ★".format(avgQuality) else "–",
                sub = "aus Sterne-Bewertungen",
                modifier = Modifier.weight(1f),
            )
        }
        if (bestNight != null && worstNight != null && entries.size >= 2) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Beste Nacht",
                    value = formatHours(bestNight.durationMinutes.toDouble()),
                    sub = LocalDate.parse(bestNight.wakeDate).format(dayFormatter),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Schwächste Nacht",
                    value = formatHours(worstNight.durationMinutes.toDouble()),
                    sub = LocalDate.parse(worstNight.wakeDate).format(dayFormatter),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Letzte Nächte", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        val recent = entries.sortedByDescending { it.wakeDate }.take(5)
        recent.forEach { entry ->
            RecentRow(entry)
            Spacer(Modifier.height(6.dp))
        }
        if (recent.isEmpty()) {
            Text(
                "Noch keine Einträge – tippe auf +, um deine erste Nacht einzutragen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentRow(entry: SleepEntry) {
    val minutes = entry.durationMinutes
    val color = bandColor(colorBand(minutes))
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
            Text(LocalDate.parse(entry.wakeDate).format(dayFormatter), modifier = Modifier.weight(1f))
            if (entry.quality in 1..5) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text("${entry.quality}", style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(formatHours(minutes.toDouble()), style = MaterialTheme.typography.titleSmall)
        }
        if (entry.note.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                entry.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun formatHours(minutes: Double): String {
    val h = (minutes / 60).toInt()
    val m = (minutes % 60).toInt()
    return "${h}h ${m.toString().padStart(2, '0')}min"
}
