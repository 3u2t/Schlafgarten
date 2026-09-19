package de.schlafgarten.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.schlafgarten.app.data.SleepEntry
import de.schlafgarten.app.data.colorBand
import de.schlafgarten.app.data.durationMinutes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

enum class HeatmapMode { YEAR, MONTH }

private val cellSize = 17.dp
private val cellGap = 3.dp
private val weekDayLabels = listOf("", "Mo", "", "Mi", "", "Fr", "")

private data class ColumnSpec(val monthLabel: String, val days: List<LocalDate?>)

private class GridData(val columns: List<ColumnSpec>)

@Composable
fun SleepHeatmap(
    entries: List<SleepEntry>,
    mode: HeatmapMode,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val grid = remember(entries, mode) { buildGrid(mode) }
    Column(modifier = modifier.horizontalScroll(rememberScrollState())) {
        if (mode == HeatmapMode.YEAR) {
            MonthLabelsRow(grid)
        }
        Row {
            WeekdayLabelsColumn()
            grid.columns.forEach { column ->
                Column {
                    column.days.forEach { day -> SleepCell(day, entries, onDayClick) }
                }
                Spacer(Modifier.width(cellGap))
            }
        }
        Spacer(Modifier.height(8.dp))
        Legend()
    }
}

@Composable
private fun WeekdayLabelsColumn() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        weekDayLabels.forEach { label ->
            Box(
                Modifier.size(cellSize),
                contentAlignment = Alignment.Center,
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthLabelsRow(grid: GridData) {
    Row {
        Spacer(Modifier.size(cellSize))
        grid.columns.forEach { column ->
            Box(Modifier.size(cellSize)) {
                if (column.monthLabel.isNotEmpty()) {
                    Text(
                        column.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(cellGap))
        }
    }
}

@Composable
private fun SleepCell(day: LocalDate?, entries: List<SleepEntry>, onDayClick: (LocalDate) -> Unit) {
    val entry = day?.let { d -> entries.firstOrNull { it.wakeDate == d.toString() } }
    val isFuture = day != null && day.isAfter(LocalDate.now())
    val background = when {
        entry != null -> bandColor(colorBand(entry.durationMinutes))
        day == null || isFuture -> Color.Transparent
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        Modifier
            .size(cellSize)
            .then(if (day == null || isFuture) Modifier else Modifier.clickable { onDayClick(day) })
            .background(background, RoundedCornerShape(4.dp)),
    )
}

@Composable
private fun Legend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            "Weniger",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        (0..4).forEach { band ->
            Box(Modifier.size(cellSize).background(bandColor(band), RoundedCornerShape(4.dp)))
        }
        Text(
            "Mehr",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildGrid(mode: HeatmapMode): GridData {
    val today = LocalDate.now()
    val weeks: List<LocalDate>
    if (mode == HeatmapMode.YEAR) {
        val end = today.with(DayOfWeek.SUNDAY)
        val start = end.minusWeeks(52)
        weeks = (0..52).map { w -> start.plusWeeks(w.toLong()) }
    } else {
        val month = YearMonth.from(today)
        val first = month.atDay(1).with(DayOfWeek.MONDAY)
        val last = month.atEndOfMonth().with(DayOfWeek.SUNDAY)
        weeks = generateSequence(first) { it.plusWeeks(1) }.takeWhile { !it.isAfter(last) }.toList()
    }
    val columns = weeks.map { weekStart ->
        ColumnSpec(
            monthLabel = if (mode == HeatmapMode.YEAR && weekStart.dayOfMonth <= 7) {
                weekStart.month.getDisplayName(JavaTextStyle.SHORT, Locale.GERMAN)
            } else {
                ""
            },
            days = (0..6).map { d -> weekStart.plusDays(d.toLong()) }.map { day ->
                when {
                    day.isAfter(today) -> null
                    mode == HeatmapMode.MONTH && YearMonth.from(day) != YearMonth.from(today) -> null
                    else -> day
                }
            },
        )
    }
    return GridData(columns)
}

@Composable
fun bandColor(band: Int): Color = when (band) {
    0 -> Color(0xFFE05252)
    1 -> Color(0xFFE5A54B)
    2 -> Color(0xFFD9D05A)
    3 -> Color(0xFF8FCB6B)
    else -> Color(0xFF4CAF6D)
}
