package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    taskId: Long
) {
    val tasks by viewModel.allTasks.collectAsState()
    val task = tasks.find { it.id == taskId }
    
    if (task == null) {
        // Task not found, show error or navigate back
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Task Details",
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
                    onClick = { 
                        viewModel.toggleTaskComplete(taskId)
                    }
                ) {
                    Icon(
                        if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "Mark incomplete" else "Mark complete",
                        tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFFB0B0B0)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A2E)
            )
        )
        
        // Task Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Priority indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = when (task.priority) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            },
                            shape = CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "${task.priority} Priority",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB0B0B0)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Task title
            Text(
                text = task.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = task.description,
                    fontSize = 16.sp,
                    color = Color.White,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Due date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Due Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0)
                    )
                    Text(
                        text = formatFullDate(task.dueDate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0)
                    )
                    Text(
                        text = if (task.isCompleted) "Completed" else "In Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action button
            Button(
                onClick = {
                    viewModel.toggleTaskComplete(taskId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) Color(0xFF404056) else Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    if (task.isCompleted) Icons.Default.Refresh else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (task.isCompleted) "Mark as Incomplete" else "Mark as Complete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatFullDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
