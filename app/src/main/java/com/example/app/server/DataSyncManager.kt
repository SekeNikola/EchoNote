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
                // First, try to find existing task by title and description to avoid duplicates
                val allTasks = db.taskDao().getAllTasksOnce()
                val existingTask = allTasks.find { 
                    it.title == serverTask.title && it.description == serverTask.body 
                }
                
                if (existingTask != null) {
                    // Update existing task
                    val updatedTask = existingTask.copy(
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.taskDao().update(updatedTask)
                    Log.d("DataSyncManager", "Updated existing task: ${serverTask.title}")
                } else {
                    // Create new task
                    val dbTask = Task(
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        dueDate = System.currentTimeMillis(), // Set to today so it shows up in "Today's Tasks"
                        updatedAt = System.currentTimeMillis()
                    )
                    db.taskDao().insert(dbTask)
                    Log.d("DataSyncManager", "Created new task: ${serverTask.title}")
                }
                
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync task to database", e)
            }
        }
    }
    
    suspend fun syncNoteToDatabase(serverNote: ServerNote) {
        database?.let { db ->
            try {
                // First, try to find existing note by title and content to avoid duplicates
                val allNotes = db.noteDao().getAllNotesOnce() // Assuming this method exists
                val existingNote = allNotes.find { 
                    it.title == serverNote.title && it.transcript == serverNote.body 
                }
                
                if (existingNote != null) {
                    // Update existing note
                    val updatedNote = existingNote.copy(
                        title = serverNote.title,
                        transcript = serverNote.body
                    )
                    db.noteDao().update(updatedNote)
                    Log.d("DataSyncManager", "Updated existing note: ${serverNote.title}")
                } else {
                    // Create new note
                    val dbNote = Note(
                        title = serverNote.title,
                        transcript = serverNote.body, // Using transcript field to store body content
                        createdAt = System.currentTimeMillis()
                    )
                    db.noteDao().insert(dbNote)
                    Log.d("DataSyncManager", "Created new note: ${serverNote.title}")
                }
                
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
    }
}