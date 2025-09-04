package com.example.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.app.MainActivity
import com.example.app.R

class LogionWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_logion)

        // Set up click listeners for each button
        setupButtonClick(context, views, R.id.btn_assistant, "assistant")
        setupButtonClick(context, views, R.id.btn_record_audio, "record_audio")
        setupButtonClick(context, views, R.id.btn_upload_audio, "upload_audio")
        setupButtonClick(context, views, R.id.btn_take_picture, "take_picture")
        setupButtonClick(context, views, R.id.btn_upload_image, "upload_image")
        setupButtonClick(context, views, R.id.btn_type_text, "type_text")
        setupButtonClick(context, views, R.id.btn_videos, "videos")
        setupButtonClick(context, views, R.id.btn_web_page, "web_page")
        setupButtonClick(context, views, R.id.btn_upload_files, "upload_files")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setupButtonClick(
        context: Context,
        views: RemoteViews,
        buttonId: Int,
        action: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("widget_action", action)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }
}