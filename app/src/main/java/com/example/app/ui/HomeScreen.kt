

package com.example.app.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import com.example.app.data.createdAtFormattedDate
import com.example.app.data.createdAtFormattedTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.media.MediaPlayer
import androidx.compose.material.icons.outlined.PlayArrow
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
            IconButton(
                onClick = {
                    onRecordClick?.invoke()
                },
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Icon(Icons.Outlined.RadioButtonChecked, contentDescription = "Start Recording", tint = Color.White)
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
            IconButton(
                onClick = {
                    // For demo: show overlay with a sample query
                    viewModel.showVoiceOverlay("What did I record this week?")
                },
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Icon(Icons.Outlined.SettingsVoice, contentDescription = "Voice Command Search", tint = Color.White)
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
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
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
            // Middle: Summary/Checklist
            val summaryText = note.snippet
            if (!summaryText.isNullOrBlank()) {
                val tasks = remember(summaryText) {
                    try {
                        val json = org.json.JSONObject(summaryText)
                        if (json.has("tasks")) {
                            val arr = json.getJSONArray("tasks")
                            List(arr.length()) { arr.getString(it) }
                        } else null
                    } catch (e: Exception) { null }
                }
                val summaryOnly = remember(summaryText) {
                    try {
                        val json = org.json.JSONObject(summaryText)
                        when {
                            json.has("summary") && json.getString("summary").isNotBlank() -> json.getString("summary")
                            json.has("text") && json.getString("text").isNotBlank() -> json.getString("text")
                            else -> null
                        }
                    } catch (e: Exception) { null }
                }
                // Always show placeholder under title and above checklist
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your thoughts...",
                    color = Color(0xFF888888),
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )
                // Show summary if present (even if tasks exist)
                if (summaryOnly != null && summaryOnly.isNotBlank()) {
                    Text(
                        text = summaryOnly,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = Color(0xFFEEEEEE),
                        modifier = Modifier.padding(bottom = if (tasks != null) 6.dp else 0.dp)
                    )
                }
                // Show checklist if present
                if (tasks != null) {
                    val checkedStates = try {
                        note.checklistState?.let { stateStr ->
                            val arr = org.json.JSONArray(stateStr)
                            List(tasks.size) { idx ->
                                if (idx < arr.length()) arr.getBoolean(idx) else false
                            }
                        } ?: List(tasks.size) { false }
                    } catch (e: Exception) { List(tasks.size) { false } }
                    Column {
                        tasks.forEachIndexed { idx, task ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                                Icon(
                                    imageVector = if (checkedStates[idx]) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (checkedStates[idx]) Color(0xFF4CAF50) else Color(0xFFB0B0B0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = task, color = Color(0xFFB0B0B0), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Bottom: Metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = note.createdAtFormattedDate(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
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
