package com.example.app.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    
    fun scheduleReminder(context: Context, noteId: Long, noteTitle: String, delayMillis: Long) {
        val data = Data.Builder()
            .putLong("noteId", noteId)
            .putString("noteTitle", noteTitle)
            .putString("notificationType", "reminder")
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun scheduleTaskReminder(context: Context, taskId: Long, taskTitle: String, delayMillis: Long) {
        val data = Data.Builder()
            .putLong("taskId", taskId)
            .putString("noteTitle", taskTitle)
            .putString("notificationType", "task_reminder")
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag("task_reminder_$taskId")
            .build()
            
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun cancelTaskReminder(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_reminder_$taskId")
    }
    
    fun scheduleRecurringNotifications(context: Context) {
        // Schedule daily summary at 8 AM
        NotificationHelper.scheduleDailySummaryNotification(context)
        
        // Schedule focus notification at 9 AM
        NotificationHelper.scheduleFocusNotification(context)
    }
}
