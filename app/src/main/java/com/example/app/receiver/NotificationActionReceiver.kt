package com.example.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.app.worker.TaskActionWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", 0L)
        
        when (intent.action) {
            "MARK_DONE" -> {
                // Schedule work to mark task as done
                val data = Data.Builder()
                    .putLong("taskId", taskId)
                    .putString("action", "mark_done")
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskActionWorker>()
                    .setInputData(data)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            "SNOOZE" -> {
                // Schedule work to snooze task (reschedule for 15 minutes later)
                val data = Data.Builder()
                    .putLong("taskId", taskId)
                    .putString("action", "snooze")
                    .putLong("snoozeMinutes", 15)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskActionWorker>()
                    .setInputData(data)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            "SNOOZE_5" -> {
                // Schedule work to snooze task for 5 minutes
                val data = Data.Builder()
                    .putLong("taskId", taskId)
                    .putString("action", "snooze")
                    .putLong("snoozeMinutes", 5)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskActionWorker>()
                    .setInputData(data)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            "SNOOZE_15" -> {
                // Schedule work to snooze task for 15 minutes
                val data = Data.Builder()
                    .putLong("taskId", taskId)
                    .putString("action", "snooze")
                    .putLong("snoozeMinutes", 15)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskActionWorker>()
                    .setInputData(data)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            "SNOOZE_60" -> {
                // Schedule work to snooze task for 1 hour
                val data = Data.Builder()
                    .putLong("taskId", taskId)
                    .putString("action", "snooze")
                    .putLong("snoozeMinutes", 60)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<TaskActionWorker>()
                    .setInputData(data)
                    .build()
                
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            "EDIT" -> {
                // Open the app to edit the task
                val appIntent = Intent(context, com.example.app.MainActivity::class.java).apply {
                    putExtra("taskId", taskId)
                    putExtra("action", "edit")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(appIntent)
            }
        }
    }
}