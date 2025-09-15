package com.example.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.app.R
import java.util.concurrent.TimeUnit

object NotificationHelper {
    
    fun showSuccessNotification(context: Context, title: String, message: String) {
        val channelId = "success_notifications"
        createNotificationChannel(context, channelId, "Success", NotificationManager.IMPORTANCE_LOW)
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000) // Auto-dismiss after 3 seconds
            .build()
        
        NotificationManagerCompat.from(context).notify(
            (title + message).hashCode(),
            notification
        )
    }
    
    fun scheduleDailySummaryNotification(context: Context) {
        val data = Data.Builder()
            .putString("notificationType", "daily_summary")
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(getTimeUntil8AM(), TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun scheduleFocusNotification(context: Context) {
        val data = Data.Builder()
            .putString("notificationType", "focus_notification")
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(getTimeUntil9AM(), TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun showSyncNotification(context: Context, message: String) {
        val data = Data.Builder()
            .putString("notificationType", "sync_notification")
            .putString("noteTitle", message)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun showErrorNotification(context: Context, errorMessage: String) {
        val data = Data.Builder()
            .putString("notificationType", "error_notification")
            .putString("noteTitle", errorMessage)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    private fun createNotificationChannel(context: Context, channelId: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun getTimeUntil8AM(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 8)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If 8 AM has passed today, schedule for tomorrow
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis - now
    }
    
    private fun getTimeUntil9AM(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // If 9 AM has passed today, schedule for tomorrow
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis - now
    }
}