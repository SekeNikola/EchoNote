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
import androidx.navigation.NavController
import android.net.Uri
import coil.compose.rememberAsyncImagePainter
import com.example.app.data.Note
import com.example.app.data.Task
import com.example.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

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
                        
                        // Chat input area that navigates to AI chat
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    Color(0xFF404056),
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable { 
                                    // Pass the selected image to chat if any
                                    selectedImageUri?.let {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedImageUri", it)
                                    }
                                    navController.navigate("ai_chat")
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ask AI Assistant...",
                                color = Color(0xFFB0B0B0),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(24.dp)
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
            
            // Header
            Text(
                text = "Good ${getGreeting()}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
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
                            onClick = { navController.navigate("tasks") }
                        )
                    }
                }
            } else {
                // Empty state with add task option
                AddTaskCard(
                    onClick = { navController.navigate("tasks") }
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
