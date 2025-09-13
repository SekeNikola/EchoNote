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
        json.optString("text", "").takeIf { it.isNotEmpty() }
            ?: json.optString("summary", "").takeIf { it.isNotEmpty() }
            ?: snippet // Fallback to original snippet if no text/summary found
    } catch (e: Exception) {
        // If JSON parsing fails, return original snippet (it's probably plain text)
        snippet
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
