package com.example.app.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(internal val noteDao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes("%$query%")
    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)
    suspend fun toggleFavorite(note: Note) = noteDao.update(note.copy(isFavorite = !note.isFavorite))
    suspend fun updateNoteTitle(id: Long, title: String) = noteDao.updateTitle(id, title)
    suspend fun archiveNote(id: Long) = noteDao.archiveNote(id)
}
