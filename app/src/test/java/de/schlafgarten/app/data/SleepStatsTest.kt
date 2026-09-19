package de.schlafgarten.app.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepStatsTest {

    private fun entry(date: String, bed: String, wake: String, quality: Int = 0, note: String = "") =
        SleepEntry(wakeDate = date, bedTime = bed, wakeTime = wake, quality = quality, note = note)

    @Test
    fun `current streak counts consecutive goal nights`() {
        val entries = listOf(
            entry("2026-09-16", "2026-09-15T23:00:00", "2026-09-16T07:00:00"),
            entry("2026-09-17", "2026-09-16T23:00:00", "2026-09-17T07:30:00"),
            entry("2026-09-18", "2026-09-17T23:00:00", "2026-09-18T07:00:00"),
        )
        assertEquals(3, SleepStats.currentStreak(entries, 480L, LocalDate.of(2026, 9, 18)))
    }

    @Test
    fun `current streak tolerates missing today`() {
        val entries = listOf(
            entry("2026-09-16", "2026-09-15T23:00:00", "2026-09-16T07:00:00"),
            entry("2026-09-17", "2026-09-16T23:00:00", "2026-09-17T07:00:00"),
        )
        assertEquals(2, SleepStats.currentStreak(entries, 480L, LocalDate.of(2026, 9, 18)))
    }

    @Test
    fun `current streak breaks on short night`() {
        val entries = listOf(
            entry("2026-09-16", "2026-09-15T23:00:00", "2026-09-16T07:00:00"),
            entry("2026-09-17", "2026-09-17T01:00:00", "2026-09-17T05:00:00"),
            entry("2026-09-18", "2026-09-17T23:00:00", "2026-09-18T07:00:00"),
        )
        assertEquals(1, SleepStats.currentStreak(entries, 480L, LocalDate.of(2026, 9, 18)))
    }

    @Test
    fun `longest streak finds best run`() {
        val entries = listOf(
            entry("2026-09-10", "2026-09-09T23:00:00", "2026-09-10T07:00:00"),
            entry("2026-09-11", "2026-09-10T23:00:00", "2026-09-11T07:00:00"),
            entry("2026-09-13", "2026-09-12T23:00:00", "2026-09-13T07:00:00"),
            entry("2026-09-14", "2026-09-13T23:00:00", "2026-09-14T07:00:00"),
            entry("2026-09-15", "2026-09-14T23:00:00", "2026-09-15T07:00:00"),
        )
        assertEquals(3, SleepStats.longestStreak(entries, 480L))
    }

    @Test
    fun `average quality ignores unrated`() {
        val entries = listOf(
            entry("2026-09-16", "2026-09-15T23:00:00", "2026-09-16T07:00:00", quality = 4),
            entry("2026-09-17", "2026-09-16T23:00:00", "2026-09-17T07:00:00", quality = 0),
            entry("2026-09-18", "2026-09-17T23:00:00", "2026-09-18T07:00:00", quality = 2),
        )
        assertEquals(3.0, SleepStats.averageQuality(entries), 0.0)
        assertEquals(0.0, SleepStats.averageQuality(emptyList()), 0.0)
    }

    @Test
    fun `csv escapes quotes and sorts by date`() {
        val entries = listOf(
            entry("2026-09-17", "2026-09-16T23:00:00", "2026-09-17T07:00:00", quality = 5, note = "sagte \"gut\""),
            entry("2026-09-16", "2026-09-15T23:30:00", "2026-09-16T06:30:00"),
        )
        val csv = SleepCsv.build(entries)
        val lines = csv.lines()
        assertEquals(3, lines.size)
        assertTrue(lines[0].startsWith("Aufwachdatum"))
        assertTrue(lines[1].startsWith("2026-09-16;"))
        assertTrue(lines[2].contains("\"sagte \"\"gut\"\"\""))
    }
}
