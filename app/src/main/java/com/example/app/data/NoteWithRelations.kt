package com.example.app.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// For cross-linking notes (AI-detected related topics)
data class NoteWithRelations(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "relatedNoteId"
        )
    )
    val relatedNotes: List<Note>
)
