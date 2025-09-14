package com.example.app.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class for exporting/importing all app data
 */
data class ExportData(
    @SerializedName("version") val version: String = "1.0",
    @SerializedName("exportDate") val exportDate: String = getCurrentTimestamp(),
    @SerializedName("notes") val notes: List<Note> = emptyList(),
    @SerializedName("tasks") val tasks: List<Task> = emptyList(),
    @SerializedName("chatMessages") val chatMessages: List<ChatMessage> = emptyList()
) {
    companion object {
        private fun getCurrentTimestamp(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
        
        fun fromJson(json: String): ExportData? {
            return try {
                Gson().fromJson(json, ExportData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toJson(): String {
        return Gson().toJson(this)
    }
}