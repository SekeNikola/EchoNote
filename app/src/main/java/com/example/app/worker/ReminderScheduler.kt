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
            .build()
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
