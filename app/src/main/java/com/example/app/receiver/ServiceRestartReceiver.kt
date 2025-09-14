package com.example.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.app.server.ServerService

class ServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ServiceRestartReceiver", "Service restart broadcast received")
        
        try {
            ServerService.startService(context)
            Log.d("ServiceRestartReceiver", "Logion server service restarted successfully")
        } catch (e: Exception) {
            Log.e("ServiceRestartReceiver", "Failed to restart Logion server service", e)
        }
    }
}