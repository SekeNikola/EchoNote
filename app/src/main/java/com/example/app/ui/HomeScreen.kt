package com.example.app.ui

import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.ui.res.painterResource
import com.example.app.R
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.media.MediaPlayer
import androidx.compose.material.icons.outlined.PlayArrow
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
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
        // Floating plus button and bottom sheet
    val showSheet = remember { androidx.compose.runtime.mutableStateOf(false) }
    val showAssistantSheet = remember { androidx.compose.runtime.mutableStateOf(false) }
    val isRecording by viewModel.isRecording.observeAsState(false)
    val transcript by viewModel.fullTranscript.collectAsState()
    val aiResponse by viewModel.aiResponse.observeAsState("")
    val assistantChatHistory by viewModel.assistantChatHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val chatListState = remember { androidx.compose.foundation.lazy.LazyListState() }
        if (showSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showSheet.value = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color(0xFFF8F8F8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Create New Note", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(20.dp))
                    // Audio
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SheetActionButton(icon = Icons.Outlined.SettingsVoice, label = "Record Audio", modifier = Modifier.weight(1f)) {
                            onRecordClick?.invoke()
                            showSheet.value = false
                        }
                        SheetActionButton(icon = Icons.Outlined.RadioButtonChecked, label = "Upload Audio", modifier = Modifier.weight(1f)) { /* TODO */ }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Photo
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SheetActionButton(icon = Icons.Outlined.CalendarToday, label = "Take Picture", modifier = Modifier.weight(1f)) { /* TODO */ }
                        SheetActionButton(icon = Icons.Outlined.CheckBoxOutlineBlank, label = "Upload Image", modifier = Modifier.weight(1f)) { /* TODO */ }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Other
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SheetActionButton(icon = Icons.Filled.Star, label = "Type Text", modifier = Modifier.weight(1f)) { /* TODO */ }
                        SheetActionButton(icon = Icons.Outlined.PlayArrow, label = "YouTube Video", modifier = Modifier.weight(1f)) { /* TODO */ }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SheetActionButton(icon = Icons.Outlined.AccessTime, label = "Web Page URL", modifier = Modifier.weight(1f)) { /* TODO */ }
                        SheetActionButton(icon = Icons.Outlined.CheckBox, label = "Upload PDF", modifier = Modifier.weight(1f)) { /* TODO */ }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        if (showAssistantSheet.value) {
            val context = LocalContext.current
            val assistantSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            ModalBottomSheet(
                onDismissRequest = {
                    showAssistantSheet.value = false
                    viewModel.stopSpeechRecognition()
                    viewModel.clearAssistantChat()
                },
                sheetState = assistantSheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color(0xFF1E1E1E)
            ) {
                androidx.compose.runtime.LaunchedEffect(showAssistantSheet.value) {
                    if (showAssistantSheet.value) {
                        viewModel.startSpeechRecognition(context)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRecording) "Listening..." else "AI Assistant",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Chat history UI
                    androidx.compose.foundation.lazy.LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(bottom = 8.dp),
                        reverseLayout = false
                    ) {
                        items(assistantChatHistory) { chatPair ->
                            val (user, ai) = chatPair
                            // User message bubble
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFBB86FC), shape = RoundedCornerShape(16.dp))
                                        .padding(10.dp)
                                        .widthIn(max = 260.dp)
                                ) {
                                    Text(text = user, color = Color.White, fontSize = 15.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // AI message bubble
                            if (ai.isNotBlank()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF2D2D2D), shape = RoundedCornerShape(16.dp))
                                            .padding(10.dp)
                                            .widthIn(max = 260.dp)
                                    ) {
                                        Text(text = ai, color = Color.White, fontSize = 15.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    // Auto-scroll to latest message - triggers on any chat history change
                    LaunchedEffect(assistantChatHistory) {
                        if (assistantChatHistory.isNotEmpty()) {
                            coroutineScope.launch {
                                chatListState.scrollToItem(assistantChatHistory.size - 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            coroutineScope.launch {
                                if (isRecording) viewModel.stopSpeechRecognition() else viewModel.startSpeechRecognition(context)
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text(if (isRecording) "Stop Listening" else "Start Listening", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = {
                            showAssistantSheet.value = false
                            viewModel.stopSpeechRecognition()
                            viewModel.clearAssistantChat()
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
        // FloatingActionButton (plus button)
        FloatingActionButton(
            onClick = { showSheet.value = true },
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Note", tint = Color.White)
        }
        // Floating AI assistant button with purple background
        Box(
            modifier = Modifier
                .padding(end = 24.dp, bottom = 96.dp)
                .align(Alignment.BottomEnd)
        ) {
            IconButton(
                onClick = { showAssistantSheet.value = true },
                modifier = Modifier
                    .background(Color(0xFFBB86FC), shape = RoundedCornerShape(50))
                    .size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sparkle_single),
                    contentDescription = "Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
    // Voice command overlay
    if (isVoiceOverlayVisible) {
        VoiceCommandOverlay(
            viewModel = viewModel,
            onDismiss = { viewModel.hideVoiceOverlay() }
        )
    }
}

@Composable
fun SheetActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = modifier.padding(horizontal = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Black, fontSize = 12.sp)
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
