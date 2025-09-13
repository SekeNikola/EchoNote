package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.data.Note
import com.example.app.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val tasks by viewModel.allTasks.collectAsState()
    var showAddTaskSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "All Tasks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showAddTaskSheet = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add task",
                        tint = Color(0xFFFF8C00)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF282828)
            )
        )
        
        // Tasks List
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Task,
                        contentDescription = null,
                        tint = Color(0xFFFF8C00),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tasks yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ask AI to create your first task",
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { navController.navigate("task_detail/${task.id}") },
                        onCompleteToggle = { taskId: Long -> viewModel.toggleTaskComplete(taskId) }
                    )
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val notes by viewModel.notes.observeAsState(emptyList())
    var showAddNoteSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "All Notes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showAddNoteSheet = true }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add note",
                        tint = Color(0xFFFF8C00)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF282828)
            )
        )
        
        // Notes List
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Note,
                        contentDescription = null,
                        tint = Color(0xFFFF8C00),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notes yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ask AI to create your first note",
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("noteDetail/${note.id}") },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1f1f1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(note.createdAt)),
                                    fontSize = 12.sp,
                                    color = Color(0xFF666680),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = note.transcript.take(100),
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0B0),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add Note Bottom Sheet
    if (showAddNoteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddNoteSheet = false },
            containerColor = Color(0xFF282828),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            AddNoteBottomSheet(
                onCreateNote = { title, content ->
                    viewModel.createNote(title, content)
                    showAddNoteSheet = false
                },
                onDismiss = { showAddNoteSheet = false }
            )
        }
    }
}
