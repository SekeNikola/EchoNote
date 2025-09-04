package com.example.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(entities = [Note::class, Reminder::class, NoteCrossRef::class], version = 2)
@TypeConverters(Converters::class, ReminderConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "logion_db"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN audioPath TEXT")
            }
        }
    }
}
