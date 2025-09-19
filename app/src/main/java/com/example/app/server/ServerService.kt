package com.example.app.server

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.data.AppDatabase
import com.example.app.util.NetworkChangeManager

class ServerService : Service() {
    private var serverJob: Job? = null
    private var serviceScope: CoroutineScope? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var healthCheckJob: Job? = null
    private var networkChangeManager: NetworkChangeManager? = null
    
    companion object {
        private const val CHANNEL_ID = "LogionServerChannel"
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TIMEOUT = 10 * 60 * 1000L // 10 minutes
        private const val HEALTH_CHECK_INTERVAL = 5 * 60 * 1000L // 5 minutes
        
        fun startService(context: Context) {
            val intent = Intent(context, ServerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ServerService", "Service created")
        
        // Create notification channel for foreground service
        createNotificationChannel()
        
        // Start foreground service immediately
        startForeground(NOTIFICATION_ID, createNotification("Starting Logion server..."))
        
        // Acquire wake lock to prevent CPU sleep
        acquireWakeLock()
        
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        
        // Initialize the database sync manager
        val database = AppDatabase.getDatabase(applicationContext)
        DataSyncManager.initialize(database)
        
        // Initialize network change monitoring
        networkChangeManager = NetworkChangeManager(applicationContext) { networkInfo ->
            // Update notification when network changes
            val networkStatus = "${networkInfo.type} (${networkInfo.ipAddress ?: "No IP"})"
            updateNotification("Logion server running - $networkStatus")
            Log.i("ServerService", "Network changed: $networkStatus")
        }
        networkChangeManager?.startMonitoring()
        
        // Start health check monitoring
        startHealthCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ServerService", "Starting server service")
        
        // Update notification to show server is starting
        updateNotification("Starting Logion server...")
        
        serviceScope?.launch {
            try {
                Log.d("ServerService", "Step 1: Setting context for image handling")
                // Set context for image handling
                KtorServer.setContext(this@ServerService)
                
                Log.d("ServerService", "Step 2: Loading data from database")
                // Load existing data from database into server memory
                DataSyncManager.syncTasksFromDatabase()
                DataSyncManager.syncNotesFromDatabase()
                
                Log.d("ServerService", "Step 3: Starting Ktor server on port 8080")
                // Start the Ktor server
                KtorServer.start()
                Log.d("ServerService", "Ktor server started successfully")
                
                // Update notification to show server is running
                updateNotification("Logion server is running on port 8080")
                
                Log.d("ServerService", "Step 4: Starting ngrok tunnel")
                // Start ngrok tunnel (if needed)
                NgrokManager.startTunnel()
                Log.d("ServerService", "Server URLs: {NgrokManager.getServerUrls()}")
                
                // Final notification update with network info
                val networkInfo = networkChangeManager?.getNetworkInfo()
                val networkStatus = "${networkInfo?.type} (${networkInfo?.ipAddress ?: "No IP"})"
                updateNotification("Logion server running - $networkStatus")
                
            } catch (e: Exception) {
                Log.e("ServerService", "Failed to start server", e)
                Log.e("ServerService", "Exception details: ${e.message}")
                Log.e("ServerService", "Stack trace: ${e.stackTraceToString()}")
                
                // Update notification to show error
                updateNotification("Logion server failed to start - Check logs")
            }
        }
        
        // Return START_STICKY so the service is restarted if killed by system
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ServerService", "Stopping server service")
        
        // Stop network change monitoring
        networkChangeManager?.stopMonitoring()
        networkChangeManager = null
        
        // Release wake lock
        releaseWakeLock()
        
        serviceScope?.launch {
            try {
                NgrokManager.stopTunnel()
            } catch (e: Exception) {
                Log.e("ServerService", "Error stopping server", e)
            }
        }
        
        serverJob?.cancel()
        serviceScope?.cancel()
        healthCheckJob?.cancel()
        
        // Send restart broadcast to restart the service
        try {
            val restartIntent = Intent("com.example.app.RESTART_SERVICE")
            restartIntent.setClass(this, com.example.app.receiver.ServiceRestartReceiver::class.java)
            sendBroadcast(restartIntent)
            Log.d("ServerService", "Restart broadcast sent")
        } catch (e: Exception) {
            Log.e("ServerService", "Failed to send restart broadcast", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Logion Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Logion server running in background"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Logion Server")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sparkle) // Use existing sparkle icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(message))
    }
    
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "LogionServer::WakeLock"
            ).apply {
                acquire(WAKE_LOCK_TIMEOUT)
            }
            Log.d("ServerService", "Wake lock acquired for ${WAKE_LOCK_TIMEOUT}ms")
        } catch (e: Exception) {
            Log.e("ServerService", "Failed to acquire wake lock", e)
        }
    }
    
    private fun renewWakeLock() {
        try {
            releaseWakeLock()
            acquireWakeLock()
            Log.d("ServerService", "Wake lock renewed")
        } catch (e: Exception) {
            Log.e("ServerService", "Failed to renew wake lock", e)
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d("ServerService", "Wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e("ServerService", "Failed to release wake lock", e)
        }
    }
    
    private fun startHealthCheck() {
        healthCheckJob = serviceScope?.launch {
            while (isActive) {
                try {
                    delay(HEALTH_CHECK_INTERVAL)
                    
                    // Renew wake lock periodically
                    renewWakeLock()
                    
                    // Update notification to show service is alive
                    val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    updateNotification("Logion server running - Last check: $currentTime")
                    
                    Log.d("ServerService", "Health check completed - service alive")
                    
                } catch (e: Exception) {
                    Log.e("ServerService", "Health check failed", e)
                }
            }
        }
    }
}
