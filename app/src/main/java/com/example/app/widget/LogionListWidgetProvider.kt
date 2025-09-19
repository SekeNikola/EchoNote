package com.example.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.util.Log
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.data.AppDatabase
import com.example.app.data.TaskRepository
import com.example.app.server.KtorServer
import com.example.app.server.ServerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class LogionListWidgetProvider : AppWidgetProvider() {
    private val TAG = "LogionWidget"
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate ids=${appWidgetIds.toList()}")
        for (id in appWidgetIds) updateAppWidget(context, appWidgetManager, id)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
        notifyChanged(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted ids=${appWidgetIds.toList()}")
    }

    override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
        val action = intent.action
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    Log.d(TAG, "onReceive action=$action appWidgetId=$appWidgetId")
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val modeKey = modeKey(appWidgetId)
    val currentMode = prefs.getString(modeKey, LogionListRemoteViewsService.MODE_TASKS) ?: LogionListRemoteViewsService.MODE_TASKS

        when (action) {
            ACTION_TOGGLE_MODE -> {
                val newMode = if (currentMode == LogionListRemoteViewsService.MODE_TASKS) LogionListRemoteViewsService.MODE_NOTES else LogionListRemoteViewsService.MODE_TASKS
                Log.d(TAG, "Toggle mode $currentMode -> $newMode for $appWidgetId")
                prefs.edit().putString(modeKey, newMode).apply()
                notifyChanged(context)
            }
            ACTION_ADD -> {
                // Open app to relevant creation screen
                val widgetAction = if (currentMode == LogionListRemoteViewsService.MODE_TASKS) "create_task" else "create_note"
                Log.d(TAG, "ACTION_ADD open $widgetAction")
                val launch = Intent(context, MainActivity::class.java).apply {
                    putExtra("widget_action", widgetAction)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launch)
            }
            ACTION_ITEM_CLICK -> {
                val type = intent.getStringExtra(LogionListRemoteViewsService.EXTRA_ITEM_TYPE)
                val id = intent.getLongExtra(LogionListRemoteViewsService.EXTRA_ITEM_ID, -1L)
                val toggle = intent.getBooleanExtra(LogionListRemoteViewsService.EXTRA_TOGGLE, false)
                Log.d(TAG, "ITEM_CLICK type=$type id=$id toggle=$toggle")
                if (type == LogionListRemoteViewsService.TYPE_TASK && id > 0) {
                    if (toggle) {
                        toggleTask(context, id)
                    } else {
                        // Open task detail in app
                        val launch = Intent(context, MainActivity::class.java).apply {
                            putExtra("open_task_id", id)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(launch)
                    }
                } else if (type == LogionListRemoteViewsService.TYPE_NOTE && id > 0) {
                    val launch = Intent(context, MainActivity::class.java).apply {
                        putExtra("open_note_id", id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(launch)
                }
            }
        }
    }

    private fun toggleTask(context: Context, taskId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val dao = db.taskDao()
            val repo = TaskRepository(dao)
            val task = dao.getTaskById(taskId) ?: return@launch
            val newCompleted = !task.isCompleted
            repo.markTaskCompleted(taskId, newCompleted)

            // Broadcast to web via existing server logic
            try {
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                val serverTask = ServerTask(
                    id = task.serverId ?: task.id.toString(),
                    title = task.title,
                    body = task.description,
                    priority = task.priority,
                    dueDate = sdfDate.format(Date(task.dueDate)),
                    dueTime = sdfTime.format(Date(task.dueDate)),
                    done = newCompleted,
                    updatedAt = Date().toString()
                )
                KtorServer.updateTaskWithBroadcast(serverTask)
            } catch (_: Exception) { }

            notifyChanged(context)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val mode = prefs.getString(modeKey(appWidgetId), LogionListRemoteViewsService.MODE_TASKS) ?: LogionListRemoteViewsService.MODE_TASKS
    Log.d(TAG, "updateAppWidget id=$appWidgetId mode=$mode")

        val views = RemoteViews(context.packageName, R.layout.widget_echo_list)
    views.setTextViewText(R.id.widget_mode_label, if (mode == LogionListRemoteViewsService.MODE_TASKS) "Tasks" else "Notes")

        // Header: toggle mode
        val toggleIntent = Intent(context, javaClass).apply {
            action = ACTION_TOGGLE_MODE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val togglePI = PendingIntent.getBroadcast(context, appWidgetId, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_mode_label, togglePI)

        // Header: add button
        val addIntent = Intent(context, javaClass).apply {
            action = ACTION_ADD
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val addPI = PendingIntent.getBroadcast(context, appWidgetId + 1000, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_add, addPI)

        // Load tasks/notes directly and build static RemoteViews
        Log.d(TAG, "Loading $mode data directly")
        
        try {
            val db = AppDatabase.getDatabase(context)
            val tasks = if (mode == LogionListRemoteViewsService.MODE_TASKS) {
                runBlocking { db.taskDao().getAllTasksOnce() }
            } else emptyList()
            val notes = if (mode == LogionListRemoteViewsService.MODE_NOTES) {
                runBlocking { db.noteDao().getAllNotesOnce() }
            } else emptyList()
            
            Log.d(TAG, "Loaded ${tasks.size} tasks, ${notes.size} notes")
            
            if (tasks.isNotEmpty() || notes.isNotEmpty()) {
                // Show first few items as text
                val itemText = buildString {
                    if (mode == LogionListRemoteViewsService.MODE_TASKS) {
                        tasks.take(3).forEach { task ->
                            val status = if (task.isCompleted) "✓" else "○"
                            appendLine("$status ${task.title}")
                        }
                        if (tasks.size > 3) appendLine("... and ${tasks.size - 3} more")
                    } else {
                        notes.take(3).forEach { note ->
                            appendLine("• ${note.title.ifBlank { "(No title)" }}")
                        }
                        if (notes.size > 3) appendLine("... and ${notes.size - 3} more")
                    }
                }
                
                views.setViewVisibility(R.id.widget_list, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_empty, android.view.View.VISIBLE)
                views.setTextViewText(R.id.widget_empty, itemText.trim())
            } else {
                views.setViewVisibility(R.id.widget_list, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_empty, android.view.View.VISIBLE)
                views.setTextViewText(R.id.widget_empty, "No ${mode.lowercase()}\nTap + to add")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data", e)
            views.setViewVisibility(R.id.widget_list, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_empty, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widget_empty, "Widget loaded!\nTap + to add $mode")
        }
        
        // Keep the click handlers working
        // (ListView click template not needed for static mode)

        // Template for item clicks (not needed in static mode)
        // val clickIntent = Intent(context, javaClass).apply {
        //     action = ACTION_ITEM_CLICK
        //     putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // }
        // val clickPI = PendingIntent.getBroadcast(context, appWidgetId + 2000, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // views.setPendingIntentTemplate(R.id.widget_list, clickPI)

        try {
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        } catch (e: Exception) {
            Log.e(TAG, "updateAppWidget failed", e)
        }
    }

    private fun notifyChanged(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, javaClass))
        Log.d(TAG, "notifyChanged ids=${ids.toList()}")
        ids.forEach { updateAppWidget(context, mgr, it) }
    }

    companion object {
        private const val PREFS = "echo_widget_prefs"
        private fun modeKey(id: Int) = "mode_$id"
        private const val ACTION_TOGGLE_MODE = "com.example.app.widget.ACTION_TOGGLE_MODE"
        private const val ACTION_ADD = "com.example.app.widget.ACTION_ADD"
        private const val ACTION_ITEM_CLICK = "com.example.app.widget.ACTION_ITEM_CLICK"

        // Static method to refresh all widgets when data changes
        fun updateAllWidgets(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, LogionListWidgetProvider::class.java))
            val provider = LogionListWidgetProvider()
            ids.forEach { provider.updateAppWidget(context, mgr, it) }
        }
    }
}
