package com.example.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.*

class TaskRepository(private val taskDao: TaskDao, private val context: Context? = null) {
    
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    fun getCompletedTasks(limit: Int = 20): Flow<List<Task>> = taskDao.getCompletedTasks(limit)
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    
    suspend fun insertTask(task: Task): Long = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteById(id)
    
    suspend fun markTaskCompleted(id: Long, isCompleted: Boolean) {
        taskDao.updateCompleted(id, isCompleted)
        // Refresh widget when task completion changes
        context?.let {
            try {
                val widgetClass = Class.forName("com.example.app.widget.LogionListWidgetProvider")
                val updateMethod = widgetClass.getMethod("updateAllWidgets", Context::class.java)
                updateMethod.invoke(null, it)
            } catch (_: Exception) { /* Widget not available */ }
        }
    }
    
    suspend fun updateTaskDueDate(id: Long, dueDate: Long) {
        // We need to get the task first, then update it
        val task = taskDao.getTaskById(id)
        task?.let {
            taskDao.update(it.copy(dueDate = dueDate, updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun updateTaskTitle(id: Long, title: String) {
        val task = taskDao.getTaskById(id)
        task?.let {
            taskDao.update(it.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun updateTaskDescription(id: Long, description: String) {
        val task = taskDao.getTaskById(id)
        task?.let {
            taskDao.update(it.copy(description = description, updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun updateTaskPriority(id: Long, priority: String) {
        val task = taskDao.getTaskById(id)
        task?.let {
            taskDao.update(it.copy(priority = priority, updatedAt = System.currentTimeMillis()))
        }
    }
    
    // Date-based task queries - simplified for now using getAllTasks and filtering
    fun getTasksDueToday(): Flow<List<Task>> {
        // For now, we'll use a simple implementation that filters active tasks
        return getActiveTasks()
    }
    
    fun getTasksDueTomorrow(): Flow<List<Task>> {
        return getActiveTasks()
    }
    
    fun getTasksDueLater(): Flow<List<Task>> {
        return getActiveTasks()
    }
    
    fun getOverdueTasks(): Flow<List<Task>> {
        return getActiveTasks()
    }
    
    fun getTopPriorityTasks(): Flow<List<Task>> {
        return getActiveTasks()
    }
}