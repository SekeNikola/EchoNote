package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.viewmodel.NoteViewModel
import com.example.app.data.CheckboxItem
import com.example.app.data.CheckboxUtils
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    taskId: Long
) {
    val tasks by viewModel.allTasks.collectAsState()
    val task = tasks.find { it.id == taskId }
    val context = LocalContext.current
    
    var isEditMode by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editCheckboxItems by remember { mutableStateOf<List<CheckboxItem>>(emptyList()) }
    var editPriority by remember { mutableStateOf("Medium") }
    var editDueDate by remember { mutableStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Initialize edit fields when task is loaded
    LaunchedEffect(task) {
        task?.let {
            editTitle = it.title
            editDescription = it.description
            editCheckboxItems = if (it.checkboxItems.isNotEmpty()) {
                it.checkboxItems
            } else {
                // Parse existing description for checkboxes if checkboxItems is empty
                CheckboxUtils.parseCheckboxesFromText(it.description)
            }
            editPriority = it.priority
            editDueDate = it.dueDate
        }
    }
    
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
            .background(Color(0xFF282828))
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
                // Edit button (always visible when not in edit mode)
                if (!isEditMode) {
                    IconButton(
                        onClick = { isEditMode = true }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit task",
                            tint = Color.White
                        )
                    }
                }
                
                // 3-dot menu with additional options
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF2A2A3E))
                    ) {
                        if (!isEditMode) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Edit Task", color = Color.White)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    isEditMode = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share Task", color = Color.White)
                                }
                            },
                            onClick = {
                                showMenu = false
                                // Share functionality can be implemented here
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Task", color = Color(0xFFFF6B6B))
                                }
                            },
                            onClick = {
                                showMenu = false
                                viewModel.deleteTask(taskId)
                                navController.navigateUp()
                            }
                        )
                    }
                }
                
                // Save button (only visible in edit mode)
                if (isEditMode) {
                    IconButton(onClick = {
                        // Save changes
                        viewModel.updateTask(
                            taskId = taskId,
                            title = editTitle,
                            description = editDescription,
                            priority = editPriority,
                            dueDate = editDueDate
                        )
                        isEditMode = false
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save changes",
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF282828)
            )
        )
        
        // Task Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Priority indicator (editable if in edit mode)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Text(
                        text = "Priority:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { priority ->
                            val isSelected = editPriority == priority
                            val color = when (priority) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF404056),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { editPriority = priority }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = priority,
                                    color = if (isSelected) Color.White else Color(0xFFB0B0B0),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Task title (editable if in edit mode)
            if (isEditMode) {
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Title", color = Color(0xFFB0B0B0)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF8C00),
                        unfocusedBorderColor = Color(0xFF404056),
                        cursorColor = Color(0xFFFF8C00)
                    ),
                    textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )
            } else {
                Text(
                    text = task.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Task description and checkbox items
            if (isEditMode) {
                // Description field for additional text
                OutlinedTextField(
                    value = editDescription,
                    onValueChange = { editDescription = it },
                    label = { Text("Additional Description", color = Color(0xFFB0B0B0)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF8C00),
                        unfocusedBorderColor = Color(0xFF404056),
                        cursorColor = Color(0xFFFF8C00)
                    ),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Button to add new checkbox item
                OutlinedButton(
                    onClick = {
                        editCheckboxItems = editCheckboxItems + CheckboxItem(text = "New item")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF8C00)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Checkbox Item")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Display checkbox items
            if (editCheckboxItems.isNotEmpty() || task?.checkboxItems?.isNotEmpty() == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val itemsToShow = if (isEditMode) editCheckboxItems else task?.checkboxItems ?: emptyList()
                        
                        itemsToShow.forEachIndexed { index, checkboxItem ->
                            CheckboxItemRow(
                                item = checkboxItem,
                                isEditMode = isEditMode,
                                onCheckedChange = { isChecked ->
                                    if (isEditMode) {
                                        editCheckboxItems = CheckboxUtils.updateCheckboxState(
                                            editCheckboxItems, checkboxItem.id, isChecked
                                        )
                                    } else {
                                        // Update in real-time and save to database
                                        task?.let { currentTask ->
                                            val updatedItems = CheckboxUtils.updateCheckboxState(
                                                currentTask.checkboxItems, checkboxItem.id, isChecked
                                            )
                                            viewModel.updateTaskCheckboxItems(currentTask.id, updatedItems)
                                        }
                                    }
                                },
                                onTextChange = { newText ->
                                    if (isEditMode) {
                                        editCheckboxItems = editCheckboxItems.map { item ->
                                            if (item.id == checkboxItem.id) {
                                                item.copy(text = newText)
                                            } else {
                                                item
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    if (isEditMode) {
                                        editCheckboxItems = editCheckboxItems.filter { it.id != checkboxItem.id }
                                    }
                                }
                            )
                            
                            if (index < itemsToShow.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // Show additional description if it exists
                        if (!isEditMode && task?.description?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = task.description,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0B0),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else if (!isEditMode && task?.description?.isNotBlank() == true) {
                // Show only description if no checkbox items
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Due date (editable if in edit mode)
            if (isEditMode) {
                // Date and time selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date picker
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF404056))
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(editDueDate)))
                    }
                    
                    // Time picker
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF404056))
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(editDueDate)))
                    }
                }
            } else {
                // Due date display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFF8C00),
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status
            if (!isEditMode) {
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
                        containerColor = if (task.isCompleted) Color(0xFF404056) else Color(0xFFFF8C00)
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
    
    // Date Picker Dialog
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = editDueDate
        LaunchedEffect(showDatePicker) {
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    editDueDate = calendar.timeInMillis
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = editDueDate
        LaunchedEffect(showTimePicker) {
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    editDueDate = calendar.timeInMillis
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

fun formatFullDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun CheckboxItemRow(
    item: CheckboxItem,
    isEditMode: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFF8C00),
                uncheckedColor = Color(0xFF666666),
                checkmarkColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text content
        if (isEditMode) {
            OutlinedTextField(
                value = item.text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFF8C00),
                    unfocusedBorderColor = Color(0xFF404056),
                    cursorColor = Color(0xFFFF8C00)
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = Color(0xFFFF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // Display text with strikethrough if checked
            Text(
                text = item.text,
                fontSize = 16.sp,
                color = if (item.isChecked) Color(0xFF888888) else Color.White,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
