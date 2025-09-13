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
                        id = dbTask.serverId ?: dbTask.id.toString(), // Use serverId if available, fallback to Long ID
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
                // First try to find by serverId (exact match for web UI tasks)
                val existingTasks = db.taskDao().getAllTasksOnce()
                var existingTask = existingTasks.find { it.serverId == serverTask.id }
                
                // If no exact serverId match, try to match by title and content (for backwards compatibility)
                if (existingTask == null) {
                    existingTask = existingTasks.find { 
                        it.title == serverTask.title && 
                        (serverTask.body.isEmpty() || it.description == serverTask.body)
                    }
                }
                
                if (existingTask != null) {
                    // Update existing task - preserve original creation time and ID, but update serverId
                    val updatedTask = existingTask.copy(
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        updatedAt = System.currentTimeMillis(),
                        serverId = serverTask.id // Ensure we store the server ID for future updates
                    )
                    db.taskDao().update(updatedTask)
                    Log.d("DataSyncManager", "Updated existing task: ${existingTask.title} -> ${serverTask.title}")
                } else {
                    // Insert new task - it's genuinely new
                    // Parse the timestamp from the server task if available
                    val timestamp = try {
                        serverTask.updatedAt.toLongOrNull() ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    
                    val dbTask = Task(
                        id = 0, // Let the database generate the ID
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        dueDate = timestamp, // Use the provided timestamp as due date
                        updatedAt = timestamp, // Use the provided timestamp as updated time
                        serverId = serverTask.id // Store the server UUID for future sync
                    )
                    db.taskDao().insert(dbTask)
                    Log.d("DataSyncManager", "Inserted new task: ${serverTask.title} with timestamp: $timestamp")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync task to database", e)
            }
        }
    }
    
    suspend fun syncNoteToDatabase(serverNote: ServerNote) {
        database?.let { db ->
            try {
                // First, try to find an existing note by serverId for exact matching
                val existingNotes = db.noteDao().getAllNotes().first()
                var existingNote = existingNotes.find { it.serverId == serverNote.id }
                
                if (existingNote != null) {
                    // Update existing note found by serverId
                    val updatedNote = existingNote.copy(
                        title = serverNote.title,
                        snippet = serverNote.body, // Store in snippet field for display
                        transcript = serverNote.body, // Also store in transcript for compatibility
                        serverId = serverNote.id
                    )
                    db.noteDao().update(updatedNote)
                    Log.d("DataSyncManager", "Updated existing note by serverId: ${serverNote.title}")
                } else {
                    // Fallback: try to find by title and content to avoid duplicates
                    existingNote = existingNotes.find { 
                        it.title == serverNote.title && (it.transcript == serverNote.body || it.snippet == serverNote.body)
                    }
                    
                    if (existingNote != null) {
                        // Update existing note and set serverId
                        val updatedNote = existingNote.copy(
                            title = serverNote.title,
                            snippet = serverNote.body, // Store in snippet field for display
                            transcript = serverNote.body, // Also store in transcript for compatibility
                            serverId = serverNote.id
                        )
                        db.noteDao().update(updatedNote)
                        Log.d("DataSyncManager", "Updated existing note and set serverId: ${serverNote.title}")
                    } else {
                        // Insert new note
                        val dbNote = Note(
                            id = 0, // Let the database generate the ID
                            title = serverNote.title,
                            snippet = serverNote.body, // Store in snippet field for display
                            transcript = serverNote.body, // Also store in transcript for compatibility
                            createdAt = System.currentTimeMillis(),
                            serverId = serverNote.id
                        )
                        db.noteDao().insert(dbNote)
                        Log.d("DataSyncManager", "Inserted new note: ${serverNote.title}")
                    }
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
    }
    
    suspend fun handleTaskDeletion(taskTitle: String) {
        database?.let { db ->
            try {
                // Find task by title and delete it
                val existingTasks = db.taskDao().getAllTasksOnce()
                val taskToDelete = existingTasks.find { it.title == taskTitle }
                
                if (taskToDelete != null) {
                    db.taskDao().deleteById(taskToDelete.id)
                    Log.d("DataSyncManager", "Deleted task: ${taskToDelete.title}")
                } else {
                    Log.w("DataSyncManager", "Task not found for deletion: $taskTitle")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to delete task", e)
            }
        }
    }
    
    suspend fun handleNoteDeletion(noteTitle: String) {
        database?.let { db ->
            try {
                // Find note by title and delete it
                val existingNotes = db.noteDao().getAllNotes().first()
                val noteToDelete = existingNotes.find { it.title == noteTitle }
                
                if (noteToDelete != null) {
                    db.noteDao().deleteById(noteToDelete.id)
                    Log.d("DataSyncManager", "Deleted note: ${noteToDelete.title}")
                } else {
                    Log.w("DataSyncManager", "Note not found for deletion: $noteTitle")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to delete note", e)
            }
        }
    }
}