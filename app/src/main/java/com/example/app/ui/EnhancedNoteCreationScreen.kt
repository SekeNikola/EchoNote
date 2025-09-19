package com.example.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.data.Task
import com.example.app.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNoteCreationScreen(
    initialNoteText: String = "",
    onNavigateBack: () -> Unit,
    taskViewModel: TaskViewModel = viewModel()
) {
    var noteText by remember { mutableStateOf(initialNoteText) }
    var checkboxItems by remember { mutableStateOf<List<CheckboxItem>>(emptyList()) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showTaskCreationDialog by remember { mutableStateOf(false) }
    var tasksToCreate by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Create Note") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        scope.launch {
                            // Save note logic here
                            showSaveConfirmation = true
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        )

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Rich text editor
            RichTextEditor(
                initialText = noteText,
                onTextChange = { text, checkboxes ->
                    noteText = text
                    checkboxItems = checkboxes
                },
                modifier = Modifier.fillMaxSize()
            )

            // Voice assistant button positioned at bottom right
            VoiceAssistantButton(
                onVoiceCommand = { command ->
                    when (command.type) {
                        VoiceCommandType.CREATE_TASK -> {
                            if (command.content.isNotEmpty()) {
                                scope.launch {
                                    taskViewModel.createTask(
                                        title = command.content,
                                        description = "Created via voice command",
                                        priority = "Medium",
                                        dueDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                                    )
                                }
                            }
                        }
                        
                        VoiceCommandType.CREATE_NOTE -> {
                            if (command.content.isNotEmpty()) {
                                noteText = if (noteText.isEmpty()) {
                                    command.content
                                } else {
                                    "$noteText\n\n${command.content}"
                                }
                            }
                        }
                        
                        VoiceCommandType.SET_REMINDER -> {
                            if (command.content.isNotEmpty()) {
                                scope.launch {
                                    // Parse time from reminder (simple parsing for "in X minutes")
                                    val reminderText = command.content.lowercase()
                                    android.util.Log.d("VoiceReminder", "Processing reminder: '$reminderText'")
                                    
                                    val reminderMinutes = when {
                                        reminderText.contains("1 minute") || reminderText.contains("one minute") -> 1L
                                        reminderText.contains("5 minutes") || reminderText.contains("five minutes") -> 5L
                                        reminderText.contains("10 minutes") || reminderText.contains("ten minutes") -> 10L
                                        reminderText.contains("30 minutes") || reminderText.contains("thirty minutes") -> 30L
                                        reminderText.contains("1 hour") || reminderText.contains("one hour") -> 60L
                                        else -> 60L // Default to 1 hour
                                    }
                                    
                                    android.util.Log.d("VoiceReminder", "Parsed reminder minutes: $reminderMinutes for text: '$reminderText'")
                                    
                                    taskViewModel.createTaskWithReminder(
                                        title = "Reminder: ${command.content}",
                                        description = "Voice reminder",
                                        priority = "High",
                                        dueDate = System.currentTimeMillis() + (reminderMinutes * 60 * 1000),
                                        reminderMinutes = reminderMinutes
                                    )
                                }
                            }
                        }
                        
                        else -> {
                            // Handle other commands
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text("Note Saved") },
            text = { 
                Column {
                    Text("Your note has been saved successfully!")
                    
                    val checkedTasks = checkboxItems.filter { it.isChecked && it.text.isNotBlank() }
                    if (checkedTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Would you like to create tasks from the checked items?")
                        checkedTasks.forEach { task ->
                            Text("• ${task.text}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                val checkedTasks = checkboxItems.filter { it.isChecked && it.text.isNotBlank() }
                if (checkedTasks.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            tasksToCreate = checkedTasks.map { it.text }
                            showTaskCreationDialog = true
                            showSaveConfirmation = false
                        }
                    ) {
                        Text("Create Tasks")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSaveConfirmation = false
                        onNavigateBack()
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Task creation confirmation dialog
    if (showTaskCreationDialog) {
        TaskCreationDialog(
            tasks = tasksToCreate,
            onConfirm = { selectedTasks, priority, dueDate ->
                scope.launch {
                    selectedTasks.forEach { taskTitle ->
                        taskViewModel.createTask(
                            title = taskTitle,
                            description = "Created from note checkbox",
                            priority = priority,
                            dueDate = dueDate
                        )
                    }
                    showTaskCreationDialog = false
                    onNavigateBack()
                }
            },
            onDismiss = { 
                showTaskCreationDialog = false 
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun TaskCreationDialog(
    tasks: List<String>,
    onConfirm: (List<String>, String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTasks by remember { mutableStateOf(tasks.toSet()) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDueDateOption by remember { mutableStateOf("Tomorrow") }
    
    val priorities = listOf("Low", "Medium", "High", "Urgent")
    val dueDateOptions = mapOf(
        "Today" to System.currentTimeMillis(),
        "Tomorrow" to System.currentTimeMillis() + (24 * 60 * 60 * 1000),
        "This Week" to System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000),
        "Next Week" to System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Tasks") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Select tasks to create:")
                Spacer(modifier = Modifier.height(8.dp))
                
                // Task selection
                tasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = task in selectedTasks,
                            onCheckedChange = { checked ->
                                selectedTasks = if (checked) {
                                    selectedTasks + task
                                } else {
                                    selectedTasks - task
                                }
                            }
                        )
                        Text(
                            text = task,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selection
                Text("Priority:", style = MaterialTheme.typography.titleSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { priority ->
                        FilterChip(
                            selected = priority == selectedPriority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Due date selection
                Text("Due Date:", style = MaterialTheme.typography.titleSmall)
                Column {
                    dueDateOptions.keys.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = option == selectedDueDateOption,
                                onClick = { selectedDueDateOption = option }
                            )
                            Text(option)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dueDate = dueDateOptions[selectedDueDateOption] ?: System.currentTimeMillis()
                    onConfirm(selectedTasks.toList(), selectedPriority, dueDate)
                },
                enabled = selectedTasks.isNotEmpty()
            ) {
                Text("Create ${selectedTasks.size} Tasks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}