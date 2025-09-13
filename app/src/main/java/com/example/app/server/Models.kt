package com.example.app.server

data class Task(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val updatedAt: String
)

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val imagePath: String? = null,
    val updatedAt: String
)

data class SyncMessage(
    val type: String, // "task_updated", "note_updated", "task_added", "note_added"
    val data: Any
)
