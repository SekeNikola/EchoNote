package com.example.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.data.Task
import com.example.app.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    onNavigateBack: () -> Unit,
    onTaskClick: (Task) -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    val allTasks by taskViewModel.allTasks.observeAsState(emptyList())
    val showCreateDialog by taskViewModel.showCreateTaskDialog.collectAsState()
    
    // Group tasks by date sections
    val taskGroups = remember(allTasks) {
        groupTasksByDate(allTasks.filter { !it.isCompleted })
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "All Tasks",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { taskViewModel.showCreateTaskDialog() }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Task",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tasks organized by sections
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            taskGroups.forEach { (sectionTitle, tasks) ->
                if (tasks.isNotEmpty()) {
                    // Section header
                    item {
                        TaskSectionHeader(
                            title = sectionTitle,
                            taskCount = tasks.size,
                            isOverdue = sectionTitle == "Overdue"
                        )
                    }
                    
                    // Tasks in this section
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onToggleComplete = { taskViewModel.toggleTaskComplete(task.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
            
            // Empty state
            if (taskGroups.values.all { it.isEmpty() }) {
                item {
                    EmptyTasksState(
                        onCreateTask = { taskViewModel.showCreateTaskDialog() }
                    )
                }
            }
        }
    }
    
    // Create Task Bottom Sheet
    if (showCreateDialog) {
        ModalBottomSheet(
            onDismissRequest = { taskViewModel.hideCreateTaskDialog() }
        ) {
            AddTaskBottomSheet(
                onCreateTask = { title, description, priority, dueDate ->
                    taskViewModel.createTask(title, description, priority, dueDate)
                },
                onDismiss = { taskViewModel.hideCreateTaskDialog() }
            )
        }
    }
}

@Composable
private fun TaskSectionHeader(
    title: String,
    taskCount: Int,
    isOverdue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isOverdue) Color(0xFFFF5252) else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isOverdue) Color(0xFFFF5252) else Color(0xFF4CAF50)
        ) {
            Text(
                text = taskCount.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        "Urgent" -> Color(0xFFE91E63)
        "High" -> Color(0xFFFF5722)
        "Medium" -> Color(0xFFFF9800)
        "Low" -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }
    
    val isOverdue = task.dueDate < System.currentTimeMillis() && !task.isCompleted
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color(0xFF2D1B1B) else Color(0xFF1F1F1F)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50),
                        uncheckedColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Task content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) Color.Gray else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Due date and priority
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority indicator
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = task.priority,
                                color = priorityColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Due date
                        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date(task.dueDate)),
                            color = if (isOverdue) Color(0xFFFF5252) else Color.Gray,
                            fontSize = 12.sp
                        )
                        
                        if (isOverdue) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Overdue",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState(
    onCreateTask: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tasks yet",
            color = Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first task to get organized",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateTask,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Task")
        }
    }
}

private fun groupTasksByDate(tasks: List<Task>): Map<String, List<Task>> {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    
    // Calculate date boundaries
    calendar.timeInMillis = now
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfToday = calendar.timeInMillis
    
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfTomorrow = calendar.timeInMillis
    
    val grouped = mutableMapOf<String, MutableList<Task>>()
    
    for (task in tasks.sortedWith(compareBy<Task> { task ->
        when (task.priority) {
            "Urgent" -> 0
            "High" -> 1
            "Medium" -> 2
            "Low" -> 3
            else -> 4
        }
    }.thenBy { it.dueDate })) {
        val section = when {
            task.dueDate < startOfToday -> "Overdue"
            task.dueDate in startOfToday..endOfToday -> "Today"
            task.dueDate in (endOfToday + 1)..endOfTomorrow -> "Tomorrow"
            else -> "Later"
        }
        
        grouped.getOrPut(section) { mutableListOf() }.add(task)
    }
    
    // Return in specific order
    val orderedSections = listOf("Overdue", "Today", "Tomorrow", "Later")
    return orderedSections.associateWith { grouped[it] ?: emptyList() }
}