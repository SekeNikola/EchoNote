package com.example.app.server

import com.example.app.data.AppDatabase
import com.example.app.data.Task
import com.example.app.data.Note
import kotlinx.coroutines.*
import android.util.Log
import kotlinx.coroutines.flow.first

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
                // Get notes synchronously by collecting the Flow
                val dbNotes = db.noteDao().getAllNotes().first()
                Log.d("DataSyncManager", "Syncing ${dbNotes.size} notes from database to server")
                
                dbNotes.forEach { dbNote ->
                    val serverNote = ServerNote(
                        id = dbNote.id.toString(),
                        title = dbNote.title,
                        body = dbNote.transcript.ifEmpty { dbNote.snippet }, // Use transcript first, fallback to snippet
                        updatedAt = java.time.Instant.ofEpochMilli(dbNote.createdAt).toString()
                    )
                    KtorServer.addNote(serverNote)
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync notes from database", e)
            }
        }
    }
    
    suspend fun syncTaskToDatabase(serverTask: ServerTask) {
        database?.let { db ->
            try {
                val dbTask = Task(
                    id = if (serverTask.id.toLongOrNull() != null) serverTask.id.toLong() else 0,
                    title = serverTask.title,
                    description = serverTask.body,
                    isCompleted = serverTask.done,
                    dueDate = System.currentTimeMillis() + 86400000, // Default to 1 day from now
                    updatedAt = System.currentTimeMillis()
                )
                
                if (serverTask.id.toLongOrNull() != null) {
                    db.taskDao().update(dbTask)
                } else {
                    db.taskDao().insert(dbTask)
                }
                
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
                    id = if (serverNote.id.toLongOrNull() != null) serverNote.id.toLong() else 0,
                    title = serverNote.title,
                    transcript = serverNote.body, // Using transcript field to store body content
                    createdAt = System.currentTimeMillis()
                )
                
                if (serverNote.id.toLongOrNull() != null) {
                    db.noteDao().update(dbNote)
                } else {
                    db.noteDao().insert(dbNote)
                }
                
                Log.d("DataSyncManager", "Synced note to database: ${serverNote.title}")
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
    }
}