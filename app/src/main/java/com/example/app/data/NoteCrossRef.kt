package com.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_cross_refs", primaryKeys = ["noteId", "relatedNoteId"])
data class NoteCrossRef(
    val noteId: Long,
    val relatedNoteId: Long
)
