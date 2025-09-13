package com.example.app.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

@Serializable
data class ServerTask(
    val id: String,
    val title: String,
    val body: String = "",
    val done: Boolean = false,
    val updatedAt: String
)

@Serializable
data class ServerNote(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: String
)

@Serializable
data class TaskRequest(
    val title: String,
    val body: String = "",
    val done: Boolean = false,
    val timestamp: Long? = null
)

@Serializable
data class NoteRequest(
    val title: String,
    val body: String,
    val timestamp: Long? = null
)

@Serializable
data class SyncData(
    val type: String,
    val data: String
)

object KtorServer {
    private val tasks = ConcurrentHashMap<String, ServerTask>()
    private val notes = ConcurrentHashMap<String, ServerNote>()
    private val connections = mutableSetOf<DefaultWebSocketSession>()
    
    private fun getCurrentTimestamp(): String = Instant.now().toString()
    
    // Methods for external access and sync
    fun addTask(task: ServerTask) {
        tasks[task.id] = task
    }
    
    fun addNote(note: ServerNote) {
        notes[note.id] = note
    }
    
    suspend fun updateNoteWithBroadcast(note: ServerNote) {
        notes[note.id] = note
        
        // Sync to Android database
        try {
            DataSyncManager.syncNoteToDatabase(note)
        } catch (e: Exception) {
            Log.w("KtorServer", "Failed to sync note to database", e)
        }
        
        // Broadcast to WebSocket clients
        broadcastSync("note_updated", Json.encodeToString(ServerNote.serializer(), note))
    }
    
    suspend fun updateTaskWithBroadcast(task: ServerTask) {
        tasks[task.id] = task
        
        // Sync to Android database
        try {
            DataSyncManager.syncTaskToDatabase(task)
        } catch (e: Exception) {
            Log.w("KtorServer", "Failed to sync task to database", e)
        }
        
        // Broadcast to WebSocket clients
        broadcastSync("task_updated", Json.encodeToString(ServerTask.serializer(), task))
    }
    
    suspend fun addNoteWithBroadcast(note: ServerNote) {
        notes[note.id] = note
        
        // Sync to Android database
        try {
            DataSyncManager.syncNoteToDatabase(note)
        } catch (e: Exception) {
            Log.w("KtorServer", "Failed to sync note to database", e)
        }
        
        // Broadcast to WebSocket clients
        broadcastSync("note_added", Json.encodeToString(ServerNote.serializer(), note))
    }
    
    suspend fun addTaskWithBroadcast(task: ServerTask) {
        tasks[task.id] = task
        
        // Sync to Android database
        try {
            DataSyncManager.syncTaskToDatabase(task)
        } catch (e: Exception) {
            Log.w("KtorServer", "Failed to sync task to database", e)
        }
        
        // Broadcast to WebSocket clients
        broadcastSync("task_added", Json.encodeToString(ServerTask.serializer(), task))
    }
    
    suspend fun deleteNoteWithBroadcast(noteId: String) {
        notes.remove(noteId)
        
        // Broadcast to WebSocket clients
        broadcastSync("note_deleted", noteId)
    }
    
    suspend fun deleteNoteWithBroadcastByTitle(noteTitle: String) {
        // Remove note by title from server memory
        val noteToRemove = notes.values.find { it.title == noteTitle }
        if (noteToRemove != null) {
            notes.remove(noteToRemove.id)
        }
        
        // Broadcast to WebSocket clients with note title
        broadcastSync("note_deleted", noteTitle)
    }
    
    suspend fun deleteTaskWithBroadcast(taskId: String) {
        val taskToDelete = tasks.remove(taskId)
        
        if (taskToDelete != null) {
            // Broadcast to WebSocket clients with task title for mobile app sync
            broadcastSync("task_deleted", taskToDelete.title)
        } else {
            // If task not found in server memory, broadcast the ID as fallback
            Log.w("KtorServer", "Task not found in server memory for deletion: $taskId")
            broadcastSync("task_deleted", taskId)
        }
    }
    
    suspend fun deleteTaskWithBroadcastByTitle(taskTitle: String) {
        // Remove task by title from server memory
        val taskToRemove = tasks.values.find { it.title == taskTitle }
        if (taskToRemove != null) {
            tasks.remove(taskToRemove.id)
        }
        
        // Broadcast to WebSocket clients with task title
        broadcastSync("task_deleted", taskTitle)
    }
    
    fun clearData() {
        tasks.clear()
        notes.clear()
    }
    
    suspend fun start() {
        embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
            install(CORS) {
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowMethod(HttpMethod.Patch)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
                anyHost() // Allow all hosts for development
            }
            
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            
            routing {
                
                get("/") {
                    call.respondText("EchoNote Server is running!")
                }
                
                get("/tasks") {
                    call.respond(tasks.values.toList())
                }
                
                post("/tasks") {
                    val request = call.receive<TaskRequest>()
                    val task = ServerTask(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = request.body,
                        done = request.done,
                        updatedAt = request.timestamp?.toString() ?: getCurrentTimestamp()
                    )
                    tasks[task.id] = task
                    
                    // Sync to Android database
                    try {
                        DataSyncManager.syncTaskToDatabase(task)
                    } catch (e: Exception) {
                        Log.w("KtorServer", "Failed to sync task to database", e)
                    }
                    
                    // Broadcast to WebSocket clients
                    broadcastSync("task_added", Json.encodeToString(ServerTask.serializer(), task))
                    
                    call.respond(task)
                }
                
                put("/tasks/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respondText("Missing task ID", status = HttpStatusCode.BadRequest)
                    val request = call.receive<TaskRequest>()
                    val updatedTask = ServerTask(
                        id = id,
                        title = request.title,
                        body = request.body,
                        done = request.done,
                        updatedAt = request.timestamp?.toString() ?: getCurrentTimestamp()
                    )
                    tasks[id] = updatedTask
                    
                    // Sync to Android database
                    try {
                        DataSyncManager.syncTaskToDatabase(updatedTask)
                    } catch (e: Exception) {
                        Log.w("KtorServer", "Failed to sync task to database", e)
                    }
                    
                    // Broadcast to WebSocket clients
                    broadcastSync("task_updated", Json.encodeToString(ServerTask.serializer(), updatedTask))
                    
                    call.respond(updatedTask)
                }
                
                delete("/tasks/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respondText("Missing task ID", status = HttpStatusCode.BadRequest)
                    val deletedTask = tasks.remove(id)
                    
                    if (deletedTask != null) {
                        // Sync deletion to Android database using task title
                        try {
                            DataSyncManager.handleTaskDeletion(deletedTask.title)
                        } catch (e: Exception) {
                            Log.w("KtorServer", "Failed to sync task deletion to database", e)
                        }
                        
                        // Broadcast to WebSocket clients with task title for matching
                        broadcastSync("task_deleted", deletedTask.title)
                        call.respond(mapOf("message" to "Task deleted successfully"))
                    } else {
                        call.respondText("Task not found", status = HttpStatusCode.NotFound)
                    }
                }
                
                get("/notes") {
                    call.respond(notes.values.toList())
                }
                
                post("/notes") {
                    val request = call.receive<NoteRequest>()
                    val note = ServerNote(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = request.body,
                        updatedAt = request.timestamp?.toString() ?: getCurrentTimestamp()
                    )
                    notes[note.id] = note
                    
                    // Sync to Android database
                    try {
                        DataSyncManager.syncNoteToDatabase(note)
                    } catch (e: Exception) {
                        Log.w("KtorServer", "Failed to sync note to database", e)
                    }
                    
                    // Broadcast to WebSocket clients
                    broadcastSync("note_added", Json.encodeToString(ServerNote.serializer(), note))
                    
                    call.respond(note)
                }
                
                put("/notes/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respondText("Missing note ID", status = HttpStatusCode.BadRequest)
                    val request = call.receive<NoteRequest>()
                    val updatedNote = ServerNote(
                        id = id,
                        title = request.title,
                        body = request.body,
                        updatedAt = getCurrentTimestamp()
                    )
                    notes[id] = updatedNote
                    
                    // Sync to Android database
                    try {
                        DataSyncManager.syncNoteToDatabase(updatedNote)
                    } catch (e: Exception) {
                        Log.w("KtorServer", "Failed to sync note to database", e)
                    }
                    
                    // Broadcast to WebSocket clients
                    broadcastSync("note_updated", Json.encodeToString(ServerNote.serializer(), updatedNote))
                    
                    call.respond(updatedNote)
                }
                
                delete("/notes/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respondText("Missing note ID", status = HttpStatusCode.BadRequest)
                    val deletedNote = notes.remove(id)
                    
                    if (deletedNote != null) {
                        // Sync deletion to Android database using note title
                        try {
                            DataSyncManager.handleNoteDeletion(deletedNote.title)
                        } catch (e: Exception) {
                            Log.w("KtorServer", "Failed to sync note deletion to database", e)
                        }
                        
                        // Broadcast to WebSocket clients with note title for matching
                        broadcastSync("note_deleted", deletedNote.title)
                        call.respond(mapOf("message" to "Note deleted successfully"))
                    } else {
                        call.respondText("Note not found", status = HttpStatusCode.NotFound)
                    }
                }
                
                // WebSocket endpoint
                webSocket("/sync") {
                    connections.add(this)
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                try {
                                    val message = Json.decodeFromString<SyncData>(frame.readText())
                                    
                                    // Handle incoming sync messages from clients
                                    when (message.type) {
                                        "task_updated" -> {
                                            // When web UI updates a task, sync it to mobile app database
                                            try {
                                                val task = Json.decodeFromString<ServerTask>(message.data)
                                                DataSyncManager.syncTaskToDatabase(task)
                                                Log.d("KtorServer", "Processed task update from client: ${task.title}")
                                            } catch (e: Exception) {
                                                Log.e("KtorServer", "Failed to process task update", e)
                                            }
                                        }
                                        "note_updated" -> {
                                            // When web UI updates a note, sync it to mobile app database
                                            try {
                                                val note = Json.decodeFromString<ServerNote>(message.data)
                                                DataSyncManager.syncNoteToDatabase(note)
                                                Log.d("KtorServer", "Processed note update from client: ${note.title}")
                                            } catch (e: Exception) {
                                                Log.e("KtorServer", "Failed to process note update", e)
                                            }
                                        }
                                        "task_deleted" -> {
                                            // When web UI deletes a task, remove it from mobile app too
                                            val taskTitle = message.data
                                            DataSyncManager.handleTaskDeletion(taskTitle)
                                            Log.d("KtorServer", "Processed task deletion from client: $taskTitle")
                                        }
                                        "note_deleted" -> {
                                            // When web UI deletes a note, remove it from mobile app too
                                            val noteTitle = message.data
                                            DataSyncManager.handleNoteDeletion(noteTitle)
                                            Log.d("KtorServer", "Processed note deletion from client: $noteTitle")
                                        }
                                        else -> {
                                            Log.d("KtorServer", "Unknown sync message type: ${message.type}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("KtorServer", "Failed to process sync message", e)
                                }
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // Connection closed
                    } finally {
                        connections.remove(this)
                    }
                }
            }
        }.start(wait = true)
    }
    
    private suspend fun broadcastSync(type: String, data: String) {
        val syncMessage = SyncData(type = type, data = data)
        val message = Json.encodeToString(SyncData.serializer(), syncMessage)
        
        connections.forEach { connection ->
            try {
                connection.send(Frame.Text(message))
            } catch (e: Exception) {
                connections.remove(connection)
            }
        }
    }
}