package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import androidx.compose.material.icons.filled.Star
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val note by viewModel.getNoteById(noteId).observeAsState()
    val title = note?.title ?: ""
    val transcript = note?.transcript ?: ""
    val highlights = note?.highlights ?: emptyList()
    val audioPath = note?.audioPath
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = title,
                onValueChange = { viewModel.updateNoteTitle(noteId, it) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Check, contentDescription = "Back", tint = Color(0xFFBB86FC))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (audioPath != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    try {
                        val player = MediaPlayer()
                        player.setDataSource(audioPath)
                        player.prepare()
                        player.start()
                        Toast.makeText(context, "Playing audio...", Toast.LENGTH_SHORT).show()
                        player.setOnCompletionListener { it.release() }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not play audio", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio", tint = Color(0xFF03DAC6))
                }
                Text("Play recording", color = Color.White, fontSize = 16.sp)
            }
        }
        Text(
            text = transcript,
            color = Color(0xFFB0B0B0),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        if (highlights.isNotEmpty()) {
            Text("Highlights:", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            highlights.forEach { highlight ->
                Text("- $highlight", color = Color(0xFFFFC107))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { viewModel.readAloud(noteId) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Read Aloud", tint = Color(0xFF03DAC6))
            }
            IconButton(onClick = { viewModel.setReminder(noteId) }) {
                Icon(Icons.Default.Schedule, contentDescription = "Set Reminder", tint = Color(0xFFBB86FC))
            }
            IconButton(onClick = { viewModel.archiveNote(noteId) }) {
                Icon(Icons.Default.Archive, contentDescription = "Archive", tint = Color(0xFFB0B0B0))
            }
            IconButton(onClick = { viewModel.toggleFavorite(note!!) }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color(0xFFB0B0B0)
                )
            }
        }
    }
}


