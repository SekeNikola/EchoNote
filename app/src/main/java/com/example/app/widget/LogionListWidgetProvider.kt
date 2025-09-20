package com.example.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.view.View
import android.os.UserManager
import android.util.Log
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.data.AppDatabase
import kotlinx.coroutines.runBlocking

class LogionListWidgetProvider : AppWidgetProvider() {
    private val TAG = "LogionWidget"
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate ids=${appWidgetIds.toList()}")
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled")
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d(TAG, "updateAppWidget id=$appWidgetId")

        val views = RemoteViews(context.packageName, R.layout.widget_logion_list)

        // Mode from prefs (default Tasks)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val isTaskMode = prefs.getBoolean(modeKey(appWidgetId), true)

        // Header label and toggle
        views.setTextViewText(R.id.widget_mode_label, if (isTaskMode) "Tasks ▼" else "Notes ▼")
        val toggleIntent = Intent(context, javaClass).apply {
            action = ACTION_TOGGLE_MODE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val togglePI = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_mode_label, togglePI)

        // Plus opens app bottom sheet
        val addIntent = Intent(context, MainActivity::class.java).apply {
            if (isTaskMode) putExtra("open_create_task", true) else putExtra("open_create_note", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addPI = PendingIntent.getActivity(
            context,
            appWidgetId + 1000,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_add, addPI)

        // Build simple bullet list text (no ListView)
        val text = try {
            val userMgr = context.getSystemService(UserManager::class.java)
            if (userMgr != null && !userMgr.isUserUnlocked) {
                ""
            } else {
                val db = AppDatabase.getDatabase(context)
                runBlocking {
                    if (isTaskMode) {
                        val tasks = db.taskDao().getAllTasksOnce()
                        val lines = tasks.take(5).joinToString("\n") { "• ${it.title}" }
                        if (tasks.size > 5) "$lines\n…and ${tasks.size - 5} more" else lines
                    } else {
                        val notes = db.noteDao().getAllNotesOnce()
                        val lines = notes.take(5).joinToString("\n") { "• ${it.title.ifBlank { "(No title)" }}" }
                        if (notes.size > 5) "$lines\n…and ${notes.size - 5} more" else lines
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading items", e)
            ""
        }

        val body = if (text.isBlank()) "No ${if (isTaskMode) "tasks" else "notes"}" else text
    views.setTextViewText(R.id.widget_empty, body)
    views.setViewVisibility(R.id.widget_empty, View.VISIBLE)

        // Clicking body opens the list in app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            if (isTaskMode) putExtra("open_task_list", true) else putExtra("open_note_list", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPI = PendingIntent.getActivity(
            context,
            appWidgetId + 2000,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_empty, openPI)

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        Log.d(TAG, "onReceive action=$action appWidgetId=$appWidgetId")
        
        if (action == ACTION_TOGGLE_MODE && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val current = prefs.getBoolean(modeKey(appWidgetId), true)
            prefs.edit().putBoolean(modeKey(appWidgetId), !current).apply()

            val mgr = AppWidgetManager.getInstance(context)
            updateAppWidget(context, mgr, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_TOGGLE_MODE = "com.example.app.widget.ACTION_TOGGLE_MODE"
        private const val PREFS = "logion_widget_prefs"
        private fun modeKey(id: Int) = "mode_tasks_$id"
    }
}
