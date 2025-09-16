package com.example.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.app.data.AppDatabase
import com.example.app.data.Task
import com.example.app.data.TaskRepository
import com.example.app.worker.ReminderWorker
import com.example.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    
    private val taskRepository = TaskRepository(AppDatabase.getDatabase(app).taskDao())
    
    // LiveData for observing tasks
    val allTasks = taskRepository.getAllTasks().asLiveData()
    val activeTasks = taskRepository.getActiveTasks().asLiveData()
    val completedTasks = taskRepository.getCompletedTasks(20).asLiveData()
    val topPriorityTasks = taskRepository.getTopPriorityTasks().asLiveData()
    
    // UI State
    private val _showCreateTaskDialog = MutableStateFlow(false)
    val showCreateTaskDialog: StateFlow<Boolean> = _showCreateTaskDialog
    
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask
    
    // Task operations
    fun createTask(
        title: String,
        description: String,
        priority: String,
        dueDate: Long,
        duration: String = ""
    ) = viewModelScope.launch {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            duration = duration,
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val taskId = taskRepository.insertTask(task)
        
        // Auto-schedule reminder if due date is in the future
        val currentTime = System.currentTimeMillis()
        if (dueDate > currentTime) {
            val delayMillis = dueDate - currentTime
            ReminderScheduler.scheduleTaskReminder(
                getApplication<android.app.Application>(),
                taskId,
                title,
                delayMillis
            )
        }
        
        hideCreateTaskDialog()
    }
    
    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }
    
    fun deleteTask(taskId: Long) = viewModelScope.launch {
        taskRepository.deleteTaskById(taskId)
    }
    
    fun toggleTaskComplete(taskId: Long) = viewModelScope.launch {
        val task = taskRepository.getTaskById(taskId)
        task?.let {
            taskRepository.markTaskCompleted(taskId, !it.isCompleted)
        }
    }
    
    fun updateTaskTitle(taskId: Long, title: String) = viewModelScope.launch {
        taskRepository.updateTaskTitle(taskId, title)
    }
    
    fun updateTaskDescription(taskId: Long, description: String) = viewModelScope.launch {
        taskRepository.updateTaskDescription(taskId, description)
    }
    
    fun updateTaskPriority(taskId: Long, priority: String) = viewModelScope.launch {
        taskRepository.updateTaskPriority(taskId, priority)
    }
    
    fun updateTaskDueDate(taskId: Long, dueDate: Long) = viewModelScope.launch {
        taskRepository.updateTaskDueDate(taskId, dueDate)
    }
    
    // UI State management
    fun showCreateTaskDialog() {
        _showCreateTaskDialog.value = true
    }
    
    fun hideCreateTaskDialog() {
        _showCreateTaskDialog.value = false
    }
    
    fun selectTask(task: Task) {
        _selectedTask.value = task
    }
    
    fun clearSelectedTask() {
        _selectedTask.value = null
    }
    
    // Schedule voice reminders
    fun scheduleVoiceReminder(title: String, delayMinutes: Long) = viewModelScope.launch {
        android.util.Log.d("TaskViewModel", "Scheduling voice reminder: title=$title, delayMinutes=$delayMinutes")
        
        val inputData = Data.Builder()
            .putString("noteTitle", title)
            .putString("notificationType", "task_reminder")
            .putLong("taskId", 0L) // For voice reminders, we can use 0 or create a temp task
            .build()
        
        val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(getApplication()).enqueue(reminderWork)
        android.util.Log.d("TaskViewModel", "Voice reminder work enqueued with ID: ${reminderWork.id}")
    }
    
    // Create task with reminder
    fun createTaskWithReminder(
        title: String,
        description: String,
        priority: String,
        dueDate: Long,
        duration: String = "",
        reminderMinutes: Long? = null
    ) = viewModelScope.launch {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            duration = duration,
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val taskId = taskRepository.insertTask(task)
        
        // Schedule reminder if specified
        reminderMinutes?.let {
            val inputData = Data.Builder()
                .putString("noteTitle", title)
                .putString("notificationType", "task_reminder")
                .putLong("taskId", taskId)
                .build()
            
            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(it, TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(getApplication()).enqueue(reminderWork)
        }
        
        hideCreateTaskDialog()
    }
    
    // Get tasks by date ranges
    fun getTasksDueToday() = taskRepository.getTasksDueToday().asLiveData()
    fun getTasksDueTomorrow() = taskRepository.getTasksDueTomorrow().asLiveData()
    fun getTasksDueLater() = taskRepository.getTasksDueLater().asLiveData()
    fun getOverdueTasks() = taskRepository.getOverdueTasks().asLiveData()
}