package com.example.app.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import com.example.app.data.AppDatabase

class ServerService : Service() {
    private var serverJob: Job? = null
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("ServerService", "Service created")
        
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        
        // Initialize the database sync manager
        val database = AppDatabase.getDatabase(applicationContext)
        DataSyncManager.initialize(database)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ServerService", "Starting server service")
        
        serviceScope?.launch {
            try {
                // Set context for image handling
                KtorServer.setContext(this@ServerService)
                
                // Load existing data from database into server memory
                DataSyncManager.syncTasksFromDatabase()
                DataSyncManager.syncNotesFromDatabase()
                
                // Start the Ktor server
                KtorServer.start()
                Log.d("ServerService", "Ktor server started successfully")
                
                // Start ngrok tunnel (if needed)
                NgrokManager.startTunnel()
                Log.d("ServerService", "Server URLs: {NgrokManager.getServerUrls()}")
                
            } catch (e: Exception) {
                Log.e("ServerService", "Failed to start server", e)
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ServerService", "Stopping server service")
        
        serviceScope?.launch {
            try {
                NgrokManager.stopTunnel()
            } catch (e: Exception) {
                Log.e("ServerService", "Error stopping server", e)
            }
        }
        
        serverJob?.cancel()
        serviceScope?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
