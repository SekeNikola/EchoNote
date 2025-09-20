


package com.example.app.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    internal val noteDao: NoteDao,
    internal val taskDao: TaskDao,
    internal val chatMessageDao: ChatMessageDao,
    internal val reminderDao: ReminderDao
) {
    // Note operations
    suspend fun insertNote(note: Note) {
        noteDao.insert(note)
        refreshWidget()
    }
    
    suspend fun updateTranscript(id: Long, transcript: String) {
        noteDao.updateTranscript(id, transcript)
        refreshWidget()
    }
    
    suspend fun updateNoteSnippet(id: Long, snippet: String) {
        noteDao.updateSnippet(id, snippet)
        refreshWidget()
    }
    
    suspend fun updateChecklistState(id: Long, checklistState: String) {
        noteDao.updateChecklistState(id, checklistState)
        refreshWidget()
    }
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun getAllNotesOnce(): List<Note> = noteDao.getAllNotesOnce()
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes("%$query%")
    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)
    suspend fun toggleFavorite(note: Note) {
        noteDao.update(note.copy(isFavorite = !note.isFavorite))
        refreshWidget()
    }
    
    suspend fun updateNoteTitle(id: Long, title: String) {
        noteDao.updateTitle(id, title)
        refreshWidget()
    }
    
    suspend fun archiveNote(id: Long) {
        noteDao.archiveNote(id)
        refreshWidget()
    }
    
    suspend fun deleteNote(id: Long) {
        noteDao.deleteById(id)
        refreshWidget()
    }
    
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
    
    // Reminder operations
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    fun getActiveReminders(): Flow<List<Reminder>> = reminderDao.getActiveReminders()
    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteById(id)
    suspend fun markReminderCompleted(id: Long) = reminderDao.markCompleted(id)
    
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
    
    private fun refreshWidget() {
        try {
            val widgetProviderClass = Class.forName("com.example.app.widget.SimpleWidgetProvider")
            val updateMethod = widgetProviderClass.getDeclaredMethod("updateAllWidgets")
            updateMethod.invoke(null)
        } catch (e: Exception) {
            // Widget provider not available, ignore
        }
    }
}
