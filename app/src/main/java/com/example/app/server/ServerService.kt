package com.example.app.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import com.example.app.data.AppDatabase

class ServerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null
    private var ngrokJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ServerService", "Starting server service")
        
        // Initialize DataSyncManager with database
        val database = AppDatabase.getDatabase(applicationContext)
        DataSyncManager.initialize(database)
        
        serverJob = serviceScope.launch {
            try {
                // Load existing data from database into server memory
                DataSyncManager.syncTasksFromDatabase()
                DataSyncManager.syncNotesFromDatabase()
                
                KtorServer.start()
            } catch (e: Exception) {
                Log.e("ServerService", "Failed to start server", e)
            }
        }
        
        ngrokJob = serviceScope.launch {
            delay(3000) // Wait for server to start
            try {
                NgrokManager.startTunnel()
                Log.d("ServerService", "Server URLs: ${NgrokManager.getServerUrls()}")
            } catch (e: Exception) {
                Log.e("ServerService", "Failed to start tunnel", e)
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ServerService", "Stopping server service")
        serverJob?.cancel()
        ngrokJob?.cancel()
        serviceScope.cancel()
        NgrokManager.stopTunnel()
    }
}
