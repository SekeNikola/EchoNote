package com.example.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.app.MainActivity
import com.example.app.util.VoiceAssistantManager

class VoiceAssistantService : Service() {
    
    companion object {
        const val TAG = "VoiceAssistantService"
        const val ACTION_TRIGGER_VOICE_ASSISTANT = "com.example.app.TRIGGER_VOICE_ASSISTANT"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VoiceAssistantService started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_TRIGGER_VOICE_ASSISTANT -> {
                triggerVoiceAssistant()
            }
        }
        
        // Stop the service immediately after handling the intent
        stopSelf()
        return START_NOT_STICKY
    }
    
    private fun triggerVoiceAssistant() {
        Log.d(TAG, "Triggering voice assistant")
        
        // Show the voice orb overlay
        VoiceOrbOverlayService.showOrb(this)
        
        // Set the voice trigger flag for when the app is eventually opened
        VoiceAssistantManager.triggerVoiceAssistant()
        
        // Don't start MainActivity - just show the orb overlay
        Log.d(TAG, "Voice assistant triggered with overlay only")
    }
}