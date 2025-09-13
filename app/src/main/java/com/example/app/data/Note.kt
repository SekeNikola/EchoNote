package com.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: String? = null,
    val title: String = "",
    val snippet: String = "",
    val transcript: String = "",
    val audioPath: String? = null,
    val imagePath: String? = null,
    val highlights: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null,
    /** Stores checklist state as a JSON string, e.g. [true, false, true] */
    val checklistState: String? = null
)

fun Note.createdAtFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(createdAt))
}

fun Note.createdAtFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(createdAt))
}
