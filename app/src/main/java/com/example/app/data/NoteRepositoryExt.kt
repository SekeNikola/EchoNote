package com.example.app.data

import kotlinx.coroutines.flow.Flow

fun NoteRepository.getNoteWithRelations(id: Long): Flow<NoteWithRelations?> =
    noteDao.getNoteWithRelations(id)

suspend fun NoteRepository.addCrossLink(noteId: Long, relatedNoteId: Long) =
    noteDao.insertCrossRef(NoteCrossRef(noteId, relatedNoteId))
