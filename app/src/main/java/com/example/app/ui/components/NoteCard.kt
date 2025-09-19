package com.example.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.Note
import org.json.JSONObject

// Utility function to extract display text from snippet
private fun extractDisplayText(snippet: String): String {
    return try {
        val json = JSONObject(snippet)
        val textContent = json.optString("text", "")
        val checkboxesArray = json.optJSONArray("checkboxes")
        
        // Build display string
        val displayParts = mutableListOf<String>()
        
        // Add text content if present
        if (textContent.isNotEmpty()) {
            displayParts.add(textContent)
        }
        
        // Add checkbox summary if present
        if (checkboxesArray != null && checkboxesArray.length() > 0) {
            val totalTasks = checkboxesArray.length()
            var completedTasks = 0
            
            for (i in 0 until checkboxesArray.length()) {
                val checkboxJson = checkboxesArray.getJSONObject(i)
                if (checkboxJson.optBoolean("checked", false)) {
                    completedTasks++
                }
            }
            
            val taskSummary = "$completedTasks/$totalTasks tasks completed"
            displayParts.add(taskSummary)
        }
        
        // Return combined text or fallback
        displayParts.joinToString(" • ").takeIf { it.isNotEmpty() }
            ?: json.optString("summary", "").takeIf { it.isNotEmpty() }
            ?: "Note with content"
    } catch (e: Exception) {
        // If JSON parsing fails, check if it's markdown-style checkboxes
        if (snippet.contains("- [")) {
            val lines = snippet.split("\n")
            val textLines = lines.filter { !it.matches(Regex("""- \[[x ]\] .+""")) && it.isNotBlank() }
            val checkboxLines = lines.filter { it.matches(Regex("""- \[[x ]\] .+""")) }
            
            val displayParts = mutableListOf<String>()
            
            // Add text content
            if (textLines.isNotEmpty()) {
                displayParts.add(textLines.first()) // Show first line of text
            }
            
            // Add checkbox summary
            if (checkboxLines.isNotEmpty()) {
                val completed = checkboxLines.count { it.contains("- [x]") }
                val total = checkboxLines.size
                displayParts.add("$completed/$total tasks completed")
            }
            
            displayParts.joinToString(" • ").takeIf { it.isNotEmpty() } ?: snippet
        } else {
            // Return original snippet (plain text), but limit length
            snippet.take(100).replace("\n", " ")
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = extractDisplayText(note.snippet),
                    color = Color(0xFFB0B0B0),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = if (note.isFavorite) Color(0xFFFFC107) else Color(0xFFB0B0B0)
                )
            }
        }
    }
}
