package com.example.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.app.server.ServerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            
            Log.d("BootReceiver", "Device booted or app updated, starting Logion server service")
            
            try {
                ServerService.startService(context)
                Log.d("BootReceiver", "Logion server service started successfully")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start Logion server service", e)
            }
        }
    }
}
