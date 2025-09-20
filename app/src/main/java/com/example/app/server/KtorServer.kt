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
import io.ktor.http.content.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.Serializable
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import android.util.Log
import android.util.Base64
import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

@Serializable
data class ServerTask(
    val id: String,
    val title: String,
    val body: String = "",
    val priority: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val done: Boolean = false,
    val updatedAt: String
)

@Serializable
data class ServerNote(
    val id: String,
    val title: String,
    val body: String,
    val imagePath: String? = null,
    val updatedAt: String
)

@Serializable
data class DeletedNote(
    val id: String,
    val title: String? = null
)

@Serializable
data class TaskRequest(
    val title: String,
    val body: String = "",
    val priority: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val done: Boolean = false,
    val timestamp: Long? = null
)

@Serializable
data class NoteRequest(
    val title: String,
    val body: String,
    val imagePath: String? = null,
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
    private var appContext: android.content.Context? = null
    private var server: io.ktor.server.engine.ApplicationEngine? = null
    
    fun setContext(context: android.content.Context) {
        appContext = context
    }
    
    private fun getCurrentTimestamp(): String = Instant.now().toString()
    
    private fun getWebUIHTML(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Logion - Web Interface</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #1a1a1a;
            color: #ffffff;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 40px;
            padding: 20px 0;
            border-bottom: 2px solid #333;
        }
        
        .header h1 {
            color: #ff8c00;
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .status {
            background: #2a2a2a;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
            border-left: 4px solid #4caf50;
        }
        
        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-top: 30px;
        }
        
        .section {
            background: #2a2a2a;
            border-radius: 12px;
            padding: 20px;
            border: 1px solid #444;
        }
        
        .section h2 {
            color: #ff8c00;
            margin-bottom: 20px;
            font-size: 1.5em;
        }
        
        .item {
            background: #333;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 10px;
            border-left: 4px solid #ff8c00;
        }
        
        .task-completed {
            opacity: 0.6;
            border-left-color: #4caf50;
        }
        
        .task-completed h3 {
            text-decoration: line-through;
        }
        
        .task-pending {
            border-left-color: #ff8c00;
        }
        
        .item h3 {
            color: #fff;
            margin-bottom: 5px;
        }
        
        .item p {
            color: #ccc;
            font-size: 0.9em;
        }
        
        .empty {
            text-align: center;
            color: #888;
            padding: 40px;
            font-style: italic;
        }
        
        .form {
            background: #2a2a2a;
            border-radius: 12px;
            padding: 20px;
            margin-top: 20px;
            border: 1px solid #444;
        }
        
        .form input, .form textarea {
            width: 100%;
            background: #333;
            border: 1px solid #555;
            border-radius: 6px;
            padding: 12px;
            color: #fff;
            margin-bottom: 15px;
            font-size: 14px;
        }
        
        .form input:focus, .form textarea:focus {
            outline: none;
            border-color: #ff8c00;
        }
        
        .btn {
            background: #ff8c00;
            color: #000;
            border: none;
            border-radius: 6px;
            padding: 12px 24px;
            cursor: pointer;
            font-weight: bold;
            font-size: 14px;
            transition: background 0.3s;
        }
        
        .btn:hover {
            background: #ff9d1a;
        }
        
        .btn-danger {
            background: #dc3545;
            color: #fff;
            margin-left: 10px;
        }
        
        .btn-danger:hover {
            background: #c82333;
        }
        
        .checkbox-item {
            display: flex;
            align-items: center;
            margin: 8px 0;
            padding: 8px;
            background: #2a2a2a;
            border-radius: 6px;
        }
        
        .checkbox-item input[type="checkbox"] {
            margin-right: 10px;
            transform: scale(1.2);
            accent-color: #ff8c00;
        }
        
        .checkbox-item label {
            color: #fff;
            cursor: pointer;
            flex: 1;
        }
        
        .note-content {
            margin: 10px 0;
            color: #ccc;
            font-size: 0.9em;
        }
        
        .note-actions {
            margin-top: 10px;
            display: flex;
            gap: 10px;
        }
        
        .note-actions .btn {
            font-size: 12px;
            padding: 6px 12px;
        }
        
        .connection-status {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 10px 15px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }
        
        .connected {
            background: #4caf50;
            color: white;
        }
        
        .disconnected {
            background: #dc3545;
            color: white;
        }
        
        @media (max-width: 768px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
        
        /* Modal Styles */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.8);
        }
        
        .modal-content {
            background-color: #2a2a2a;
            margin: 5% auto;
            border-radius: 12px;
            width: 90%;
            max-width: 600px;
            border: 1px solid #444;
        }
        
        .modal-header {
            padding: 20px;
            border-bottom: 1px solid #444;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header h2 {
            color: #ff8c00;
            margin: 0;
        }
        
        .close {
            color: #aaa;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }
        
        .close:hover {
            color: #fff;
        }
        
        .modal-body {
            padding: 20px;
        }
        
        .modal-input, .modal-textarea {
            width: 100%;
            background: #333;
            border: 1px solid #555;
            border-radius: 6px;
            padding: 12px;
            color: #fff;
            margin-bottom: 15px;
            font-size: 14px;
            font-family: inherit;
        }
        
        .modal-textarea {
            resize: vertical;
            min-height: 200px;
        }
        
        .modal-input:focus, .modal-textarea:focus {
            outline: none;
            border-color: #ff8c00;
        }
        
        .modal-footer {
            padding: 20px;
            border-top: 1px solid #444;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
    </style>
</head>
<body>
    <div class="connection-status" id="connectionStatus">Connecting...</div>
    
    <div class="container">
        <div class="header">
            <h1>📱 Logion</h1>
            <p>Connected to your Android device</p>
        </div>
        
        <div class="status">
            <strong>🟢 Server Connected</strong> - Real-time sync with your mobile app
        </div>
        
        <div class="grid">
            <div class="section">
                <h2>📋 Tasks</h2>
                <div id="tasksList">
                    <div class="empty">Loading tasks...</div>
                </div>
                
                <div class="form">
                    <h3>Add New Task</h3>
                    <input type="text" id="taskTitle" placeholder="Task title">
                    <textarea id="taskBody" placeholder="Task description" rows="3"></textarea>
                    <button class="btn" onclick="addTask()">Add Task</button>
                </div>
            </div>
            
            <div class="section">
                <h2>📝 Notes</h2>
                <div id="notesList">
                    <div class="empty">Loading notes...</div>
                </div>
                
                <div class="form">
                    <h3>Add New Note</h3>
                    <input type="text" id="noteTitle" placeholder="Note title">
                    <textarea id="noteBody" placeholder="Note content" rows="5"></textarea>
                    <button class="btn" onclick="addNote()">Add Note</button>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Note Edit Modal -->
    <div id="noteEditModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Edit Note</h2>
                <span class="close" onclick="closeNoteEditModal()">&times;</span>
            </div>
            <div class="modal-body">
                <input type="text" id="editNoteTitle" placeholder="Note title" class="modal-input">
                <textarea id="editNoteBody" placeholder="Note content" rows="10" class="modal-textarea"></textarea>
            </div>
            <div class="modal-footer">
                <button class="btn btn-danger" onclick="closeNoteEditModal()">Cancel</button>
                <button class="btn" onclick="saveNoteEdit()">Save</button>
            </div>
        </div>
    </div>
    
    <script>
        let socket = null;
        const serverUrl = window.location.origin;
        
        // Connect to WebSocket
        function connectWebSocket() {
            const wsUrl = serverUrl.replace('http:', 'ws:') + '/sync';
            socket = new WebSocket(wsUrl);
            
            socket.onopen = function() {
                console.log('WebSocket connected');
                updateConnectionStatus(true);
            };
            
            socket.onclose = function() {
                console.log('WebSocket disconnected');
                updateConnectionStatus(false);
                // Reconnect after 3 seconds
                setTimeout(connectWebSocket, 3000);
            };
            
            socket.onmessage = function(event) {
                const data = JSON.parse(event.data);
                handleSyncMessage(data);
            };
            
            socket.onerror = function(error) {
                console.error('WebSocket error:', error);
                updateConnectionStatus(false);
            };
        }
        
        function updateConnectionStatus(connected) {
            const status = document.getElementById('connectionStatus');
            if (connected) {
                status.textContent = '🟢 Connected';
                status.className = 'connection-status connected';
            } else {
                status.textContent = '🔴 Disconnected';
                status.className = 'connection-status disconnected';
            }
        }
        
        function handleSyncMessage(data) {
            console.log('Sync message:', data);
            switch(data.type) {
                case 'task_added':
                case 'task_updated':
                    loadTasks();
                    break;
                case 'note_added':
                case 'note_updated':
                    loadNotes();
                    break;
                case 'task_deleted':
                case 'note_deleted':
                    loadTasks();
                    loadNotes();
                    break;
            }
        }
        
        // Load tasks from server
        async function loadTasks() {
            try {
                const response = await fetch(serverUrl + '/tasks');
                const tasks = await response.json();
                displayTasks(tasks);
            } catch (error) {
                console.error('Error loading tasks:', error);
            }
        }
        
        // Load notes from server
        async function loadNotes() {
            try {
                const response = await fetch(serverUrl + '/notes');
                const notes = await response.json();
                displayNotes(notes);
            } catch (error) {
                console.error('Error loading notes:', error);
            }
        }
        
        function displayTasks(tasks) {
            const container = document.getElementById('tasksList');
            if (tasks.length === 0) {
                container.innerHTML = '<div class="empty">No tasks yet</div>';
                return;
            }
            
            // Sort tasks: uncompleted first, then completed
            const sortedTasks = tasks.sort((a, b) => {
                if (a.done === b.done) return 0;
                return a.done ? 1 : -1; // false (uncompleted) comes first
            });
            
            let html = '';
            for (let i = 0; i < sortedTasks.length; i++) {
                const task = sortedTasks[i];
                const statusIcon = task.done ? '✅' : '⭕';
                const statusClass = task.done ? 'task-completed' : 'task-pending';
                html += '<div class="item ' + statusClass + '">';
                html += '<h3>' + task.title + ' ' + statusIcon + '</h3>';
                html += '<p>' + (task.body || 'No description') + '</p>';
                html += '<p><small>Due: ' + (task.dueDate || 'No due date') + '</small></p>';
                html += '<button class="btn btn-danger" onclick="deleteTask(\'' + task.id + '\')">Delete</button>';
                html += '</div>';
            }
            container.innerHTML = html;
        }
        
        function displayNotes(notes) {
            const container = document.getElementById('notesList');
            if (notes.length === 0) {
                container.innerHTML = '<div class="empty">No notes yet</div>';
                return;
            }
            
            let html = '';
            for (let i = 0; i < notes.length; i++) {
                const note = notes[i];
                html += '<div class="item">';
                html += '<h3>' + note.title + '</h3>';
                
                // Process note body for checkboxes
                const processedBody = processNoteContent(note.body, note.id);
                html += '<div class="note-content">' + processedBody + '</div>';
                
                html += '<div class="note-actions">';
                html += '<button class="btn" onclick="editNote(\'' + note.id + '\')">Edit</button>';
                html += '<button class="btn btn-danger" onclick="deleteNote(\'' + note.id + '\')">Delete</button>';
                html += '</div>';
                html += '</div>';
            }
            container.innerHTML = html;
        }
        
        function processNoteContent(content, noteId) {
            if (!content) return 'No content';
            
            // First, try to parse as JSON (structured format from Android app)
            try {
                const jsonData = JSON.parse(content);
                if (jsonData.text !== undefined && jsonData.checkboxes !== undefined) {
                    // This is structured JSON format
                    let result = '';
                    
                    // Add the main text content
                    if (jsonData.text && jsonData.text.trim()) {
                        result += jsonData.text.replace(/\n/g, '<br>') + '<br><br>';
                    }
                    
                    // Add checkboxes as interactive HTML
                    if (jsonData.checkboxes && jsonData.checkboxes.length > 0) {
                        jsonData.checkboxes.forEach((checkbox, index) => {
                            const checkboxId = 'checkbox_' + noteId + '_' + index;
                            const isChecked = checkbox.checked || false;
                            const text = checkbox.text || '';
                            
                            result += '<div class="checkbox-item">' +
                                     '<input type="checkbox" id="' + checkboxId + '" ' + (isChecked ? 'checked' : '') + 
                                     ' onchange="updateNoteCheckbox(\'' + noteId + '\', this)">' +
                                     '<label for="' + checkboxId + '">' + text + '</label>' +
                                     '</div>';
                        });
                    }
                    
                    return result || 'No content';
                }
            } catch (e) {
                // Not JSON, continue with markdown processing
            }
            
            // Fallback: Process as markdown format - convert - [ ] and - [x] to interactive checkboxes
            let processed = content.replace(/- \[([ x])\] (.+?)(?=\n|$)/g, function(match, checked, text) {
                const isChecked = checked === 'x';
                const checkboxId = 'checkbox_' + noteId + '_' + Math.random().toString(36).substr(2, 9);
                return '<div class="checkbox-item">' +
                       '<input type="checkbox" id="' + checkboxId + '" ' + (isChecked ? 'checked' : '') + 
                       ' onchange="updateNoteCheckbox(\'' + noteId + '\', this)">' +
                       '<label for="' + checkboxId + '">' + text + '</label>' +
                       '</div>';
            });
            
            // Convert newlines to <br> for remaining text
            processed = processed.replace(/\n/g, '<br>');
            
            // Limit length for preview
            if (processed.length > 300) {
                processed = processed.substring(0, 300) + '...';
            }
            
            return processed;
        }
        
        // Add new task
        async function addTask() {
            const title = document.getElementById('taskTitle').value;
            const body = document.getElementById('taskBody').value;
            
            if (!title.trim()) {
                alert('Please enter a task title');
                return;
            }
            
            try {
                await fetch(serverUrl + '/tasks', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        title: title,
                        body: body,
                        done: false,
                        priority: 'Medium'
                    })
                });
                
                document.getElementById('taskTitle').value = '';
                document.getElementById('taskBody').value = '';
                loadTasks();
            } catch (error) {
                console.error('Error adding task:', error);
                alert('Failed to add task');
            }
        }
        
        // Add new note
        async function addNote() {
            const title = document.getElementById('noteTitle').value;
            const body = document.getElementById('noteBody').value;
            
            if (!title.trim()) {
                alert('Please enter a note title');
                return;
            }
            
            try {
                await fetch(serverUrl + '/notes', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        title: title,
                        body: body
                    })
                });
                
                document.getElementById('noteTitle').value = '';
                document.getElementById('noteBody').value = '';
                loadNotes();
            } catch (error) {
                console.error('Error adding note:', error);
                alert('Failed to add note');
            }
        }
        
        // Delete task
        async function deleteTask(taskId) {
            if (!confirm('Are you sure you want to delete this task?')) return;
            
            try {
                await fetch(serverUrl + '/tasks/' + taskId, {
                    method: 'DELETE'
                });
                loadTasks();
            } catch (error) {
                console.error('Error deleting task:', error);
                alert('Failed to delete task');
            }
        }
        
        // Delete note
        async function deleteNote(noteId) {
            if (!confirm('Are you sure you want to delete this note?')) return;
            
            try {
                await fetch(serverUrl + '/notes/' + noteId, {
                    method: 'DELETE'
                });
                loadNotes();
            } catch (error) {
                console.error('Error deleting note:', error);
                alert('Failed to delete note');
            }
        }
        
        // Update note checkbox
        async function updateNoteCheckbox(noteId, checkboxElement) {
            try {
                // Find the note
                const response = await fetch(serverUrl + '/notes');
                const notes = await response.json();
                const note = notes.find(n => n.id == noteId);
                
                if (!note) return;
                
                // Update the checkbox state in the note content
                let updatedContent = note.body;
                const label = checkboxElement.nextElementSibling.textContent;
                const isChecked = checkboxElement.checked;
                
                // Find and update the specific checkbox line
                // Simple replace approach to avoid regex escaping issues
                const oldCheckedPattern = '- [x] ' + label;
                const oldUncheckedPattern = '- [ ] ' + label;
                const newPattern = '- [' + (isChecked ? 'x' : ' ') + '] ' + label;
                
                if (updatedContent.includes(oldCheckedPattern)) {
                    updatedContent = updatedContent.replace(oldCheckedPattern, newPattern);
                } else if (updatedContent.includes(oldUncheckedPattern)) {
                    updatedContent = updatedContent.replace(oldUncheckedPattern, newPattern);
                }
                
                // Update the note
                await fetch(serverUrl + '/notes/' + noteId, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        title: note.title,
                        body: updatedContent
                    })
                });
                
            } catch (error) {
                console.error('Error updating checkbox:', error);
                // Revert checkbox state on error
                checkboxElement.checked = !checkboxElement.checked;
            }
        }
        
        // Edit note
        let currentEditingNoteId = null;
        
        function editNote(noteId) {
            console.log('Editing note:', noteId);
            fetch(serverUrl + '/notes')
                .then(response => response.json())
                .then(notes => {
                    console.log('All notes:', notes);
                    const note = notes.find(n => n.id == noteId);
                    console.log('Found note:', note);
                    if (note) {
                        currentEditingNoteId = noteId;
                        document.getElementById('editNoteTitle').value = note.title || '';
                        document.getElementById('editNoteBody').value = note.body || '';
                        console.log('Set title:', note.title, 'Set body:', note.body);
                        document.getElementById('noteEditModal').style.display = 'block';
                    } else {
                        console.error('Note not found with id:', noteId);
                    }
                })
                .catch(error => {
                    console.error('Error loading note for editing:', error);
                });
        }
        
        function closeNoteEditModal() {
            document.getElementById('noteEditModal').style.display = 'none';
            currentEditingNoteId = null;
        }
        
        function saveNoteEdit() {
            if (!currentEditingNoteId) return;
            
            const title = document.getElementById('editNoteTitle').value;
            const body = document.getElementById('editNoteBody').value;
            
            if (!title.trim()) {
                alert('Please enter a note title');
                return;
            }
            
            updateNote(currentEditingNoteId, title, body);
            closeNoteEditModal();
        }
        
        // Update note
        async function updateNote(noteId, title, body) {
            try {
                await fetch(serverUrl + '/notes/' + noteId, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        title: title,
                        body: body
                    })
                });
                loadNotes();
            } catch (error) {
                console.error('Error updating note:', error);
                alert('Failed to update note');
            }
        }
        
        // Initialize
        document.addEventListener('DOMContentLoaded', function() {
            loadTasks();
            loadNotes();
            connectWebSocket();
            
            // Close modal when clicking outside
            window.onclick = function(event) {
                const modal = document.getElementById('noteEditModal');
                if (event.target === modal) {
                    closeNoteEditModal();
                }
            };
        });
    </script>
</body>
</html>
        """
    }
    
    private fun saveBase64Image(base64Data: String, call: ApplicationCall): String? {
        try {
            // Extract MIME type and base64 data
            val dataStartIndex = base64Data.indexOf(",")
            if (dataStartIndex == -1) return null
            
            val base64ImageData = base64Data.substring(dataStartIndex + 1)
            val mimeTypePart = base64Data.substring(0, dataStartIndex)
            
            // Determine file extension
            val extension = when {
                mimeTypePart.contains("jpeg") || mimeTypePart.contains("jpg") -> "jpg"
                mimeTypePart.contains("png") -> "png"
                mimeTypePart.contains("gif") -> "gif"
                mimeTypePart.contains("webp") -> "webp"
                else -> "jpg" // default
            }
            
            // Create unique filename
            val fileName = "web_image_${UUID.randomUUID()}.$extension"
            
            // Use app-specific directory if context is available, fallback to Pictures/Logion
            val imageFile = if (appContext != null) {
                val imagesDir = File(appContext!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Logion")
                imagesDir.mkdirs()
                File(imagesDir, fileName)
            } else {
                val fallbackDir = File("/storage/emulated/0/Pictures/Logion")
                fallbackDir.mkdirs()
                File(fallbackDir, fileName)
            }
            
            // Decode and save
            val imageBytes = Base64.decode(base64ImageData, Base64.DEFAULT)
            FileOutputStream(imageFile).use { fos ->
                fos.write(imageBytes)
            }
            
            Log.d("KtorServer", "Saved base64 image to: ${imageFile.absolutePath}")
            
            // Get the host and port from the request
            val host = call.request.local.serverHost
            val port = call.request.local.serverPort
            // Return server URL format for web-uploaded images so Android can access via HTTP
            return "http://$host:$port/images/$fileName"
        } catch (e: Exception) {
            Log.e("KtorServer", "Error saving base64 image", e)
            return null
        }
    }
    
    private fun convertImagePathForWeb(originalPath: String?): String? {
        if (originalPath == null) return null
        
        // If it's already a web-accessible path, return as-is
        if (originalPath.startsWith("http") || originalPath.startsWith("data:")) {
            return originalPath
        }
        
        // For content URIs and other Android paths, extract filename and make web-accessible
        val fileName = originalPath.substringAfterLast("/")
        return if (fileName.isNotEmpty() && fileName.contains(".")) {
            // Return just the filename - the web client will construct the full HTTP URL
            fileName
        } else {
            // If we can't extract a filename, return null
            null
        }
    }
    
    // Methods for external access and sync
    fun addTask(task: ServerTask) {
        Log.d("KtorServer", "Adding task to server: ${task.title} (${task.id})")
        tasks[task.id] = task
    }
    
    fun addNote(note: ServerNote) {
        Log.d("KtorServer", "Adding note to server: ${note.title} (${note.id})")
        Log.d("KtorServer", "Note body length: ${note.body.length}")
        Log.d("KtorServer", "Note body preview: ${note.body.take(100)}")
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
    
    private fun convertHtmlToMarkdown(content: String): String {
        Log.d("KtorServer", "Converting HTML to markdown: $content")
        
        // If content doesn't contain HTML tags, return as-is
        if (!content.contains("<")) {
            return content
        }
        
        var result = content
        
        // First, let's handle the complex HTML structure from the web UI logs
        // Pattern: <div class="checkbox-item checkbox-checked completed" style="...">...<span>text</span>...</div>
        
        // Extract all checkbox items regardless of their complex structure
        val checkboxPattern = Regex(
            """<div[^>]*class="[^"]*checkbox-item[^"]*"[^>]*>.*?<span[^>]*>(.*?)</span>.*?</div>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        val checkboxMatches = checkboxPattern.findAll(result)
        val replacements = mutableListOf<Pair<String, String>>()
        
        for (match in checkboxMatches) {
            val fullMatch = match.value
            val text = match.groupValues[1]
            
            // Check if this checkbox is checked by looking for various indicators
            val isChecked = fullMatch.contains("checkbox-checked") || 
                           fullMatch.contains("completed") ||
                           fullMatch.contains("checked") ||
                           fullMatch.contains("line-through")
            
            val markdownCheckbox = if (isChecked) "- [x] $text" else "- [ ] $text"
            replacements.add(fullMatch to markdownCheckbox)
        }
        
        // Apply all replacements
        for ((original, replacement) in replacements) {
            result = result.replace(original, replacement)
        }
        
        // Also handle simpler patterns for backward compatibility
        
        // Convert checked checkboxes with label: <div class="checkbox-item..."><input...checked...><label>text</label></div>
        result = result.replace(
            Regex("""<div[^>]*class="[^"]*checkbox-item[^"]*"[^>]*>.*?<input[^>]*checked[^>]*>.*?<label[^>]*>(.*?)</label>.*?</div>""", RegexOption.DOT_MATCHES_ALL),
            "- [x] $1"
        )
        
        // Convert unchecked checkboxes with label: <div class="checkbox-item..."><input...><label>text</label></div>
        result = result.replace(
            Regex("""<div[^>]*class="[^"]*checkbox-item[^"]*"[^>]*>.*?<input(?![^>]*checked)[^>]*>.*?<label[^>]*>(.*?)</label>.*?</div>""", RegexOption.DOT_MATCHES_ALL),
            "- [ ] $1"
        )
        
        // Remove any remaining HTML tags
        result = result.replace(Regex("""<[^>]+>"""), "")
        
        // Clean up whitespace and line breaks
        result = result.replace(Regex("""<br\s*/?>"""), "\n")  // Convert <br> to newlines first
        
        // IMPORTANT: Clean up checkbox symbols that may have leaked into text
        // Replace checkbox symbols with proper markdown
        result = result.replace("☐", "- [ ]")  // Unchecked checkbox symbol
        result = result.replace("☑", "- [x]")  // Checked checkbox symbol
        result = result.replace("✓", "- [x]")  // Checkmark symbol
        result = result.replace("✔", "- [x]")  // Another checkmark variant
        
        result = result.replace(Regex("""\s+"""), " ").trim()  // Clean up extra whitespace
        
        Log.d("KtorServer", "Converted result: $result")
        return result
    }
    
    private fun processContentToStructuredFormat(content: String): String {
        Log.d("KtorServer", "Processing content to structured format: $content")
        
        // If the content already looks like JSON with checkboxes, return as-is
        if (content.contains("\"checkboxes\"") && content.contains("\"text\"")) {
            return content
        }
        
        // Check if content contains checkbox symbols or markdown checkboxes
        val checkboxSymbols = listOf("☐", "☑", "✓", "✔")
        val hasCheckboxSymbols = checkboxSymbols.any { content.contains(it) }
        val hasMarkdownCheckboxes = content.contains(Regex("""- \[([ x])\]"""))
        
        if (!hasCheckboxSymbols && !hasMarkdownCheckboxes) {
            // No checkboxes, return content as plain text
            return content
        }
        
        // Parse content to extract checkboxes and remaining text
        val checkboxes = mutableListOf<Map<String, Any>>()
        var remainingText = content
        
        // Handle checkbox symbols first
        checkboxSymbols.forEach { symbol ->
            val isChecked = symbol in setOf("☑", "✓", "✔")
            val pattern = if (symbol == "☐") {
                Regex("""☐\s*([^\n☐☑✓✔]+)""")
            } else {
                Regex("""$symbol\s*([^\n☐☑✓✔]+)""")
            }
            
            val matches = pattern.findAll(remainingText).toList()
            matches.forEach { match ->
                val text = match.groupValues[1].trim()
                if (text.isNotEmpty()) {
                    checkboxes.add(mapOf("text" to text, "checked" to isChecked))
                }
                remainingText = remainingText.replace(match.value, "")
            }
        }
        
        // Handle markdown checkboxes
        val markdownPattern = Regex("""- \[([ x])\]\s*([^\n]+)""")
        val markdownMatches = markdownPattern.findAll(remainingText).toList()
        markdownMatches.forEach { match ->
            val isChecked = match.groupValues[1] == "x"
            val text = match.groupValues[2].trim()
            if (text.isNotEmpty()) {
                checkboxes.add(mapOf("text" to text, "checked" to isChecked))
            }
            remainingText = remainingText.replace(match.value, "")
        }
        
        // Clean up remaining text
        remainingText = remainingText
            .replace(Regex("""\n\s*\n"""), "\n\n") // Normalize multiple newlines
            .replace(Regex("""^\s+|\s+$"""), "") // Trim whitespace
        
        // Create structured JSON if we found checkboxes
        return if (checkboxes.isNotEmpty()) {
            val json = buildJsonObject {
                put("text", remainingText)
                putJsonArray("checkboxes") {
                    checkboxes.forEach { checkbox ->
                        addJsonObject {
                            put("text", checkbox["text"] as String)
                            put("checked", checkbox["checked"] as Boolean)
                        }
                    }
                }
            }
            json.toString()
        } else {
            content
        }
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
        broadcastSync("note_deleted", Json.encodeToString(DeletedNote.serializer(), DeletedNote(id = noteId)))
    }
    
    suspend fun deleteNoteWithBroadcastByTitle(noteTitle: String) {
        // Remove note by title from server memory
        val noteToRemove = notes.values.find { it.title == noteTitle }
        if (noteToRemove != null) {
            notes.remove(noteToRemove.id)
        }
        
        // Broadcast to WebSocket clients with note title
        broadcastSync("note_deleted", Json.encodeToString(DeletedNote.serializer(), DeletedNote(id = "", title = noteTitle)))
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
        Log.d("KtorServer", "Attempting to start server on 0.0.0.0:8080")
        try {
            // Stop existing server if running
            stop()
            
            // Load existing tasks and notes from database before starting server
            Log.d("KtorServer", "Loading existing data from database...")
            try {
                DataSyncManager.syncTasksFromDatabase()
                DataSyncManager.syncNotesFromDatabase()
                Log.d("KtorServer", "Loaded ${tasks.size} tasks and ${notes.size} notes from database")
            } catch (e: Exception) {
                Log.e("KtorServer", "Failed to load data from database", e)
            }

            server = embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
                Log.d("KtorServer", "Installing CORS plugin")
                install(CORS) {
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Delete)
                    allowMethod(HttpMethod.Patch)
                    allowHeader(HttpHeaders.Authorization)
                    allowHeader(HttpHeaders.ContentType)
                    anyHost() // Allow all hosts for development
                }
                
                Log.d("KtorServer", "Installing ContentNegotiation plugin")
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
                
                Log.d("KtorServer", "Installing WebSockets plugin")
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                    timeout = Duration.ofSeconds(15)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                
                Log.d("KtorServer", "Setting up routes")
                routing {
                
                get("/") {
                    call.respondText("Logion Server is running!")
                }
                
                get("/ui") {
                    call.respondText(getWebUIHTML(), ContentType.Text.Html)
                }
                
                get("/tasks") {
                    Log.d("KtorServer", "GET /tasks - returning ${tasks.size} tasks")
                    val tasksList = tasks.values.toList()
                    tasksList.forEach { task ->
                        Log.d("KtorServer", "Task: ${task.title} (${task.id}) - done: ${task.done}")
                    }
                    call.respond(tasksList)
                }
                
                post("/tasks") {
                    val request = call.receive<TaskRequest>()
                    val task = ServerTask(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = request.body,
                        priority = request.priority,
                        dueDate = request.dueDate,
                        dueTime = request.dueTime,
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
                        priority = request.priority,
                        dueDate = request.dueDate,
                        dueTime = request.dueTime,
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
                    Log.d("KtorServer", "GET /notes - returning ${notes.size} notes")
                    notes.values.forEach { note ->
                        Log.d("KtorServer", "Note: ${note.title} (${note.id}) - body length: ${note.body.length}")
                        Log.d("KtorServer", "Note body preview: ${note.body.take(100)}")
                    }
                    val webCompatibleNotes = notes.values.map { note ->
                        note.copy(imagePath = convertImagePathForWeb(note.imagePath))
                    }
                    call.respond(webCompatibleNotes)
                }
                
                get("/images/{path...}") {
                    val imagePath = call.parameters.getAll("path")?.joinToString("/") ?: return@get call.respond(HttpStatusCode.BadRequest)
                    
                    // Try multiple possible image locations, including app-specific directories
                    val possiblePaths = mutableListOf<String>()
                    
                    // Add app-specific directory if context is available
                    if (appContext != null) {
                        val appSpecificDir = File(appContext!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Logion")
                        possiblePaths.add("${appSpecificDir.absolutePath}/$imagePath")
                    }
                    
                    // Add fallback paths
                    possiblePaths.addAll(listOf(
                        "/storage/emulated/0/Pictures/Logion/$imagePath",
                        "/storage/emulated/0/Android/data/com.example.app/files/Pictures/Logion/$imagePath",
                        imagePath // In case it's already an absolute path
                    ))
                    
                    var imageFile: File? = null
                    for (path in possiblePaths) {
                        val file = File(path)
                        if (file.exists() && file.isFile) {
                            imageFile = file
                            Log.d("KtorServer", "Found image at: $path")
                            break
                        }
                    }
                    
                    if (imageFile != null) {
                        val contentType = when (imageFile.extension.lowercase()) {
                            "jpg", "jpeg" -> ContentType.Image.JPEG
                            "png" -> ContentType.Image.PNG
                            "gif" -> ContentType.Image.GIF
                            "webp" -> ContentType("image", "webp")
                            else -> ContentType.Application.OctetStream
                        }
                        call.response.header(HttpHeaders.ContentType, contentType.toString())
                        call.respondFile(imageFile)
                    } else {
                        Log.w("KtorServer", "Image not found: $imagePath, tried paths: $possiblePaths")
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
                
                post("/notes") {
                    val request = call.receive<NoteRequest>()
                    
                    // Process the body content to handle checkbox symbols and convert to structured format
                    val processedBody = processContentToStructuredFormat(request.body)
                    
                    // Handle image data if provided
                    var processedImagePath: String? = null
                    request.imagePath?.let { imageData ->
                        processedImagePath = if (imageData.startsWith("data:image/")) {
                            // Base64 image from web client - save it as a file
                            saveBase64Image(imageData, call)
                        } else {
                            // Direct path from Android app
                            imageData
                        }
                    }
                    
                    val note = ServerNote(
                        id = UUID.randomUUID().toString(),
                        title = request.title,
                        body = processedBody,
                        imagePath = processedImagePath,
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
                    
                    try {
                        // Try to receive as JSON first
                        val requestBody = call.receiveText()
                        Log.d("KtorServer", "Received PUT request body: $requestBody")
                        
                        // Parse JSON manually to handle potential issues
                        val request = try {
                            Json.decodeFromString<NoteRequest>(requestBody)
                        } catch (e: Exception) {
                            Log.e("KtorServer", "Failed to parse JSON: ${e.message}")
                            call.respondText("Invalid JSON format: ${e.message}", status = HttpStatusCode.BadRequest)
                            return@put
                        }
                        
                        // Handle image data if provided
                        var processedImagePath: String? = null
                        request.imagePath?.let { imageData ->
                            processedImagePath = if (imageData.startsWith("data:image/")) {
                                // Base64 image from web client - save it as a file
                                saveBase64Image(imageData, call)
                            } else {
                                // Direct path from Android app
                                imageData
                            }
                        }
                        
                        // Convert HTML content to markdown first, then process for structured format
                        val markdownBody = convertHtmlToMarkdown(request.body)
                        val processedBody = processContentToStructuredFormat(markdownBody)
                        
                        val updatedNote = ServerNote(
                            id = id,
                            title = request.title,
                            body = processedBody,
                            imagePath = processedImagePath,
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
                    } catch (e: Exception) {
                        Log.e("KtorServer", "Error updating note: ${e.message}", e)
                        call.respondText("Error updating note: ${e.message}", status = HttpStatusCode.BadRequest)
                    }
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
                        broadcastSync("note_deleted", Json.encodeToString(DeletedNote.serializer(), DeletedNote(id = deletedNote.id, title = deletedNote.title)))
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
            Log.d("KtorServer", "About to start server with wait=false")
        }
        server?.start(wait = false)
        Log.d("KtorServer", "Server started successfully in background")
    } catch (e: Exception) {
        Log.e("KtorServer", "Failed to start server", e)
        Log.e("KtorServer", "Exception: ${e.message}")
        throw e
    }
    }
    
    fun stop() {
        try {
            Log.d("KtorServer", "Stopping server...")
            server?.stop(1000, 2000) // 1 second grace period, 2 seconds timeout
            server = null
            Log.d("KtorServer", "Server stopped successfully")
        } catch (e: Exception) {
            Log.e("KtorServer", "Error stopping server", e)
        }
    }
    
    fun isRunning(): Boolean {
        return server != null
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