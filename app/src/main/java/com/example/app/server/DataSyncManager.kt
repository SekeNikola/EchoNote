package com.example.app.server

import com.example.app.data.AppDatabase
import com.example.app.data.Task
import com.example.app.data.Note
import kotlinx.coroutines.*
import android.util.Log

object DataSyncManager {
    private var database: AppDatabase? = null
    
    fun initialize(db: AppDatabase) {
        database = db
        Log.d("DataSyncManager", "Database sync manager initialized")
    }
    
    suspend fun syncTasksFromDatabase() {
        database?.let { db ->
            try {
                val dbTasks = db.taskDao().getAllTasksOnce()
                Log.d("DataSyncManager", "Syncing ${dbTasks.size} tasks from database to server")
                
                dbTasks.forEach { dbTask ->
                    val serverTask = ServerTask(
                        id = dbTask.id.toString(),
                        title = dbTask.title,
                        body = dbTask.description,
                        done = dbTask.isCompleted,
                        updatedAt = java.time.Instant.ofEpochMilli(dbTask.updatedAt).toString()
                    )
                    KtorServer.addTask(serverTask)
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync tasks from database", e)
            }
        }
    }
    
    suspend fun syncNotesFromDatabase() {
        database?.let { db ->
            try {
                // Since getAllNotes() returns Flow, we need to collect it
                // For now, let's skip this until we can add a synchronous method
                Log.d("DataSyncManager", "Note sync from database skipped - requires Flow handling")
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync notes from database", e)
            }
        }
    }
    
    suspend fun syncTaskToDatabase(serverTask: ServerTask) {
        database?.let { db ->
            try {
                val dbTask = Task(
                    id = 0, // Let database auto-generate ID for new tasks
                    title = serverTask.title,
                    description = serverTask.body,
                    isCompleted = serverTask.done,
                    dueDate = System.currentTimeMillis(), // Set to today so it shows in "Today's Tasks"
                    updatedAt = System.currentTimeMillis()
                )
                
                // Always insert new tasks since server uses UUID and database uses Long
                db.taskDao().insert(dbTask)
                
                Log.d("DataSyncManager", "Synced task to database: ${serverTask.title}")
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync task to database", e)
            }
        }
    }
    
    suspend fun syncNoteToDatabase(serverNote: ServerNote) {
        database?.let { db ->
            try {
                val dbNote = Note(
                    id = 0, // Let database auto-generate ID for new notes
                    title = serverNote.title,
                    transcript = serverNote.body, // Using transcript field to store body content
                    createdAt = System.currentTimeMillis()
                )
                
                // Always insert new notes since server uses UUID and database uses Long
                db.noteDao().insert(dbNote)
                
                Log.d("DataSyncManager", "Synced note to database: ${serverNote.title}")
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
    }
}
