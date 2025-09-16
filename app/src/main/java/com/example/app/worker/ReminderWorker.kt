package com.example.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.receiver.NotificationActionReceiver

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val noteTitle = inputData.getString("noteTitle") ?: "Reminder"
        val noteId = inputData.getLong("noteId", 0L)
        val taskId = inputData.getLong("taskId", 0L)
        val notificationType = inputData.getString("notificationType") ?: "reminder"
        
        android.util.Log.d("ReminderWorker", "Executing reminder work: type=$notificationType, title=$noteTitle, taskId=$taskId")
        
        when (notificationType) {
            "task_reminder" -> showTaskReminderNotification(noteTitle, taskId)
            "daily_summary" -> showDailySummaryNotification()
            "sync_notification" -> showSyncNotification(noteTitle)
            "error_notification" -> showErrorNotification(noteTitle)
            else -> showNotification(noteTitle, noteId)
        }
        
        return Result.success()
    }

    private fun showTaskReminderNotification(title: String, taskId: Long) {
        val channelId = "task_reminders"
        createNotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)

        // Check if this is a voice reminder and format accordingly
        val isVoiceReminder = title.startsWith("Reminder: ")
        val notificationTitle = if (isVoiceReminder) "Reminder" else "Task Reminder"
        val notificationContent = if (isVoiceReminder) title.removePrefix("Reminder: ") else title

        // Create actionable intents
        val markDoneIntent = createActionIntent("MARK_DONE", taskId)
        val snoozeIntent = createActionIntent("SNOOZE", taskId)
        val editIntent = createActionIntent("EDIT", taskId)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_check, "Mark Done", markDoneIntent)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozeIntent)
            .addAction(R.drawable.ic_edit, "Edit", editIntent)
            .setContentIntent(createOpenAppIntent())
            .build()

        NotificationManagerCompat.from(applicationContext).notify(taskId.toInt(), notification)
    }

    private fun showDailySummaryNotification() {
        val channelId = "daily_summary"
        createNotificationChannel(channelId, "Daily Summary", NotificationManager.IMPORTANCE_DEFAULT)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Good morning!")
            .setContentText("Tap to view today's tasks and notes")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createOpenAppIntent())
            .build()

        NotificationManagerCompat.from(applicationContext).notify(DAILY_SUMMARY_ID, notification)
    }

    private fun showSyncNotification(message: String) {
        val channelId = "sync_status"
        createNotificationChannel(channelId, "Sync Status", NotificationManager.IMPORTANCE_LOW)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Sync Complete")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
            .build()

        NotificationManagerCompat.from(applicationContext).notify(SYNC_NOTIFICATION_ID, notification)
    }

    private fun showErrorNotification(errorMessage: String) {
        val channelId = "errors"
        createNotificationChannel(channelId, "Errors", NotificationManager.IMPORTANCE_HIGH)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Connection Error")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createOpenAppIntent())
            .build()

        NotificationManagerCompat.from(applicationContext).notify(ERROR_NOTIFICATION_ID, notification)
    }

    private fun showNotification(title: String, noteId: Long) {
        val channelId = "logion_reminders"
        createNotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createOpenAppIntent())
            .build()
            
        NotificationManagerCompat.from(applicationContext).notify(noteId.toInt(), notification)
    }

    private fun createNotificationChannel(channelId: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance)
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createActionIntent(action: String, taskId: Long): PendingIntent {
        val intent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra("taskId", taskId)
        }
        return PendingIntent.getBroadcast(
            applicationContext,
            (action + taskId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val DAILY_SUMMARY_ID = 1001
        const val SYNC_NOTIFICATION_ID = 1003
        const val ERROR_NOTIFICATION_ID = 1004
    }
}
