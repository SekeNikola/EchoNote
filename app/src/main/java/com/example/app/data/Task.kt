package com.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
@TypeConverters(TaskItemListConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverId: String? = null,
    val title: String,
    val description: String,
    val checkboxItems: List<CheckboxItem> = emptyList(), // New field for checkbox items
    val priority: String = "Medium", // High, Medium, Low
    val dueDate: Long,
    val duration: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
