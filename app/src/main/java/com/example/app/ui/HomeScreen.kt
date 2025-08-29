package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked // Added
import androidx.compose.material.icons.filled.SettingsVoice // Added
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import com.example.app.data.createdAtFormattedDate
import com.example.app.data.createdAtFormattedTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.media.MediaPlayer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import com.example.app.data.Note
import com.example.app.ui.VoiceCommandOverlay // Re-added import
// import androidx.compose.material3.ExperimentalMaterial3Api // Unused import

@OptIn(ExperimentalMaterial3Api::class) // Removed redundant qualifier
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit,
    onRecordClick: (() -> Unit)? = null
) {
    val notes by viewModel.notes.observeAsState(listOf())
    val searchQuery by viewModel.searchQuery.observeAsState("")
    val isVoiceOverlayVisible by viewModel.isVoiceOverlayVisible.observeAsState(false)
    // val context = LocalContext.current
    // var showVoiceInput = false // Unused variable

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it }
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onFavorite = { viewModel.toggleFavorite(note) },
                        showTranscript = true,
                        showPlayButton = note.audioPath != null
                    )
                }
            }
        }
        // Floating record button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    onRecordClick?.invoke()
                },
                containerColor = Color(0xFFBB86FC),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Icon(Icons.Filled.RadioButtonChecked, contentDescription = "Start Recording") // Changed icon and description
            }
        }
        // Floating voice command mic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    // For demo: show overlay with a sample query
                    viewModel.showVoiceOverlay("What did I record this week?")
                },
                containerColor = Color(0xFF03DAC6),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Icon(Icons.Filled.SettingsVoice, contentDescription = "Voice Command Search") // Changed icon and description
            }
        }
        // Voice command overlay
        if (isVoiceOverlayVisible) {
            VoiceCommandOverlay( // Removed fully qualified name
                viewModel = viewModel,
                onDismiss = { viewModel.hideVoiceOverlay() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Removed redundant qualifier
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search notes", color = Color(0xFFB0B0B0)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = TextFieldDefaults.colors( // Changed to colors
            // containerColor = Color(0xFF1F1F1F) // Commented out due to No parameter with name 'containerColor' found
        )
    )
}



@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    showTranscript: Boolean = false,
    showPlayButton: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top: Title
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Middle: Summary
            Text(
                text = note.snippet,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Bottom: Metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.createdAtFormattedDate(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.createdAtFormattedTime(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
