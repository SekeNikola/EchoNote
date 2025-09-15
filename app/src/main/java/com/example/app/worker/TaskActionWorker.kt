package com.example.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.app.data.AppDatabase
import com.example.app.data.NoteRepository
import com.example.app.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskActionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskId = inputData.getLong("taskId", 0L)
            val action = inputData.getString("action") ?: return@withContext Result.failure()
            
            val database = AppDatabase.getDatabase(applicationContext)
            val taskRepository = TaskRepository(database.taskDao())
            
            when (action) {
                "mark_done" -> {
                    // Update task as completed
                    taskRepository.markTaskCompleted(taskId, true)
                    
                    // Show success notification
                    NotificationHelper.showSuccessNotification(
                        applicationContext,
                        "Task Completed",
                        "Task marked as done!"
                    )
                }
                
                "snooze" -> {
                    val snoozeMinutes = inputData.getLong("snoozeMinutes", 15L)
                    val newDueTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
                    
                    // Reschedule the task
                    taskRepository.updateTaskDueDate(taskId, newDueTime)
                    
                    // Schedule new reminder
                    ReminderScheduler.scheduleTaskReminder(
                        applicationContext,
                        taskId,
                        "Snoozed Task",
                        snoozeMinutes * 60 * 1000
                    )
                    
                    // Show snooze notification
                    NotificationHelper.showSuccessNotification(
                        applicationContext,
                        "Task Snoozed",
                        "Reminder set for ${snoozeMinutes} minutes"
                    )
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}