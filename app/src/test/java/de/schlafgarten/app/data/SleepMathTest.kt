package de.schlafgarten.app.data

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SleepMathTest {

    private val now: LocalDateTime = LocalDateTime.of(2026, 9, 17, 8, 0)

    private fun entry(bed: String, wake: String, date: String = "2026-09-17") =
        SleepEntry(wakeDate = date, bedTime = bed, wakeTime = wake)

    @Test
    fun `duration crosses midnight overnight`() {
        val e = entry("2026-09-16T23:00:00", "2026-09-17T07:30:00")
        assertEquals(510L, e.durationMinutes)
    }

    @Test
    fun `duration across year boundary`() {
        val e = entry("2025-12-31T22:00:00", "2026-01-01T06:00:00", date = "2026-01-01")
        assertEquals(480L, e.durationMinutes)
    }

    @Test
    fun `rejects future wake time`() {
        val error = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 17, 6, 0),
            wake = LocalDateTime.of(2026, 9, 17, 9, 0),
            now = now
        )
        assertEquals("Schlafzeiten dürfen nicht in der Zukunft liegen.", error)
    }

    @Test
    fun `rejects future bed time`() {
        val error = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 17, 23, 0),
            wake = LocalDateTime.of(2026, 9, 18, 7, 0),
            now = now
        )
        assertEquals("Schlafzeiten dürfen nicht in der Zukunft liegen.", error)
    }

    @Test
    fun `rejects zero and negative duration`() {
        val zero = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 16, 23, 0),
            wake = LocalDateTime.of(2026, 9, 16, 23, 0),
            now = now
        )
        val negative = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 17, 7, 0),
            wake = LocalDateTime.of(2026, 9, 17, 6, 0),
            now = now
        )
        assertEquals("Die Aufwachzeit muss nach der Einschlafzeit liegen.", zero)
        assertEquals("Die Aufwachzeit muss nach der Einschlafzeit liegen.", negative)
    }

    @Test
    fun `rejects duration over 24 hours`() {
        val error = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 16, 7, 0),
            wake = LocalDateTime.of(2026, 9, 17, 8, 0),
            now = now
        )
        assertEquals("Die Schlafdauer darf höchstens 24 Stunden betragen.", error)
    }

    @Test
    fun `accepts valid past interval`() {
        val error = validateSleepTimes(
            bed = LocalDateTime.of(2026, 9, 16, 23, 0),
            wake = LocalDateTime.of(2026, 9, 17, 7, 0),
            now = now
        )
        assertNull(error)
    }

    @Test
    fun `color band thresholds`() {
        assertEquals(0, colorBand(240))
        assertEquals(1, colorBand(241))
        assertEquals(1, colorBand(359))
        assertEquals(2, colorBand(360))
        assertEquals(2, colorBand(419))
        assertEquals(3, colorBand(420))
        assertEquals(3, colorBand(479))
        assertEquals(4, colorBand(480))
        assertEquals(4, colorBand(600))
    }

    @Test
    fun `average of entries ignores empty list`() {
        assertEquals(0.0, averageMinutes(emptyList()), 0.0)
    }

    @Test
    fun `average of entries`() {
        val entries = listOf(
            entry("2026-09-15T23:00:00", "2026-09-16T07:00:00", date = "2026-09-16"),
            entry("2026-09-16T23:30:00", "2026-09-17T06:30:00")
        )
        assertEquals(450.0, averageMinutes(entries), 0.0)
    }
}
