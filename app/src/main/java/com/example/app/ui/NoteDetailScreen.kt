package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.example.app.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val notes by viewModel.notes.observeAsState(emptyList())
    val note = remember(notes, noteId) {
        notes.find { it.id.toString() == noteId }
    }

    // Edit mode states
    var isEditMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var editableTitle by remember(note?.title) { mutableStateOf(note?.title ?: "") }
    var editableText by remember(note?.snippet) {
        mutableStateOf(
            try {
                val json = JSONObject(note?.snippet ?: "")
                json.optString("text", "")
            } catch (e: Exception) {
                note?.snippet ?: ""
            }
        )
    }

    // Update editable states when note changes
    LaunchedEffect(note?.title) {
        editableTitle = note?.title ?: ""
    }

    LaunchedEffect(note?.snippet) {
        editableText = try {
            val json = JSONObject(note?.snippet ?: "")
            json.optString("text", "")
        } catch (e: Exception) {
            note?.snippet ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Title - read-only or editable based on mode
            note?.let { noteObj: com.example.app.data.Note ->
                if (isEditMode) {
                    BasicTextField(
                        value = editableTitle,
                        onValueChange = { editableTitle = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = noteObj.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Save button in edit mode or menu in view mode
            if (isEditMode) {
                IconButton(
                    onClick = {
                        note?.let { noteObj: com.example.app.data.Note ->
                            coroutineScope.launch {
                                // Update title
                                viewModel.updateNoteTitle(noteObj.id, editableTitle)
                                
                                // Update content
                                val json = JSONObject()
                                json.put("text", editableText)
                                viewModel.updateNoteSnippet(noteObj.id, json.toString())
                                
                                // Sync to web server
                                try {
                                    com.example.app.server.KtorServer.updateNoteWithBroadcast(
                                        com.example.app.server.ServerNote(
                                            id = noteObj.id.toString(),
                                            title = editableTitle,
                                            body = editableText,
                                            updatedAt = java.time.Instant.now().toString()
                                        )
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("NoteDetailScreen", "Failed to sync note to server", e)
                                }
                            }
                            isEditMode = false
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                }
            } else {
                // Three dots menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Menu", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                isEditMode = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }

        // Date + Time
        note?.let { noteObj: com.example.app.data.Note ->
            val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(noteObj.createdAt))
            val parts = dateTime.split(" ")
            val date = parts[0]
            val time = parts[1]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = date, color = Color(0xFFB0B0B0), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = time, color = Color(0xFFB0B0B0), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content area - simplified read/edit mode
        note?.let { noteObj: com.example.app.data.Note ->
            val text = if (noteObj.snippet.isNotEmpty()) {
                try {
                    val json = JSONObject(noteObj.snippet)
                    json.optString("text", "")
                } catch (e: Exception) {
                    noteObj.snippet
                }
            } else {
                ""
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF2A2A2A))
                    .padding(16.dp)
            ) {
                if (isEditMode) {
                    BasicTextField(
                        value = editableText,
                        onValueChange = { editableText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = if (text.isNotEmpty()) text else "No content",
                        color = if (text.isNotEmpty()) Color.White else Color(0xFFB0B0B0),
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Outlined.Share,
                label = "Share",
                tint = Color.White
            ) {
                // Share action
            }
            ActionButton(
                icon = if (note?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.Star,
                label = "Star",
                tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color.White
            ) {
                note?.let { noteObj: com.example.app.data.Note -> viewModel.toggleFavorite(noteObj) }
            }
            ActionButton(icon = Icons.Outlined.Delete, label = "Delete", tint = Color(0xFFFF5252)) {
                note?.let { noteObj: com.example.app.data.Note -> viewModel.deleteNote(noteObj.id); onBack() }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}
