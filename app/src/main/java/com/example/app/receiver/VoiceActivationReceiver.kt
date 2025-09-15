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
            ACTION_VOICE_ASSISTANT_TRIGGER -> {
                // Start the main activity with voice assistant flag
                val mainIntent = Intent(context, com.example.app.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("start_voice_assistant", true)
                    action = "android.intent.action.VOICE_ASSISTANT"
                }
                context.startActivity(mainIntent)
            }
            
            Intent.ACTION_VOICE_COMMAND -> {
                // Handle system voice command
                val mainIntent = Intent(context, com.example.app.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("start_voice_assistant", true)
                }
                context.startActivity(mainIntent)
            }
        }
    }
}