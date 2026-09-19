package de.schlafgarten.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries ORDER BY wakeDate DESC")
    fun observeAll(): Flow<List<SleepEntry>>

    @Upsert
    suspend fun upsert(entry: SleepEntry)

    @Delete
    suspend fun delete(entry: SleepEntry)
}
