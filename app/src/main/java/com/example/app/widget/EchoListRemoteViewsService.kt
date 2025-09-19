package com.example.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.app.R
import com.example.app.data.AppDatabase
import com.example.app.data.Note
import com.example.app.data.Task
import kotlinx.coroutines.runBlocking

class EchoListRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_TASKS
        val appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, -1)
        return EchoListFactory(applicationContext, appWidgetId, mode)
    }

    class EchoListFactory(
        private val context: Context,
        private val appWidgetId: Int,
        private var mode: String
    ) : RemoteViewsFactory {
        private val db by lazy { AppDatabase.getDatabase(context) }
        private var tasks: List<Task> = emptyList()
        private var notes: List<Note> = emptyList()

        override fun onCreate() {}

        override fun onDataSetChanged() {
            // Load data synchronously for widget
            try {
                runBlocking {
                    if (mode == MODE_TASKS) {
                        tasks = db.taskDao().getAllTasksOnce()
                        notes = emptyList()
                    } else {
                        notes = db.noteDao().getAllNotesOnce()
                        tasks = emptyList()
                    }
                }
            } catch (e: Exception) {
                // Fallback to empty lists to avoid widget load failure
                tasks = emptyList()
                notes = emptyList()
            }
        }

        override fun onDestroy() {
            tasks = emptyList()
            notes = emptyList()
        }

        override fun getCount(): Int = if (mode == MODE_TASKS) tasks.size else notes.size

        override fun getViewAt(position: Int): RemoteViews? {
            return try {
                if (mode == MODE_TASKS) buildTaskView(position) else buildNoteView(position)
            } catch (_: Exception) {
                null
            }
        }

        private fun buildTaskView(position: Int): RemoteViews {
            val task = tasks[position]
            val rv = RemoteViews(context.packageName, R.layout.widget_item_task)
            rv.setTextViewText(R.id.widget_task_title, task.title)
            rv.setBoolean(R.id.widget_task_checkbox, "setChecked", task.isCompleted)

            // Fill-in intent for item click/open
            val fillIn = Intent().apply {
                putExtra(EXTRA_ITEM_TYPE, TYPE_TASK)
                putExtra(EXTRA_ITEM_ID, task.id)
            }
            rv.setOnClickFillInIntent(R.id.widget_task_title, fillIn)
            rv.setOnClickFillInIntent(R.id.widget_task_checkbox, Intent().apply {
                putExtra(EXTRA_ITEM_TYPE, TYPE_TASK)
                putExtra(EXTRA_ITEM_ID, task.id)
                putExtra(EXTRA_TOGGLE, true)
            })
            return rv
        }

        private fun buildNoteView(position: Int): RemoteViews {
            val note = notes[position]
            val rv = RemoteViews(context.packageName, R.layout.widget_item_note)
            rv.setTextViewText(R.id.widget_note_title, note.title.ifBlank { "(No title)" })
            rv.setTextViewText(R.id.widget_note_snippet, note.snippet.ifBlank { note.transcript.take(40) })

            val fillIn = Intent().apply {
                putExtra(EXTRA_ITEM_TYPE, TYPE_NOTE)
                putExtra(EXTRA_ITEM_ID, note.id)
            }
            rv.setOnClickFillInIntent(R.id.widget_note_title, fillIn)
            rv.setOnClickFillInIntent(R.id.widget_note_snippet, fillIn)
            return rv
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount(): Int = 2
        override fun getItemId(position: Int): Long = position.toLong()
        override fun hasStableIds(): Boolean = true

        fun updateMode(newMode: String) { this.mode = newMode }
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_APPWIDGET_ID = "appWidgetId"
        const val MODE_TASKS = "tasks"
        const val MODE_NOTES = "notes"
        const val EXTRA_ITEM_TYPE = "item_type"
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_TOGGLE = "toggle"
        const val TYPE_TASK = "task"
        const val TYPE_NOTE = "note"
    }
}
