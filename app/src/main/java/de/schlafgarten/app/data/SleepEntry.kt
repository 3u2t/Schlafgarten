package de.schlafgarten.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey val wakeDate: String,
    val bedTime: String,
    val wakeTime: String,
    val quality: Int = 0,
    val note: String = ""
)
