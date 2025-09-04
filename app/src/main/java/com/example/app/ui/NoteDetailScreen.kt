package com.example.app.ui

import android.media.MediaPlayer
import android.widget.Toast
import org.json.JSONObject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    val transcript by viewModel.fullTranscript.collectAsState()
    val summary by viewModel.summary.observeAsState("")
    val date = note?.createdAtFormattedDate() ?: ""
    val time = note?.createdAtFormattedTime() ?: ""
    val transcriptScrollState = rememberScrollState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp) // Leave space for sticky bar
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
                    BasicTextField(
                        value = noteObj.title,
                        onValueChange = { new -> viewModel.updateNoteTitle(noteObj.id, new) },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
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

            var selectedTab by remember { mutableStateOf(0) }
            val tabTitles = listOf("Summary", "Transcript")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF181818),
                contentColor = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) Color.White else Color(0xFFB0B0B0)) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Summary Tab
                        val summaryScrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF181818))
                        ) {
                            val scrollState = summaryScrollState
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState)
                                        .padding(16.dp)
                                ) {
                                    // Removed duplicate "Summary" title
                                    // --- Begin: Mixed summary and checklist support ---
                                    // Parse snippet as JSON with optional "text" and "tasks" fields
                                    var editableText by remember(note?.snippet) {
                                        mutableStateOf(
                                            try {
                                                val json = org.json.JSONObject(note?.snippet ?: "")
                                                json.optString("text", "")
                                            } catch (e: Exception) { note?.snippet ?: "" }
                                        )
                                    }
                                    // ...existing code...
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
                                    // Always extract and show the summary if it exists, never show raw JSON
                                    val summaryContent: String? = try {
                                        val json = org.json.JSONObject(note?.snippet ?: "")
                                        when {
                                            json.has("summary") && json.getString("summary").isNotBlank() -> json.getString("summary")
                                            json.has("text") && json.getString("text").isNotBlank() -> json.getString("text")
                                            else -> null
                                        }
                                    } catch (e: Exception) { null }
                                    if (!summaryContent.isNullOrBlank()) {
                                        Text(
                                            text = summaryContent,
                                            color = Color(0xFFB0B0B0),
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    var isSummaryFocused by remember { mutableStateOf(false) }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box {
                                            BasicTextField(
                                                value = editableText,
                                                onValueChange = { editableText = it },
                                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .onFocusChanged { focusState -> isSummaryFocused = focusState.isFocused },
                                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                                            )
                                            if (editableText.isEmpty() && !isSummaryFocused) {
                                                Text(
                                                    text = "Write your thoughts",
                                                    color = Color.LightGray,
                                                    fontSize = 16.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                                )
                                            }
                                        }
                                        androidx.compose.material.Divider(
                                            color = if (isSummaryFocused) Color(0xFF4CAF50) else Color.LightGray,
                                            thickness = 2.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    // Editable checklist UI (if any tasks or always show)
                                    Text("Tasks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                                    editableTasks.forEachIndexed { idx, task ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                            Checkbox(
                                                checked = checkedStates.getOrNull(idx) ?: false,
                                                onCheckedChange = { checked ->
                                                    if (idx < checkedStates.size) checkedStates[idx] = checked
                                                    // Optionally persist checklist state here
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
                                            val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
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
                                    // Save both text and tasks as JSON
                                    val originalSummaryText = remember(note?.snippet) {
                                        try {
                                            val json = org.json.JSONObject(note?.snippet ?: "")
                                            json.optString("text", "")
                                        } catch (e: Exception) { note?.snippet ?: "" }
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
                                    val hasSummaryChanges = editableText != originalSummaryText || editableTasks != originalTasks
                                    if (hasSummaryChanges) {
                                        Button(onClick = {
                                            note?.let { n ->
                                                val json = JSONObject()
                                                json.put("text", editableText)
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
                                    // --- End: Mixed summary and checklist support ---
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
                    1 -> {
                        // Transcript Tab
                        val transcriptScrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF181818))
                        ) {
                            val scrollState = transcriptScrollState
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Transcript",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    var editableTranscript by remember { mutableStateOf(
                                        if (transcript.isNotBlank()) transcript else note?.transcript.orEmpty()
                                    ) }
                                    val originalTranscript = remember(note?.transcript, transcript) {
                                        if (transcript.isNotBlank()) transcript else note?.transcript.orEmpty()
                                    }
                                    var isTranscriptFocused by remember { mutableStateOf(false) }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box {
                                            BasicTextField(
                                                value = editableTranscript,
                                                onValueChange = { editableTranscript = it },
                                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .onFocusChanged { focusState -> isTranscriptFocused = focusState.isFocused },
                                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                                            )
                                            if (editableTranscript.isEmpty() && !isTranscriptFocused) {
                                                Text(
                                                    text = "Write transcript...",
                                                    color = Color.LightGray,
                                                    fontSize = 16.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                                )
                                            }
                                        }
                                        androidx.compose.material.Divider(
                                            color = if (isTranscriptFocused) Color(0xFF4CAF50) else Color.LightGray,
                                            thickness = 2.dp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val coroutineScope = rememberCoroutineScope()
                                        val hasChanges = editableTranscript != originalTranscript
                                        if (hasChanges) {
                                            Button(
                                                onClick = {
                                                    note?.let { n ->
                                                        coroutineScope.launch {
                                                            // Save the transcript as a snippet JSON (preserving other fields if needed)
                                                            val snippetJson = try {
                                                                val json = org.json.JSONObject(n.snippet ?: "{}")
                                                                json.put("transcript", editableTranscript)
                                                                json.toString()
                                                            } catch (e: Exception) {
                                                                // fallback: just save transcript
                                                                org.json.JSONObject().put("transcript", editableTranscript).toString()
                                                            }
                                                            viewModel.updateNoteSnippet(n.id, snippetJson)
                                                            viewModel.updateTranscript(n.id, editableTranscript)
                                                            // Automatically update summary with OpenAI
                                                            viewModel.updateSummaryWithOpenAI(n.id, editableTranscript)
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Save changes", color = Color.White)
                                            }
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

        // Sticky action bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(icon = Icons.Outlined.PlayArrow, label = "Read") {
                viewModel.readAloud(noteId)
            }
            ActionButton(icon = Icons.Outlined.Schedule, label = "Remind") {
                viewModel.setReminder(noteId)
            }
            ActionButton(icon = Icons.Outlined.Archive, label = "Archive") {
                viewModel.archiveNote(noteId)
            }
            ActionButton(
                icon = Icons.Outlined.Star,
                label = "Star",
                tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color.White
            ) {
                note?.let { noteObj -> viewModel.toggleFavorite(noteObj) }
            }
            ActionButton(icon = Icons.Outlined.Delete, label = "Delete", tint = Color(0xFFFF5252)) {
                note?.let { noteObj -> viewModel.deleteNote(noteObj.id); onBack() }
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



