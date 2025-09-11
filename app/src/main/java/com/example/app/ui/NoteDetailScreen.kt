package com.example.app.ui

import android.media.MediaPlayer
import android.widget.Toast
import org.json.JSONObject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.createdAtFormattedDate
import com.example.app.data.createdAtFormattedTime
import com.example.app.viewmodel.NoteViewModel

import kotlinx.coroutines.launch

enum class PlaybackState { Idle, Playing, Paused }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val note by viewModel.getNoteById(noteId).observeAsState()
    val date = note?.createdAtFormattedDate() ?: ""
    val time = note?.createdAtFormattedTime() ?: ""
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                note?.let { noteObj ->
                    // Use local state to prevent cursor jumping
                    var titleText by remember(noteObj.title) { mutableStateOf(noteObj.title) }
                    var isTitleFocused by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = titleText,
                            onValueChange = { newValue -> 
                                titleText = newValue
                                // Only update the database when focus is lost to prevent cursor jumping
                                if (!isTitleFocused) {
                                    viewModel.updateNoteTitle(noteObj.id, newValue)
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState -> 
                                    val wasFocused = isTitleFocused
                                    isTitleFocused = focusState.isFocused
                                    // Save to database when focus is lost
                                    if (wasFocused && !focusState.isFocused) {
                                        viewModel.updateNoteTitle(noteObj.id, titleText)
                                    }
                                }
                        )
                        
                        // Show placeholder for empty titles
                        if (titleText.isEmpty() && !isTitleFocused) {
                            Text(
                                text = "Untitled Note",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
                
                // Delete icon
                IconButton(onClick = { 
                    note?.let { noteObj -> viewModel.deleteNote(noteObj.id); onBack() }
                }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete note",
                        tint = Color(0xFFFF5252)
                    )
                }
            }

            // Date + Time
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

            Spacer(modifier = Modifier.height(16.dp))

            // Content area (removed tabs)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                // Content area
                val summaryScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2A2A2A))
                ) {
                    val scrollState = summaryScrollState
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            // Unified content field - combines all text content
                            var editableText by remember(note?.snippet, note?.transcript) {
                                mutableStateOf(
                                    try {
                                        val json = org.json.JSONObject(note?.snippet ?: "")
                                        
                                        // Get the unified text content (try different fields for compatibility)
                                        val content = json.optString("text", "").ifEmpty {
                                            json.optString("summary", "").ifEmpty {
                                                // If no structured content, use transcript or snippet as fallback
                                                note?.transcript?.takeIf { it.isNotEmpty() } ?: ""
                                            }
                                        }
                                        content
                                    } catch (e: Exception) { 
                                        // Fallback: use snippet directly or transcript
                                        note?.snippet?.takeIf { it.isNotEmpty() } ?: note?.transcript ?: ""
                                    }
                                )
                            }
                            
                            val initialTasks: List<String> = try {
                                val json = org.json.JSONObject(note?.snippet ?: "")
                                if (json.has("tasks")) {
                                    val arr = json.getJSONArray("tasks")
                                    List(arr.length()) { arr.getString(it) }
                                } else emptyList()
                            } catch (e: Exception) { emptyList() }
                            
                            var editableTasks by remember(note?.snippet) { mutableStateOf(initialTasks.toMutableList()) }
                            
                            val initialChecked = remember(note?.checklistState to initialTasks) {
                                try {
                                    note?.checklistState?.let { stateStr ->
                                        val arr = org.json.JSONArray(stateStr)
                                        MutableList(initialTasks.size) { idx ->
                                            if (idx < arr.length()) arr.getBoolean(idx) else false
                                        }
                                    } ?: MutableList(initialTasks.size) { false }
                                } catch (e: Exception) { MutableList(initialTasks.size) { false } }
                            }
                            
                            val checkedStates = remember(note?.snippet) { mutableStateListOf<Boolean>().apply { addAll(initialChecked) } }
                            var newTask by remember { mutableStateOf("") }
                            
                            // Single unified content area
                            var isFocused by remember { mutableStateOf(false) }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Summary & Notes",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 120.dp)
                                        .background(
                                            Color(0xFF2A2A3E),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    BasicTextField(
                                        value = editableText,
                                        onValueChange = { editableText = it },
                                        textStyle = TextStyle(
                                            color = Color.White, 
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                                    )
                                    if (editableText.isEmpty() && !isFocused) {
                                        Text(
                                            text = "Add your summary and thoughts here...\n\nThis area combines AI-generated summary with your personal notes.",
                                            color = Color.LightGray,
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                                
                                // Focus indicator
                                Spacer(modifier = Modifier.height(4.dp))
                                androidx.compose.material.Divider(
                                    color = if (isFocused) Color(0xFF4CAF50) else Color.Transparent,
                                    thickness = 2.dp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Tasks section
                            Text(
                                "Tasks", 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 16.sp, 
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            editableTasks.forEachIndexed { idx, task ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                    Checkbox(
                                        checked = checkedStates.getOrNull(idx) ?: false,
                                        onCheckedChange = { checked ->
                                            if (idx < checkedStates.size) checkedStates[idx] = checked
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF4CAF50),
                                            uncheckedColor = Color(0xFFB0B0B0),
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BasicTextField(
                                        value = task,
                                        onValueChange = { newText ->
                                            editableTasks[idx] = newText
                                        },
                                        textStyle = TextStyle(color = Color(0xFFB0B0B0), fontSize = 16.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        editableTasks.removeAt(idx)
                                        if (idx < checkedStates.size) checkedStates.removeAt(idx)
                                    }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Task", tint = Color(0xFFFF5252))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    var isFocused by remember { mutableStateOf(false) }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box {
                                            BasicTextField(
                                                value = newTask,
                                                onValueChange = { newTask = it },
                                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                                            )
                                            if (newTask.isEmpty() && !isFocused) {
                                                Text(
                                                    text = "Add task description...",
                                                    color = Color.LightGray,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                                )
                                            }
                                        }
                                        androidx.compose.material.Divider(
                                            color = if (isFocused) Color(0xFF4CAF50) else Color.LightGray,
                                            thickness = 2.dp
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (newTask.isNotBlank()) {
                                            editableTasks.add(newTask)
                                            checkedStates.add(false)
                                            newTask = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    contentPadding = PaddingValues(8.dp),
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = "Add Task",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Save changes logic - simplified for single content field
                            val originalText = remember(note?.snippet) {
                                try {
                                    val json = org.json.JSONObject(note?.snippet ?: "")
                                    json.optString("text", "").ifEmpty {
                                        json.optString("summary", "").ifEmpty {
                                            note?.transcript ?: ""
                                        }
                                    }
                                } catch (e: Exception) { 
                                    note?.snippet?.takeIf { it.isNotEmpty() } ?: note?.transcript ?: ""
                                }
                            }
                            
                            val originalTasks = remember(note?.snippet) {
                                try {
                                    val json = org.json.JSONObject(note?.snippet ?: "")
                                    if (json.has("tasks")) {
                                        val arr = json.getJSONArray("tasks")
                                        List(arr.length()) { arr.getString(it) }
                                    } else emptyList()
                                } catch (e: Exception) { emptyList<String>() }
                            }
                            
                            val hasChanges = editableText != originalText || editableTasks != originalTasks
                            if (hasChanges) {
                                Button(onClick = {
                                    note?.let { n ->
                                        val json = JSONObject()
                                        // Store the unified content as both 'text' and 'summary' for WebUI compatibility
                                        json.put("text", editableText)
                                        json.put("summary", editableText) // Also store as summary for WebUI sync
                                        json.put("tasks", org.json.JSONArray(editableTasks))
                                        coroutineScope.launch {
                                            viewModel.updateNoteSnippet(n.id, json.toString())
                                            // Save checklist state as before
                                            val checkedJson = org.json.JSONArray(checkedStates)
                                            viewModel.updateChecklistState(n.id, checkedJson.toString())
                                        }
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                                    Text("Save changes", color = Color.White)
                                }
                            }
                        }
                        
                        // Custom scrollbar indicator (gray, only if scrollable)
                        if (scrollState.maxValue > 1) {
                            Canvas(modifier = Modifier
                                .fillMaxHeight()
                                .width(6.dp)
                                .padding(vertical = 8.dp)
                            ) {
                                val proportion = if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f
                                val thumbHeight = 48.dp.toPx()
                                val y = proportion * (size.height - thumbHeight)
                                drawRoundRect(
                                    color = Color(0xFFB0B0B0),
                                    topLeft = Offset(x = 0f, y = y),
                                    size = androidx.compose.ui.geometry.Size(size.width, thumbHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
