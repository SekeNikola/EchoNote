package com.example.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.app.R

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val noteTitle = inputData.getString("noteTitle") ?: "Reminder"
        val noteId = inputData.getLong("noteId", 0L)
        showNotification(noteTitle, noteId)
        return Result.success()
    }

    private fun showNotification(title: String, noteId: Long) {
        val channelId = "logion_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Tap to view or mark as done.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        NotificationManagerCompat.from(applicationContext).notify(noteId.toInt(), builder.build())
    }
}
