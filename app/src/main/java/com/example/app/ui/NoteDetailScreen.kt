package com.example.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.observeAsState(emptyList())
    val note = remember(notes, noteId) {
        notes.find { it.id.toString() == noteId }
    }

    // Edit mode states
    var isEditMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var editableTitle by remember(note?.title) { mutableStateOf(note?.title ?: "") }
    var editableContent by remember(note?.snippet) { mutableStateOf(note?.snippet ?: "") }
    var editableText by remember(note?.snippet) {
        mutableStateOf(
            TextFieldValue(
                text = try {
                    val json = JSONObject(note?.snippet ?: "")
                    json.optString("text", "")
                } catch (e: Exception) {
                    note?.snippet ?: ""
                }
            )
        )
    }

    // Update editable states when note changes
    LaunchedEffect(note?.title) {
        editableTitle = note?.title ?: ""
    }

    LaunchedEffect(note?.snippet) {
        editableContent = note?.snippet ?: ""
        editableText = TextFieldValue(
            text = try {
                val json = JSONObject(note?.snippet ?: "")
                json.optString("text", "")
            } catch (e: Exception) {
                note?.snippet ?: ""
            }
        )
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
                        onValueChange = { newTitle: String -> editableTitle = newTitle },
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
                            // Update title
                            viewModel.updateNoteTitle(noteObj.id, editableTitle)
                            
                            // Update content - use the properly structured JSON
                            viewModel.updateNoteSnippet(noteObj.id, editableContent)
                            
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
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showMenu = false
                                // Share action
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note?.isFavorite == true) "Remove from favorites" else "Add to favorites") },
                            onClick = {
                                showMenu = false
                                note?.let { noteObj: com.example.app.data.Note -> viewModel.toggleFavorite(noteObj) }
                            },
                            leadingIcon = {
                                Icon(
                                    if (note?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color.Unspecified
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                note?.let { noteObj: com.example.app.data.Note -> viewModel.deleteNote(noteObj.id); onBack() }
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFFF5252))
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

        // Scrollable content area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Image display if available
            note?.imagePath?.let { imagePath ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF383838))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imagePath)
                            .build(),
                        contentDescription = "Note image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Content area - simple editor without RichTextEditor
            note?.let { noteObj: com.example.app.data.Note ->
                if (isEditMode) {
                    SimpleNoteEditor(
                        content = noteObj.snippet,
                        onContentChange = { newContent ->
                            editableContent = newContent
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    // Simple content display with interactive checkboxes
                    NoteContentDisplay(
                        content = noteObj.snippet,
                        onCheckboxChange = { index, newCheckedState ->
                            // Parse current content to update specific checkbox
                            val contentData = parseNoteContent(noteObj.snippet)
                            val updatedCheckboxes = contentData.checkboxes.toMutableList()
                            if (index < updatedCheckboxes.size) {
                                updatedCheckboxes[index] = updatedCheckboxes[index].copy(isChecked = newCheckedState)
                                
                                // Create updated JSON
                                val updatedJson = JSONObject().apply {
                                    put("text", contentData.text)
                                    put("checkboxes", org.json.JSONArray().apply {
                                        updatedCheckboxes.forEach { item ->
                                            put(JSONObject().apply {
                                                put("text", item.text)
                                                put("checked", item.isChecked)
                                            })
                                        }
                                    })
                                }
                                
                                // Save the updated JSON
                                viewModel.updateNoteSnippet(noteObj.id, updatedJson.toString())
                            }
                        }
                    )
                }
            }
        }
    }
}

data class CheckboxDisplayItem(
    val text: String,
    val isChecked: Boolean
)

data class NoteContentData(
    val text: String,
    val checkboxes: List<CheckboxDisplayItem>,
    val isStructuredJson: Boolean
)

private fun parseNoteContent(snippet: String): NoteContentData {
    // 1. Try JSON parsing
    try {
        val json = JSONObject(snippet)
        val textContent = json.optString("text", "")
        val checkboxesArray = json.optJSONArray("checkboxes")
        val checkboxItems = mutableListOf<CheckboxDisplayItem>()

        if (checkboxesArray != null) {
            for (i in 0 until checkboxesArray.length()) {
                val checkboxJson = checkboxesArray.getJSONObject(i)
                checkboxItems.add(
                    CheckboxDisplayItem(
                        text = checkboxJson.optString("text", ""),
                        isChecked = checkboxJson.optBoolean("checked", false)
                    )
                )
            }
        }

        return NoteContentData(textContent, checkboxItems, true)
    } catch (_: Exception) {
        // Not JSON → fall through
    }

    // 2. Try HTML parsing (look for <input type="checkbox">)
    return try {
        val regex = Regex(
            """<div class="checkbox-item[^>]*>.*?<input[^>]*type=["']checkbox["'][^>]*(checked)?[^>]*>.*?<span[^>]*>(.*?)</span>.*?</div>""",
            RegexOption.DOT_MATCHES_ALL
        )

        val checkboxItems = mutableListOf<CheckboxDisplayItem>()
        regex.findAll(snippet).forEach { match ->
            val isChecked = match.groups[1] != null
            val text = match.groups[2]?.value
                ?.replace(Regex("<.*?>"), "") // remove any HTML tags
                ?.trim() ?: ""
            checkboxItems.add(CheckboxDisplayItem(text, isChecked))
        }

        // Remove checkboxes from HTML and extract remaining plain text
        val cleanedText = snippet
            .replace(regex, "")
            .replace(Regex("<.*?>"), "")
            .trim()

        NoteContentData(cleanedText, checkboxItems, false)
    } catch (_: Exception) {
        // 3. Fallback → plain text only
        NoteContentData(snippet, emptyList(), false)
    }
}


@Composable
private fun NoteContentDisplay(
    content: String,
    onCheckboxChange: (index: Int, newCheckedState: Boolean) -> Unit
) {
    val contentData = remember(content) { parseNoteContent(content) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Display regular text if present
        if (contentData.text.isNotBlank()) {
            Text(
                text = contentData.text,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (contentData.checkboxes.isNotEmpty()) 16.dp else 0.dp)
            )
        }
        
        // Display checkboxes if present - following official Compose patterns
        contentData.checkboxes.forEachIndexed { index, checkboxItem ->
            var checked by remember(checkboxItem.isChecked) { mutableStateOf(checkboxItem.isChecked) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { newState ->
                        checked = newState
                        onCheckboxChange(index, newState)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50),
                        uncheckedColor = Color(0xFFB0B0B0),
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = checkboxItem.text,
                    color = if (checked) Color(0xFFB0B0B0) else Color.White,
                    fontSize = 16.sp,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Show message if no content
        if (contentData.text.isBlank() && contentData.checkboxes.isEmpty()) {
            Text(
                text = "No content",
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun SimpleNoteEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentData = remember(content) { parseNoteContent(content) }
    var currentText by remember(contentData.text) { mutableStateOf(contentData.text) }
    var currentCheckboxes by remember { mutableStateOf(contentData.checkboxes.toMutableList()) }
    
    // Update parent when content changes
    LaunchedEffect(currentText, currentCheckboxes.size, currentCheckboxes.map { "${it.text}-${it.isChecked}" }) {
        val jsonObject = JSONObject().apply {
            put("text", currentText)
            put("checkboxes", org.json.JSONArray().apply {
                currentCheckboxes.forEach { checkbox ->
                    put(JSONObject().apply {
                        put("text", checkbox.text)
                        put("checked", checkbox.isChecked)
                    })
                }
            })
        }
        onContentChange(jsonObject.toString())
    }
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Text editing field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BasicTextField(
                value = currentText,
                onValueChange = { currentText = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                decorationBox = { innerTextField ->
                    if (currentText.isEmpty()) {
                        Text(
                            "Enter your note text...",
                            color = Color(0xFF808080),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add Task button
        OutlinedButton(
            onClick = {
                currentCheckboxes = currentCheckboxes.toMutableList().apply {
                    add(CheckboxDisplayItem("", false))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4CAF50)
            ),
            border = BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add task",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Task", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display and edit checkboxes
        currentCheckboxes.forEachIndexed { index, checkboxItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF383838)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkboxItem.isChecked,
                        onCheckedChange = { checked ->
                            currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                set(index, checkboxItem.copy(isChecked = checked))
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            uncheckedColor = Color(0xFFB0B0B0),
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = checkboxItem.text,
                        onValueChange = { newText ->
                            currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                set(index, checkboxItem.copy(text = newText))
                            }
                        },
                        textStyle = TextStyle(
                            color = if (checkboxItem.isChecked) Color(0xFFB0B0B0) else Color.White,
                            fontSize = 16.sp,
                            textDecoration = if (checkboxItem.isChecked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                if (checkboxItem.text.isEmpty()) {
                                    Text(
                                        "Enter task...",
                                        color = Color(0xFF808080),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    // Remove button
                    IconButton(
                        onClick = {
                            currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove task",
                            tint = Color(0xFF808080),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
