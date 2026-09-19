package de.schlafgarten.app.data

import java.time.Duration
import java.time.LocalDateTime

val SleepEntry.durationMinutes: Long
    get() = Duration.between(LocalDateTime.parse(bedTime), LocalDateTime.parse(wakeTime)).toMinutes()

fun validateSleepTimes(bed: LocalDateTime, wake: LocalDateTime, now: LocalDateTime): String? {
    if (bed.isAfter(now) || wake.isAfter(now)) return "Schlafzeiten dürfen nicht in der Zukunft liegen."
    val duration = Duration.between(bed, wake)
    if (duration.isZero || duration.isNegative) return "Die Aufwachzeit muss nach der Einschlafzeit liegen."
    if (duration > Duration.ofHours(24)) return "Die Schlafdauer darf höchstens 24 Stunden betragen."
    return null
}

fun colorBand(minutes: Long): Int = when {
    minutes <= 240 -> 0
    minutes < 360 -> 1
    minutes < 420 -> 2
    minutes < 480 -> 3
    else -> 4
}

fun averageMinutes(entries: List<SleepEntry>): Double =
    if (entries.isEmpty()) 0.0 else entries.map { it.durationMinutes }.average()
