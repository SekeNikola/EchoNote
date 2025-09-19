package com.example.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(entities = [Note::class, Reminder::class, NoteCrossRef::class, Task::class, ChatMessage::class], version = 9)
@TypeConverters(Converters::class, ReminderConverters::class, TaskItemListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun taskDao(): TaskDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "logion_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN audioPath TEXT")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create tasks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        priority TEXT NOT NULL DEFAULT 'Medium',
                        dueDate INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create chat_messages table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        content TEXT NOT NULL,
                        isUser INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        sessionId TEXT
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add duration column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN duration TEXT NOT NULL DEFAULT ''")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add imageUri column to chat_messages table
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN imageUri TEXT")
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add serverId and imagePath columns to notes table
                database.execSQL("ALTER TABLE notes ADD COLUMN serverId TEXT")
                database.execSQL("ALTER TABLE notes ADD COLUMN imagePath TEXT")
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Recreate reminders table with new structure
                database.execSQL("DROP TABLE IF EXISTS reminders")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        reminderTime INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        noteId INTEGER
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add checkboxItems column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN checkboxItems TEXT NOT NULL DEFAULT '[]'")
                
                // Check if serverId column exists before adding it
                val cursor = database.query("PRAGMA table_info(tasks)")
                var hasServerId = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(1) // Column name is at index 1
                    if (columnName == "serverId") {
                        hasServerId = true
                        break
                    }
                }
                cursor.close()
                
                // Add serverId column only if it doesn't exist
                if (!hasServerId) {
                    database.execSQL("ALTER TABLE tasks ADD COLUMN serverId TEXT")
                }
            }
        }
        
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // This migration ensures clean state - no changes needed
                // All necessary columns should already exist from previous migrations
            }
        }
    }
}
