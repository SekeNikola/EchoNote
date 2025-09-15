package com.example.app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VoiceAssistantManager {
    private const val PREFS_NAME = "voice_prefs"
    private const val KEY_TRIGGER_VOICE = "trigger_voice"
    
    private val _shouldTriggerVoice = MutableStateFlow(false)
    val shouldTriggerVoice: StateFlow<Boolean> = _shouldTriggerVoice.asStateFlow()
    
    private var sharedPreferences: SharedPreferences? = null
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check if there's a pending voice trigger
        val hasTrigger = sharedPreferences?.getBoolean(KEY_TRIGGER_VOICE, false) ?: false
        if (hasTrigger) {
            triggerVoiceAssistant()
            clearTrigger()
        }
    }
    
    fun triggerVoiceAssistant() {
        _shouldTriggerVoice.value = true
    }
    
    fun clearTrigger() {
        _shouldTriggerVoice.value = false
        sharedPreferences?.edit()?.putBoolean(KEY_TRIGGER_VOICE, false)?.apply()
    }
    
    fun setPendingTrigger(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TRIGGER_VOICE, true).apply()
    }
}