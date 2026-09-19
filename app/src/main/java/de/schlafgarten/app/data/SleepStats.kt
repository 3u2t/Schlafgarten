package de.schlafgarten.app.data

import android.content.Context
import java.time.LocalDate

const val DEFAULT_GOAL_MINUTES = 480L

object SleepStats {

    fun currentStreak(entries: List<SleepEntry>, goalMinutes: Long, today: LocalDate = LocalDate.now()): Int {
        val byDate = entries.associateBy { it.wakeDate }
        var streak = 0
        var day = today
        if (byDate[day.toString()]?.durationMinutes?.let { it >= goalMinutes } != true) {
            day = day.minusDays(1)
        }
        while (byDate[day.toString()]?.durationMinutes?.let { it >= goalMinutes } == true) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun longestStreak(entries: List<SleepEntry>, goalMinutes: Long): Int {
        val goodDays = entries
            .filter { it.durationMinutes >= goalMinutes }
            .map { LocalDate.parse(it.wakeDate) }
            .toSortedSet()
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        for (day in goodDays) {
            run = if (prev != null && prev.plusDays(1) == day) run + 1 else 1
            if (run > best) best = run
            prev = day
        }
        return best
    }

    fun goalHitRate(entries: List<SleepEntry>, goalMinutes: Long, days: Int = 30): Double {
        if (entries.isEmpty()) return 0.0
        val cutoff = LocalDate.now().minusDays(days.toLong())
        val recent = entries.filter { LocalDate.parse(it.wakeDate).isAfter(cutoff) }
        if (recent.isEmpty()) return 0.0
        return recent.count { it.durationMinutes >= goalMinutes }.toDouble() / recent.size
    }

    fun averageQuality(entries: List<SleepEntry>): Double {
        val rated = entries.filter { it.quality in 1..5 }
        if (rated.isEmpty()) return 0.0
        return rated.map { it.quality }.average()
    }

    fun totalMinutes(entries: List<SleepEntry>): Long = entries.sumOf { it.durationMinutes }

    fun bestNight(entries: List<SleepEntry>): SleepEntry? = entries.maxByOrNull { it.durationMinutes }

    fun worstNight(entries: List<SleepEntry>): SleepEntry? = entries.minByOrNull { it.durationMinutes }
}

object SleepCsv {
    fun build(entries: List<SleepEntry>): String {
        val header = "Aufwachdatum,Ins Bett,Aufgestanden,Dauer (Min),Dauer (Std),Qualität (1-5),Notiz"
        val rows = entries.sortedBy { it.wakeDate }.map { e ->
            val hours = e.durationMinutes / 60.0
            listOf(
                e.wakeDate,
                e.bedTime,
                e.wakeTime,
                e.durationMinutes.toString(),
                "%.2f".format(hours).replace('.', ','),
                if (e.quality in 1..5) e.quality.toString() else "",
                "\"${e.note.replace("\"", "\"\"")}\"",
            ).joinToString(";")
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}

class GoalStore(context: Context) {
    private val prefs = context.getSharedPreferences("schlafgarten_settings", Context.MODE_PRIVATE)

    fun getGoalMinutes(): Long = prefs.getLong("goal_minutes", DEFAULT_GOAL_MINUTES)

    fun setGoalMinutes(minutes: Long) {
        prefs.edit().putLong("goal_minutes", minutes).apply()
    }
}
