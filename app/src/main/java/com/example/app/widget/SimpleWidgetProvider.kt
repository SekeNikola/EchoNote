package com.example.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.data.AppDatabase
import kotlinx.coroutines.runBlocking

class SimpleWidgetProvider : AppWidgetProvider() {
    private val TAG = "SimpleWidget"
    
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

        try {
            val views = RemoteViews(context.packageName, R.layout.widget_simple)

            // Get current mode from preferences (default Tasks)
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val isTaskMode = prefs.getBoolean(modeKey(appWidgetId), true)

            // Set mode label
            views.setTextViewText(R.id.widget_mode, if (isTaskMode) "Tasks" else "Notes")

            // Load real content from database
            val content = try {
                val db = AppDatabase.getDatabase(context)
                runBlocking {
                    if (isTaskMode) {
                        val tasks = db.taskDao().getAllTasksOnce()
                        if (tasks.isEmpty()) {
                            "No tasks yet\n\nTap + to create your first task"
                        } else {
                            val taskList = tasks.take(6).joinToString("\n") { it.title }
                            if (tasks.size > 6) {
                                "$taskList\n\n...and ${tasks.size - 6} more"
                            } else {
                                taskList
                            }
                        }
                    } else {
                        val notes = db.noteDao().getAllNotesOnce()
                        if (notes.isEmpty()) {
                            "No notes yet\n\nTap + to create your first note"
                        } else {
                            val noteList = notes.take(6).joinToString("\n") { 
                                val title = it.title.ifBlank { "(Untitled)" }
                                title
                            }
                            if (notes.size > 6) {
                                "$noteList\n\n...and ${notes.size - 6} more"
                            } else {
                                noteList
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load real content", e)
                if (isTaskMode) {
                    "Failed to load tasks\nTap to open app"
                } else {
                    "Failed to load notes\nTap to open app"
                }
            }
            views.setTextViewText(R.id.widget_content, content)

            // Mode toggle click
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
            views.setOnClickPendingIntent(R.id.widget_mode, togglePI)

            // Plus button - opens app with bottom sheet
            val addIntent = Intent(context, MainActivity::class.java).apply {
                if (isTaskMode) {
                    putExtra("open_create_task", true)
                } else {
                    putExtra("open_create_note", true)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPI = PendingIntent.getActivity(
                context,
                appWidgetId + 1000,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add, addPI)

            // Content tap - opens respective list in app
            val openIntent = Intent(context, MainActivity::class.java).apply {
                if (isTaskMode) {
                    putExtra("open_task_list", true)
                } else {
                    putExtra("open_note_list", true)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPI = PendingIntent.getActivity(
                context,
                appWidgetId + 2000,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_content, openPI)

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "Widget updated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget", e)
            
            // Fallback: try super minimal layout
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_simple)
                views.setTextViewText(R.id.widget_mode, "Logion")
                views.setTextViewText(R.id.widget_content, "Widget loaded successfully!\n\nTap to open app")
                views.setTextViewText(R.id.widget_add, "Open App")
                
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPI = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_content, openPI)
                views.setOnClickPendingIntent(R.id.widget_add, openPI)
                views.setOnClickPendingIntent(R.id.widget_mode, openPI)
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d(TAG, "Fallback widget updated")
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback widget failed", e2)
            }
        }
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
        private const val PREFS = "simple_widget_prefs"
        private fun modeKey(id: Int) = "mode_tasks_$id"
        
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, SimpleWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                if (widgetIds.isNotEmpty()) {
                    val provider = SimpleWidgetProvider()
                    provider.onUpdate(context, appWidgetManager, widgetIds)
                }
            } catch (e: Exception) {
                Log.e("SimpleWidget", "Failed to update all widgets", e)
            }
        }
    }
}