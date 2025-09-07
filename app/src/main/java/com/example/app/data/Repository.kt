


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
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(id: Long) = taskDao.deleteById(id)
    suspend fun toggleTaskComplete(id: Long, isCompleted: Boolean) = taskDao.updateCompleted(id, isCompleted)
    
    // Chat message operations
    fun getAllChatMessages(): Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    fun getChatMessagesBySession(sessionId: String): Flow<List<ChatMessage>> = chatMessageDao.getMessagesBySession(sessionId)
    suspend fun insertChatMessage(message: ChatMessage) = chatMessageDao.insert(message)
    suspend fun clearChatHistory() = chatMessageDao.deleteAll()
    suspend fun deleteChatSession(sessionId: String) = chatMessageDao.deleteSession(sessionId)
}
