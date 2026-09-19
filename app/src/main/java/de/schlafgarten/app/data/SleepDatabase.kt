package de.schlafgarten.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SleepEntry::class], version = 2, exportSchema = true)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var instance: SleepDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN quality INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }
        }

        fun get(context: Context): SleepDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SleepDatabase::class.java,
                "sleep.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
