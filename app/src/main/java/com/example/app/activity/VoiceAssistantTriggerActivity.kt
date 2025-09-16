package com.example.app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.app.MainActivity

class VoiceAssistantTriggerActivity : Activity() {
    
    companion object {
        const val TAG = "VoiceAssistantTriggerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "VoiceAssistantTriggerActivity started - opening AI voice screen directly")
        
        // Open MainActivity and navigate directly to AI voice screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "ai_voice")
        }
        startActivity(intent)
        
        // Finish this trigger activity immediately
        finish()
    }
}