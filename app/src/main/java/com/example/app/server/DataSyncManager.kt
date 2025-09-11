package com.example.app.server

import com.example.app.data.AppDatabase
import com.example.app.data.Task
import com.example.app.data.Note
import kotlinx.coroutines.*
import android.util.Log
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.json.JSONArray

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
                    // Extract unified content from snippet (which contains our JSON structure)
                    val unifiedContent = try {
                        val json = org.json.JSONObject(dbNote.snippet)
                        val text = json.optString("text", "")
                        val summary = json.optString("summary", "")
                        
                        when {
                            summary.isNotEmpty() && text.isNotEmpty() -> "$summary\n\n$text"
                            summary.isNotEmpty() -> summary
                            text.isNotEmpty() -> text
                            else -> dbNote.transcript.ifEmpty { dbNote.snippet }
                        }
                    } catch (e: Exception) {
                        // Fallback to transcript or snippet if JSON parsing fails
                        dbNote.transcript.ifEmpty { dbNote.snippet }
                    }
                    
                    val serverNote = ServerNote(
                        id = dbNote.id.toString(),
                        title = dbNote.title,
                        body = unifiedContent,
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
        Log.d("DataSyncManager", "=== SYNC TASK TO DATABASE STARTED ===")
        Log.d("DataSyncManager", "ServerTask title: '${serverTask.title}'")
        Log.d("DataSyncManager", "ServerTask body: '${serverTask.body}'")
        database?.let { db ->
            try {
                // First, try to find existing task by title and description to avoid duplicates
                val allTasks = db.taskDao().getAllTasksOnce()
                Log.d("DataSyncManager", "Total tasks in database: ${allTasks.size}")
                
                var existingTask: Task? = null
                
                // Try exact match first
                existingTask = allTasks.find { 
                    Log.d("DataSyncManager", "Comparing task: '${it.title}' | '${it.description}' vs '${serverTask.title}' | '${serverTask.body}'")
                    it.title == serverTask.title && it.description == serverTask.body 
                }
                
                // If no exact match, try trimmed match
                if (existingTask == null) {
                    existingTask = allTasks.find { 
                        it.title.trim() == serverTask.title.trim() && it.description.trim() == serverTask.body.trim()
                    }
                    if (existingTask != null) {
                        Log.d("DataSyncManager", "Found task match with trimmed content")
                    }
                }
                
                if (existingTask != null) {
                    Log.d("DataSyncManager", "Found existing task to update: ${existingTask.title} (ID: ${existingTask.id})")
                    // Update existing task
                    val updatedTask = existingTask.copy(
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.taskDao().update(updatedTask)
                    Log.d("DataSyncManager", "Successfully updated existing task: ${serverTask.title}")
                } else {
                    Log.d("DataSyncManager", "No existing task found - creating new task: ${serverTask.title}")
                    // Create new task
                    val dbTask = Task(
                        title = serverTask.title,
                        description = serverTask.body,
                        isCompleted = serverTask.done,
                        dueDate = System.currentTimeMillis(), // Set to today so it shows up in "Today's Tasks"
                        updatedAt = System.currentTimeMillis()
                    )
                    val insertedId = db.taskDao().insert(dbTask)
                    Log.d("DataSyncManager", "Created new task with ID: $insertedId")
                }
                
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync task to database", e)
            }
        }
        Log.d("DataSyncManager", "=== SYNC TASK TO DATABASE COMPLETED ===")
    }
    
    suspend fun syncNoteToDatabase(serverNote: ServerNote) {
        Log.d("DataSyncManager", "=== SYNC NOTE TO DATABASE STARTED ===")
        Log.d("DataSyncManager", "ServerNote title: '${serverNote.title}'")
        Log.d("DataSyncManager", "ServerNote body: '${serverNote.body}'")
        database?.let { db ->
            try {
                // Find existing note by comparing title and content to avoid duplicates
                val allNotes = db.noteDao().getAllNotesOnce()
                Log.d("DataSyncManager", "Total notes in database: ${allNotes.size}")
                
                var existingNote: Note? = null
                
                // First try exact title match (case-sensitive)
                existingNote = allNotes.find { note ->
                    Log.d("DataSyncManager", "Comparing titles: '${note.title}' vs '${serverNote.title}'")
                    note.title == serverNote.title
                }
                
                // If no exact title match, try trimmed title match
                if (existingNote == null) {
                    existingNote = allNotes.find { note ->
                        note.title.trim() == serverNote.title.trim()
                    }
                    if (existingNote != null) {
                        Log.d("DataSyncManager", "Found match with trimmed titles")
                    }
                }
                
                // If still no match, try content-based matching
                if (existingNote == null) {
                    existingNote = allNotes.find { note ->
                        try {
                            val json = org.json.JSONObject(note.snippet)
                            val text = json.optString("text", "").trim()
                            val summary = json.optString("summary", "").trim()
                            val serverBodyTrimmed = serverNote.body.trim()
                            
                            Log.d("DataSyncManager", "Content matching - text: '$text', summary: '$summary', serverBody: '$serverBodyTrimmed'")
                            
                            // Check if server body matches existing text or summary
                            text == serverBodyTrimmed || summary == serverBodyTrimmed
                        } catch (e: Exception) {
                            Log.d("DataSyncManager", "Error parsing note snippet for content matching: ${e.message}")
                            false
                        }
                    }
                    if (existingNote != null) {
                        Log.d("DataSyncManager", "Found match with content-based matching")
                    }
                }
                
                if (existingNote != null) {
                    Log.d("DataSyncManager", "Found existing note to update: ${existingNote.title} (ID: ${existingNote.id})")
                    
                    // Update existing note - store WebUI content as unified snippet
                    val json = org.json.JSONObject()
                    json.put("text", serverNote.body)
                    json.put("summary", serverNote.body) // Store same content as both for consistency
                    
                    // Preserve existing tasks if any
                    try {
                        val existingJson = org.json.JSONObject(existingNote.snippet)
                        if (existingJson.has("tasks")) {
                            json.put("tasks", existingJson.getJSONArray("tasks"))
                            Log.d("DataSyncManager", "Preserved existing tasks")
                        }
                    } catch (e: Exception) {
                        // No existing tasks or invalid JSON, add empty tasks array
                        json.put("tasks", org.json.JSONArray())
                        Log.d("DataSyncManager", "Added empty tasks array")
                    }
                    
                    val updatedNote = existingNote.copy(
                        title = serverNote.title,
                        snippet = json.toString() // Store unified content in snippet as JSON
                    )
                    db.noteDao().update(updatedNote)
                    Log.d("DataSyncManager", "Successfully updated existing note: ${serverNote.title}")
                } else {
                    Log.d("DataSyncManager", "No existing note found - creating new note from WebUI: ${serverNote.title}")
                    
                    // Create new note
                    val json = org.json.JSONObject()
                    json.put("text", serverNote.body)
                    json.put("summary", serverNote.body)
                    json.put("tasks", org.json.JSONArray()) // Empty tasks array
                    
                    val dbNote = Note(
                        title = serverNote.title,
                        snippet = json.toString(), // Store content in snippet as JSON
                        transcript = "", // Keep transcript empty for WebUI-created notes
                        createdAt = System.currentTimeMillis()
                    )
                    val insertedId = db.noteDao().insert(dbNote)
                    Log.d("DataSyncManager", "Created new note with ID: $insertedId")
                }
                
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to sync note to database", e)
            }
        }
        Log.d("DataSyncManager", "=== SYNC NOTE TO DATABASE COMPLETED ===")
    }
    
    suspend fun deleteNoteFromDatabase(serverId: String) {
        database?.let { db ->
            try {
                // Try to find the note by server ID (stored as string)
                val allNotes = db.noteDao().getAllNotesOnce()
                val noteToDelete = allNotes.find { it.id.toString() == serverId }
                
                if (noteToDelete != null) {
                    db.noteDao().deleteById(noteToDelete.id)
                    Log.d("DataSyncManager", "Deleted note with server ID: $serverId")
                } else {
                    Log.w("DataSyncManager", "Note not found for deletion: $serverId")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to delete note from database", e)
            }
        }
    }
    
    suspend fun deleteTaskFromDatabase(serverId: String) {
        database?.let { db ->
            try {
                // Try to find the task by server ID (stored as string)
                val allTasks = db.taskDao().getAllTasksOnce()
                val taskToDelete = allTasks.find { it.id.toString() == serverId }
                
                if (taskToDelete != null) {
                    db.taskDao().deleteById(taskToDelete.id)
                    Log.d("DataSyncManager", "Deleted task with server ID: $serverId")
                } else {
                    Log.w("DataSyncManager", "Task not found for deletion: $serverId")
                }
            } catch (e: Exception) {
                Log.e("DataSyncManager", "Failed to delete task from database", e)
            }
        }
    }
}