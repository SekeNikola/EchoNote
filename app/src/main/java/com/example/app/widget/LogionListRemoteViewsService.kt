package com.example.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.util.Log
import com.example.app.R
import com.example.app.data.AppDatabase
import kotlinx.coroutines.runBlocking

class LogionListRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return LogionListFactory(this.applicationContext, intent)
    }
}

class LogionListFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private val mode = intent.getStringExtra("mode") ?: "tasks"
    private var items = mutableListOf<DisplayItem>()
    
    data class DisplayItem(
        val id: Long,
        val title: String,
        val subtitle: String = "",
        val type: String
    )

    override fun onCreate() {
        Log.d("LogionWidgetSvc", "onCreate")
    }

    override fun onDataSetChanged() {
        Log.d("LogionWidgetSvc", "onDataSetChanged mode=$mode")
        
        try {
            val db = AppDatabase.getDatabase(context)
            items.clear()
            
            if (mode == "tasks") {
                val tasks = runBlocking { db.taskDao().getAllTasksOnce() }
                tasks.take(10).forEach { task ->
                    items.add(DisplayItem(
                        id = task.id,
                        title = task.title,
                        subtitle = if (task.isCompleted) "✓ Completed" else "○ Pending",
                        type = "task"
                    ))
                }
            } else {
                val notes = runBlocking { db.noteDao().getAllNotesOnce() }
                notes.take(10).forEach { note ->
                    items.add(DisplayItem(
                        id = note.id,
                        title = note.title.ifBlank { "(No title)" },
                        subtitle = note.snippet.take(50) + if (note.snippet.length > 50) "..." else "",
                        type = "note"
                    ))
                }
            }
            
            Log.d("LogionWidgetSvc", "Loaded ${items.size} items")
        } catch (e: Exception) {
            Log.e("LogionWidgetSvc", "Error loading data", e)
        }
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position >= items.size) return null
        
        val item = items[position]
        val views = RemoteViews(context.packageName, 
            if (item.type == "task") R.layout.widget_item_task else R.layout.widget_item_note)
        
        views.setTextViewText(R.id.widget_item_title, item.title)
        views.setTextViewText(R.id.widget_item_subtitle, item.subtitle)
        
        // Set click intent
        val clickIntent = Intent().apply {
            putExtra("item_id", item.id)
            putExtra("item_type", item.type)
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, clickIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 2
    override fun getItemId(position: Int): Long = if (position < items.size) items[position].id else position.toLong()
    override fun hasStableIds(): Boolean = true
}