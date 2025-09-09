package com.example.app.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
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
    val done: Boolean = false
)

@Serializable
data class NoteRequest(
    val title: String,
    val body: String
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
    
    fun clearData() {
        tasks.clear()
        notes.clear()
    }
    
    suspend fun start() {
        embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
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
                // CORS headers
                options("{...}") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    call.response.header("Access-Control-Allow-Headers", "Content-Type")
                    call.respond(HttpStatusCode.OK)
                }
                
                // Tasks endpoints
                get("/tasks") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respond(tasks.values.toList())
                }
                
                post("/tasks") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    val request = call.receive<TaskRequest>()
                    val task = ServerTask(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = request.body,
                        done = request.done,
                        updatedAt = getCurrentTimestamp()
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
                    
                    call.respond(HttpStatusCode.Created, task)
                }
                
                put("/tasks/{id}") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                    val request = call.receive<TaskRequest>()
                    
                    val existingTask = tasks[id] ?: return@put call.respond(HttpStatusCode.NotFound)
                    val updatedTask = existingTask.copy(
                        title = request.title,
                        body = request.body,
                        done = request.done,
                        updatedAt = getCurrentTimestamp()
                    )
                    tasks[id] = updatedTask
                    
                    // Sync to Android database
                    try {
                        DataSyncManager.syncTaskToDatabase(updatedTask)
                    } catch (e: Exception) {
                        Log.w("KtorServer", "Failed to sync updated task to database", e)
                    }
                    
                    // Broadcast to WebSocket clients
                    broadcastSync("task_updated", Json.encodeToString(ServerTask.serializer(), updatedTask))
                    
                    call.respond(updatedTask)
                }
                
                // Notes endpoints
                get("/notes") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respond(notes.values.toList())
                }
                
                post("/notes") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    val request = call.receive<NoteRequest>()
                    val note = ServerNote(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = request.body,
                        updatedAt = getCurrentTimestamp()
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
                    
                    call.respond(HttpStatusCode.Created, note)
                }
                
                put("/notes/{id}") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                    val request = call.receive<NoteRequest>()
                    
                    val existingNote = notes[id] ?: return@put call.respond(HttpStatusCode.NotFound)
                    val updatedNote = existingNote.copy(
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
                
                // WebSocket endpoint
                webSocket("/sync") {
                    connections.add(this)
                    try {
                        for (frame in incoming) {
                            // Handle incoming messages if needed
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
