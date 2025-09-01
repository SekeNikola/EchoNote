
















package com.example.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("UPDATE notes SET snippet = :snippet WHERE id = :id")
    suspend fun updateSnippet(id: Long, snippet: String)
    @Query("UPDATE notes SET checklistState = :checklistState WHERE id = :id")
    suspend fun updateChecklistState(id: Long, checklistState: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE (title LIKE :query OR transcript LIKE :query) AND isArchived = 0 ORDER BY createdAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note?>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteWithRelations(id: Long): Flow<NoteWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: NoteCrossRef)

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE notes SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE notes SET isArchived = 1 WHERE id = :id")
    suspend fun archiveNote(id: Long)
}
