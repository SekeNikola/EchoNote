package com.example.app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VoiceAssistantManager {
    private const val PREFS_NAME = "voice_prefs"
    private const val KEY_TRIGGER_VOICE = "trigger_voice"
    private const val KEY_SHOW_ORB = "show_voice_orb"
    
    private val _shouldTriggerVoice = MutableStateFlow(false)
    val shouldTriggerVoice: StateFlow<Boolean> = _shouldTriggerVoice.asStateFlow()
    
    private val _shouldShowOrb = MutableStateFlow(false)
    val shouldShowOrb: StateFlow<Boolean> = _shouldShowOrb.asStateFlow()
    
    private var sharedPreferences: SharedPreferences? = null
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check if there's a pending voice trigger
        val hasTrigger = sharedPreferences?.getBoolean(KEY_TRIGGER_VOICE, false) ?: false
        if (hasTrigger) {
            triggerVoiceAssistant()
            clearTrigger()
        }
        
        // Check if there's a pending orb display
        val hasOrbTrigger = sharedPreferences?.getBoolean(KEY_SHOW_ORB, false) ?: false
        if (hasOrbTrigger) {
            showVoiceOrb()
            clearOrbTrigger()
        }
    }
    
    fun triggerVoiceAssistant() {
        _shouldTriggerVoice.value = true
        // Don't show orb when triggered programmatically (e.g., from shortcuts)
        // User can still use the orb from within the app UI
    }
    
    fun showVoiceOrb() {
        _shouldShowOrb.value = true
    }
    
    fun clearTrigger() {
        _shouldTriggerVoice.value = false
        sharedPreferences?.edit()?.putBoolean(KEY_TRIGGER_VOICE, false)?.apply()
    }
    
    fun clearOrbTrigger() {
        _shouldShowOrb.value = false
        sharedPreferences?.edit()?.putBoolean(KEY_SHOW_ORB, false)?.apply()
    }
    
    fun setPendingTrigger(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TRIGGER_VOICE, true).apply()
        prefs.edit().putBoolean(KEY_SHOW_ORB, true).apply()
    }
}