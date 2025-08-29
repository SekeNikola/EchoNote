package com.example.app.util

import android.content.Context
import android.content.SharedPreferences

object ApiKeyProvider {
    private const val PREFS_NAME = "echo_note_prefs"
    private const val KEY_OPENAI = "openai_api_key"

    fun saveApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_OPENAI, key).apply()
    }

    fun getApiKey(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_OPENAI, null)
    }
}
