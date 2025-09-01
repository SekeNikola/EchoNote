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
                                    Text(
                                        text = "Summary",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    val summaryText = note?.snippet?.takeIf { it.isNotBlank() } ?: summary
                                    if (!summaryText.isNullOrBlank()) {
                                        // Try to parse as JSON for tasks or summary
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
                                                if (json.has("summary")) json.getString("summary") else null
                                            } catch (e: Exception) { null }
                                        }
                                        if (tasks != null && note != null) {
                                            // Restore checklist state from note.checklistState (JSON array of booleans)
                                            val initialChecked = remember(note!!.checklistState to tasks) {
                                                try {
                                                    note!!.checklistState?.let { stateStr ->
                                                        val arr = org.json.JSONArray(stateStr)
                                                        MutableList(tasks.size) { idx ->
                                                            if (idx < arr.length()) arr.getBoolean(idx) else false
                                                        }
                                                    } ?: MutableList(tasks.size) { false }
                                                } catch (e: Exception) { MutableList(tasks.size) { false } }
                                            }
                                            val checkedStates = remember { mutableStateListOf<Boolean>().apply { addAll(initialChecked) } }
                                            // Save checklist state on change
                                            fun persistChecklist() {
                                                val json = org.json.JSONArray(checkedStates)
                                                coroutineScope.launch {
                                                    viewModel.updateChecklistState(note!!.id, json.toString())
                                                }
                                            }
                                            Column {
                                                Text("Tasks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                                                tasks.forEachIndexed { idx, task ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                                        Checkbox(
                                                            checked = checkedStates[idx],
                                                            onCheckedChange = { checked ->
                                                                checkedStates[idx] = checked
                                                                persistChecklist()
                                                            },
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = Color(0xFF4CAF50),
                                                                uncheckedColor = Color(0xFFB0B0B0),
                                                                checkmarkColor = Color.White
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(task, color = Color(0xFFB0B0B0), fontSize = 16.sp)
                                                    }
                                                }
                                            }
                                        } else if (summaryOnly != null) {
                                            Text(summaryOnly, color = Color(0xFFB0B0B0), fontSize = 16.sp)
                                        } else {
                                            Text(summaryText, color = Color(0xFFB0B0B0), fontSize = 16.sp)
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
                                                                    color = Color.Gray,
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
                                    val transcriptText = if (transcript.isNotBlank()) transcript else note?.transcript.orEmpty()
                                    if (transcriptText.isNotBlank()) {
                                        Text(
                                            text = transcriptText,
                                            color = Color(0xFFB0B0B0),
                                            fontSize = 16.sp
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(120.dp))
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
                                            color = Color.Gray,
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
