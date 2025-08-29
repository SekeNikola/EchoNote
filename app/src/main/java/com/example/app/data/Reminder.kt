package com.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val time: Long,
    val isDone: Boolean = false
)
