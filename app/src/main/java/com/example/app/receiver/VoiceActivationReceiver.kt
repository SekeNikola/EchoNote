package com.example.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class VoiceActivationReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_VOICE_ASSISTANT_TRIGGER = "com.example.app.VOICE_ASSISTANT_TRIGGER"
        const val TAG = "VoiceActivationReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Voice activation broadcast received: ${intent.action}")
        
        when (intent.action) {
            ACTION_VOICE_ASSISTANT_TRIGGER,
            "com.example.app.KEYMAP_VOICE_TRIGGER" -> {
                // Start VoiceAssistantTriggerActivity to navigate directly to AI voice screen
                val triggerIntent = Intent(context, com.example.app.activity.VoiceAssistantTriggerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                Log.d(TAG, "Starting VoiceAssistantTriggerActivity for direct AI voice navigation")
                context.startActivity(triggerIntent)
            }
            
            Intent.ACTION_VOICE_COMMAND -> {
                // Handle system voice command - also navigate directly to AI voice
                val triggerIntent = Intent(context, com.example.app.activity.VoiceAssistantTriggerActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                Log.d(TAG, "Starting VoiceAssistantTriggerActivity for system voice command")
                context.startActivity(triggerIntent)
            }
        }
    }
}