package com.example.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import android.net.Uri
import coil.compose.rememberAsyncImagePainter
import com.example.app.data.Note
import com.example.app.data.Task
import com.example.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    onAddNote: () -> Unit = {}
) {
    val notes by viewModel.notes.observeAsState(emptyList())
    val tasks by viewModel.allTasks.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var aiInputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Handle incoming image from navigation
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(Unit) {
        savedStateHandle?.get<String>("selectedImageUri")?.let { uriString ->
            if (uriString.isNotEmpty()) {
                selectedImageUri = uriString
                // Clear the saved state to prevent re-showing on navigation
                savedStateHandle.set("selectedImageUri", "")
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF1A1A2E),
        bottomBar = {
            // Fixed AI Assistant Input at bottom
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Image preview (if image is selected)
                    selectedImageUri?.let { uriString ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(uriString)),
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Close button for image
                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Plus icon with dropdown menu for media options
                        var showDropDown by remember { mutableStateOf(false) }
                        
                        Box {
                            IconButton(
                                onClick = { showDropDown = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add media",
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showDropDown,
                                onDismissRequest = { showDropDown = false },
                                modifier = Modifier
                                    .background(
                                        Color(0xFF1A1A2E),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                // Options in a row
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Camera option
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                showDropDown = false
                                                navController.navigate("image_preview?source=camera")
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "Camera",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Camera",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    // Gallery option
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                showDropDown = false
                                                navController.navigate("image_preview?source=gallery")
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = "Photos",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Photos",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    // Files option
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                showDropDown = false
                                                // TODO: Handle file upload
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Files",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Files",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Chat input area with immediate typing capability
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    Color(0xFF404056),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = aiInputText,
                                onValueChange = { aiInputText = it },
                                placeholder = { 
                                    Text(
                                        "Ask AI Assistant...",
                                        color = Color(0xFFB0B0B0),
                                        fontSize = 16.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF8B5CF6)
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    Row {
                                        if (aiInputText.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    if (aiInputText.isNotBlank()) {
                                                        // Navigate to AI chat with the typed text
                                                        navController.currentBackStackEntry?.savedStateHandle?.set("initialMessage", aiInputText)
                                                        selectedImageUri?.let {
                                                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedImageUri", it)
                                                        }
                                                        navController.navigate("ai_chat")
                                                        aiInputText = ""
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Send,
                                                    contentDescription = "Send message",
                                                    tint = Color(0xFF8B5CF6),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                // Handle voice input - could trigger voice recording
                                                selectedImageUri?.let {
                                                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedImageUri", it)
                                                }
                                                navController.navigate("ai_chat")
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Mic,
                                                contentDescription = "Voice input",
                                                tint = Color(0xFF8B5CF6),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Header with greeting and settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Good ${getGreeting()}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = { navController.navigate("settings") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Today's Tasks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("tasks") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tasks List
            if (tasks.any { isToday(it.dueDate) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(tasks.filter { isToday(it.dueDate) }) { task ->
                        TaskCard(
                            task = task,
                            onClick = { navController.navigate("task_detail/${task.id}") }
                        )
                    }
                    
                    item {
                        AddTaskCard(
                            onClick = { showAddTaskSheet = true }
                        )
                    }
                }
            } else {
                // Empty state with add task option
                AddTaskCard(
                    onClick = { showAddTaskSheet = true }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Recent Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Notes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("notes") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Notes List
            if (notes.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(notes.take(5)) { note ->
                        NoteCard(
                            note = note,
                            onClick = { navController.navigate("noteDetail/${note.id}") }
                        )
                    }
                    
                    item {
                        AddNoteCard(
                            onClick = { onAddNote() }
                        )
                    }
                }
            } else {
                // Empty state with add note option
                AddNoteCard(
                    onClick = { onAddNote() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Chat Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("chats") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chat Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("ai_chat") },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFF3B82F6)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Assistant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start new chat",
                            fontSize = 14.sp,
                            color = Color(0xFFB0B0B0),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp)) // Extra space for bottom input
        }
    }
    
    // Add Task Bottom Sheet
    if (showAddTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTaskSheet = false },
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            AddTaskBottomSheet(
                onCreateTask = { title, description, priority, dueDate ->
                    viewModel.createTask(title, description, priority, dueDate)
                    showAddTaskSheet = false
                },
                onDismiss = { showAddTaskSheet = false }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Circle,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatTime(task.dueDate),
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0)
            )
        }
    }
}

@Composable
fun AddTaskCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Task",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Task",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Note",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = note.snippet,
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatDate(note.createdAt),
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0)
            )
        }
    }
}

@Composable
fun AddNoteCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Note",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Note",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper functions
fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..17 -> "Afternoon"
        else -> "Evening"
    }
}

fun isToday(date: Long): Boolean {
    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { timeInMillis = date }
    
    return today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
}

fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    onCreateTask: (String, String, String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    // Set default date to start of today to ensure it shows up in "Today's Tasks"
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var selectedDate by remember { mutableStateOf<Long>(todayStart) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val priorities = listOf("Low", "Medium", "High")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add New Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Add Your Title", color = Color(0xFFB0B0B0)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF8B5CF6)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Write here Description", color = Color(0xFFB0B0B0)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF8B5CF6)
            ),
            minLines = 3,
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Row with time, date, and priority
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time picker
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showTimePicker = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatTime(selectedDate),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Date picker
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatDateShort(selectedDate),
                        color = Color(0xFF8B5CF6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Priority dropdown
            var showPriorityDropdown by remember { mutableStateOf(false) }
            
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPriorityDropdown = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = priority,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showPriorityDropdown,
                    onDismissRequest = { showPriorityDropdown = false },
                    modifier = Modifier.background(Color(0xFF2A2A3E))
                ) {
                    priorities.forEach { priorityOption ->
                        DropdownMenuItem(
                            text = { Text(priorityOption, color = Color.White) },
                            onClick = {
                                priority = priorityOption
                                showPriorityDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save button
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onCreateTask(title.trim(), description.trim(), priority, selectedDate)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6)
            ),
            enabled = title.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Save",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFF8B5CF6))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFFB0B0B0))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1A1A2E)
                )
            )
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        
        LaunchedEffect(showTimePicker) {
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    selectedDate = calendar.timeInMillis
                    showTimePicker = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }
    }
}
