


package com.example.app.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    internal val noteDao: NoteDao,
    internal val taskDao: TaskDao,
    internal val chatMessageDao: ChatMessageDao
) {
    // Note operations
    suspend fun insertNote(note: Note) = noteDao.insert(note)
    suspend fun updateTranscript(id: Long, transcript: String) = noteDao.updateTranscript(id, transcript)
    suspend fun updateNoteSnippet(id: Long, snippet: String) = noteDao.updateSnippet(id, snippet)
    suspend fun updateChecklistState(id: Long, checklistState: String) = noteDao.updateChecklistState(id, checklistState)
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes("%$query%")
    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)
    suspend fun toggleFavorite(note: Note) = noteDao.update(note.copy(isFavorite = !note.isFavorite))
    suspend fun updateNoteTitle(id: Long, title: String) = noteDao.updateTitle(id, title)
    suspend fun archiveNote(id: Long) = noteDao.archiveNote(id)
    
    // Task operations
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun getAllTasksOnce(): List<Task> = taskDao.getAllTasksOnce()
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(id: Long) = taskDao.deleteById(id)
    suspend fun toggleTaskComplete(id: Long, isCompleted: Boolean) = taskDao.updateCompleted(id, isCompleted)
    
    // Chat message operations
    fun getAllChatMessages(): Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    fun getChatMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = chatMessageDao.getMessagesBySession(sessionId)
    fun getCurrentConversation(): Flow<List<ChatMessage>> = chatMessageDao.getCurrentConversation()
    suspend fun insertChatMessage(message: ChatMessage) = chatMessageDao.insert(message)
    suspend fun clearChatHistory() = chatMessageDao.deleteAll()
    suspend fun deleteChatSession(sessionId: String) = chatMessageDao.deleteSession(sessionId)
    
    // Export/Import operations
    suspend fun exportAllData(): ExportData {
        val notes = noteDao.getAllNotesOnce()
        val tasks = taskDao.getAllTasksOnce()
        val chatMessages = chatMessageDao.getAllMessagesOnce()
        
        return ExportData(
            notes = notes,
            tasks = tasks,
            chatMessages = chatMessages
        )
    }
    
    suspend fun importData(exportData: ExportData, replaceExisting: Boolean = false) {
        if (replaceExisting) {
            // Clear existing data
            chatMessageDao.deleteAll()
            taskDao.deleteAll()
            noteDao.deleteAll()
        }
        
        // Import notes (create new IDs to avoid conflicts)
        exportData.notes.forEach { note ->
            noteDao.insert(note.copy(id = 0)) // Let Room auto-generate new ID
        }
        
        // Import tasks (create new IDs to avoid conflicts)
        exportData.tasks.forEach { task ->
            taskDao.insert(task.copy(id = 0)) // Let Room auto-generate new ID
        }
        
        // Import chat messages (create new IDs to avoid conflicts)
        exportData.chatMessages.forEach { message ->
            chatMessageDao.insert(message.copy(id = 0)) // Let Room auto-generate new ID
        }
    }
}
