package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
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
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

enum class PlaybackState { Idle, Playing, Paused }

@androidx.compose.material3.ExperimentalMaterial3Api
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

    // Move playback state and player to top-level composable scope
    val playbackState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(PlaybackState.Idle) }
    val player = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<MediaPlayer?>(null) }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        androidx.compose.material3.TopAppBar(
            title = { Text("Note Detail", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Check, contentDescription = "Back", tint = Color(0xFFBB86FC))
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF222222)
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
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
            }
        Spacer(modifier = Modifier.height(16.dp))
        // Release player if audioPath changes
        androidx.compose.runtime.LaunchedEffect(audioPath) {
            player.value?.release()
            player.value = null
            playbackState.value = PlaybackState.Idle
        }

        fun startPlayback() {
            if (audioPath == null) return
            try {
                val mp = MediaPlayer()
                mp.setDataSource(audioPath)
                mp.prepare()
                mp.start()
                playbackState.value = PlaybackState.Playing
                player.value = mp
                Toast.makeText(context, "Playing audio...", Toast.LENGTH_SHORT).show()
                mp.setOnCompletionListener {
                    playbackState.value = PlaybackState.Idle
                    it.release()
                    player.value = null
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not play audio", Toast.LENGTH_SHORT).show()
                playbackState.value = PlaybackState.Idle
            }
        }

        fun pausePlayback() {
            player.value?.let {
                if (it.isPlaying) {
                    it.pause()
                    playbackState.value = PlaybackState.Paused
                }
            }
        }

        fun resumePlayback() {
            player.value?.let {
                it.start()
                playbackState.value = PlaybackState.Playing
            }
        }

        fun stopPlayback() {
            player.value?.let {
                it.stop()
                it.release()
                player.value = null
                playbackState.value = PlaybackState.Idle
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (playbackState.value) {
                PlaybackState.Idle -> {
                    IconButton(onClick = { startPlayback() }, enabled = audioPath != null) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio", tint = Color(0xFF03DAC6))
                    }
                    Text("Play recording", color = Color.White, fontSize = 16.sp)
                }
                PlaybackState.Playing -> {
                    IconButton(onClick = { pausePlayback() }, enabled = audioPath != null) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause Audio", tint = Color(0xFF03DAC6))
                    }
                    IconButton(onClick = { stopPlayback() }, enabled = audioPath != null) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Audio", tint = Color(0xFF03DAC6))
                    }
                    Text("Playing...", color = Color.White, fontSize = 16.sp)
                }
                PlaybackState.Paused -> {
                    IconButton(onClick = { resumePlayback() }, enabled = audioPath != null) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume Audio", tint = Color(0xFF03DAC6))
                    }
                    IconButton(onClick = { stopPlayback() }, enabled = audioPath != null) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Audio", tint = Color(0xFF03DAC6))
                    }
                    Text("Paused", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    // ...existing code...
        // Summarized text (assume note.snippet is summary)
        if (note?.snippet?.isNotBlank() == true) {
            Text(
                text = note!!.snippet,
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Voice/Audio controls (already present above)
        // Transcript
        if (transcript.isNotBlank()) {
            Text(
                text = transcript,
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.readAloud(noteId) }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Read Aloud", tint = Color(0xFF03DAC6))
                }
                Text("Read", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.setReminder(noteId) }) {
                    Icon(Icons.Default.Schedule, contentDescription = "Set Reminder", tint = Color(0xFFBB86FC))
                }
                Text("Remind", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.archiveNote(noteId) }) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive", tint = Color(0xFFB0B0B0))
                }
                Text("Archive", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.toggleFavorite(note!!) }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color(0xFFB0B0B0)
                    )
                }
                Text("Star", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { note?.let { viewModel.deleteNote(it.id); onBack() } }) {
                    Icon(Icons.Default.Archive, contentDescription = "Delete", tint = Color(0xFFFF5252))
                }
                Text("Delete", color = Color.White, fontSize = 12.sp)
            }
        }
        }
    }
}


