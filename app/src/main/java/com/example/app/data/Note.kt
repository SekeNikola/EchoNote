package com.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val snippet: String = "",
    val transcript: String = "",
    val audioPath: String? = null,
    val highlights: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null
)
