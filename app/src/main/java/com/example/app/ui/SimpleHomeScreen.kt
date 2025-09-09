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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import com.example.app.ui.components.SimpleVoiceOrb
import org.json.JSONObject
import androidx.compose.animation.core.*
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app.R

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
    val context = LocalContext.current
    
    // Camera and file functionality
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        try {
            if (success && capturedImageUri != null) {
                selectedImageUri = capturedImageUri.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("SimpleHomeScreen", "Camera result error", e)
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        try {
            uri?.let {
                selectedImageUri = it.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("SimpleHomeScreen", "Gallery result error", e)
        }
    }
    
    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        try {
            uri?.let {
                // Handle file selection - for now just log it
                android.util.Log.d("SimpleHomeScreen", "File selected: $it")
                // TODO: Add file processing logic here
            }
        } catch (e: Exception) {
            android.util.Log.e("SimpleHomeScreen", "File picker result error", e)
        }
    }
    
    // Function to create temporary file for camera
    fun createImageFile(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            
            // Try external files directory first, then cache directory as fallback
            val storageDir = context.getExternalFilesDir("Pictures") ?: context.cacheDir
            
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            
            return try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } catch (e: Exception) {
                android.util.Log.e("SimpleHomeScreen", "FileProvider error, using file URI", e)
                // Fallback to file URI if FileProvider fails
                Uri.fromFile(imageFile)
            }
        } catch (e: Exception) {
            android.util.Log.e("SimpleHomeScreen", "Error creating image file", e)
            null
        }
    }
    
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
        containerColor = Color(0xFF282828),
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
                                .size(width = 160.dp, height = 120.dp)
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(uriString)),
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
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
                        // Chat input area - tap to open chat screen
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    Color(0xFF404056),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ask AI Assistant...",
                                color = Color(0xFFB0B0B0),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        // Clear chat history and start new conversation
                                        viewModel.clearChatHistory()
                                        // Navigate to AI chat
                                        navController.navigate("ai_chat")
                                    }
                            )
                            
                            CompactVoiceOrb(
                                onClick = {
                                    // Navigate to voice assistant
                                    navController.navigate("ai_voice")
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
            
            // Header with search bar and settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                TextField(
                    value = "", // You can add search state here
                    onValueChange = { /* Add search functionality */ },
                    placeholder = { 
                        Text(
                            "Search notes, tasks...", 
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF6B7280)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("tasks") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6),
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tasks List
            if (tasks.any { isToday(it.dueDate) }) {
                Column {
                    tasks.filter { isToday(it.dueDate) }.forEach { task ->
                        TaskCard(
                            task = task,
                            onClick = { navController.navigate("task_detail/${task.id}") }
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddTaskSheet = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Task",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Empty state with add task option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddTaskSheet = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add Task",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("notes") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6),
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Notes List
            if (notes.isNotEmpty()) {
                Column {
                    notes.take(5).forEach { note ->
                        NoteCard(
                            note = note,
                            onClick = { navController.navigate("noteDetail/${note.id}") }
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddNote() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Note",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Note",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Empty state with add note option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddNote() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Note",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add Note",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { navController.navigate("chats") }
                ) {
                    Text(
                        "See All",
                        color = Color(0xFF8B5CF6),
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chat Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("ai_chat") }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Spacer(modifier = Modifier.width(16.dp))
                
            }
            
            Spacer(modifier = Modifier.height(120.dp)) // Extra space for bottom input
        }
    }
    
    // Add Task Bottom Sheet
    if (showAddTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTaskSheet = false },
            containerColor = Color(0xFF282828),
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1f1f1f))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Checkbox for completion
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { 
                // Note: This would need to be passed as a parameter to actually toggle completion
                // For now it's just visual
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFF6B00), // Very bright orange
                uncheckedColor = Color(0xFF4A4A4A), // Darker gray for better contrast
                checkmarkColor = Color.White // White checkmark for better visibility
            ),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Priority circle
        val priorityColor = when (task.priority.lowercase()) {
            "high" -> Color(0xFFFF4444) // Bright red for high priority
            "medium" -> Color(0xFFFF8C00) // Orange/amber for medium priority (matching checkbox)
            "low" -> Color(0xFF00C851) // Green for low priority
            else -> Color(0xFF6B7280) // Gray for unknown priority
        }
        
        Icon(
            Icons.Default.Circle,
            contentDescription = "Priority: ${task.priority}",
            tint = priorityColor,
            modifier = Modifier.size(12.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Text(
            text = formatTime(task.dueDate),
            fontSize = 12.sp,
            color = Color(0xFFB0B0B0)
        )
        }
        
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF444444))
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1f1f1f))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = formatDate(note.createdAt),
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0)
            )
        }
        
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF444444))
        )
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

fun extractSummaryFromSnippet(snippet: String): String {
    return try {
        if (snippet.startsWith("{") && snippet.contains("summary")) {
            val json = org.json.JSONObject(snippet)
            json.getString("summary")
        } else {
            snippet
        }
    } catch (e: Exception) {
        snippet
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
                    containerColor = Color(0xFF282828)
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

// Compact version of the Lottie voice orb for home screen input field
@Composable
fun CompactVoiceOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompactLottieVoiceOrb(
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun CompactLottieVoiceOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load the Lottie composition from raw resources
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.orb)
    )
    
    // Gentle pulsing animation for idle state
    val infiniteTransition = rememberInfiniteTransition(label = "compact_voice_orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier
            .size(32.dp)
            .scale(pulseScale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Lottie animation - only show if composition is loaded
        composition?.let {
            LottieAnimation(
                composition = it,
                iterations = LottieConstants.IterateForever,
                speed = 0.8f, // Slower, more gentle animation
                modifier = Modifier
                    .size(28.dp)
                    .fillMaxSize()
            )
        } ?: run {
            // Fallback: Show a simple orb if Lottie fails to load
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6), // Purple for idle
                                Color(0xFF7C3AED)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MicNone,
                    contentDescription = "Voice Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
