package com.example.app.server

import com.example.app.data.AppDatabase
import com.example.app.data.Task
import com.example.app.data.Note
import kotlinx.coroutines.*
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

object DataSyncManager {
    private var database: AppDatabase? = null
    
    // Helper function to convert timestamp to ISO string (API 24 compatible)
    private fun timestampToIsoString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
    
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
                        updatedAt = timestampToIsoString(dbTask.updatedAt)
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
                        updatedAt = timestampToIsoString(dbNote.createdAt)
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
                val noteId = serverNote.id.toLongOrNull()
                
                if (noteId != null && noteId > 0) {
                    // Update existing note
                    val existingNote = db.noteDao().getNoteById(noteId).firstOrNull()
                    if (existingNote != null) {
                        val updatedNote = existingNote.copy(
                            title = serverNote.title,
                            transcript = serverNote.body,
                            snippet = serverNote.body
                        )
                        db.noteDao().update(updatedNote)
                        Log.d("DataSyncManager", "Updated existing note: ${serverNote.title}")
                    } else {
                        // Note ID doesn't exist, treat as new
                        val newNote = Note(
                            id = 0, // Let Room generate new ID
                            title = serverNote.title,
                            transcript = serverNote.body,
                            snippet = serverNote.body,
                            createdAt = System.currentTimeMillis()
                        )
                        db.noteDao().insert(newNote)
                        Log.d("DataSyncManager", "Inserted new note (ID not found): ${serverNote.title}")
                    }
                } else {
                    // No valid ID, create new note
                    val newNote = Note(
                        id = 0, // Let Room generate new ID
                        title = serverNote.title,
                        transcript = serverNote.body,
                        snippet = serverNote.body,
                        createdAt = System.currentTimeMillis()
                    )
                    db.noteDao().insert(newNote)
                    Log.d("DataSyncManager", "Inserted new note: ${serverNote.title}")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
    }
}