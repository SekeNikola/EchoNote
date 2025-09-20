package com.example.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.app.data.AppDatabase
import com.example.app.data.Note
import com.example.app.data.NoteRepository
import com.example.app.data.Task
import com.example.app.data.TaskRepository
import com.example.app.viewmodel.NoteViewModel
import com.example.app.viewmodel.TaskViewModel
import com.example.app.widget.SimpleWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WidgetCreationOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    
    companion object {
        const val TAG = "WidgetCreationOverlayService"
        const val ACTION_SHOW_TASK_CREATOR = "com.example.app.SHOW_TASK_CREATOR"
        const val ACTION_SHOW_NOTE_CREATOR = "com.example.app.SHOW_NOTE_CREATOR"
        const val ACTION_HIDE_OVERLAY = "com.example.app.HIDE_CREATION_OVERLAY"
        const val EXTRA_MODE = "mode"
        const val MODE_TASK = "task"
        const val MODE_NOTE = "note"
        
        fun showTaskCreator(context: Context) {
            if (canDrawOverlays(context)) {
                val intent = Intent(context, WidgetCreationOverlayService::class.java).apply {
                    action = ACTION_SHOW_TASK_CREATOR
                    putExtra(EXTRA_MODE, MODE_TASK)
                }
                context.startService(intent)
            } else {
                Log.w(TAG, "Cannot draw overlays - permission not granted")
            }
        }
        
        fun showNoteCreator(context: Context) {
            if (canDrawOverlays(context)) {
                val intent = Intent(context, WidgetCreationOverlayService::class.java).apply {
                    action = ACTION_SHOW_NOTE_CREATOR
                    putExtra(EXTRA_MODE, MODE_NOTE)
                }
                context.startService(intent)
            } else {
                Log.w(TAG, "Cannot draw overlays - permission not granted")
            }
        }
        
        fun hideOverlay(context: Context) {
            val intent = Intent(context, WidgetCreationOverlayService::class.java).apply {
                action = ACTION_HIDE_OVERLAY
            }
            context.startService(intent)
        }
        
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }
    
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var hideJob: Job? = null
    private var taskRepository: TaskRepository? = null
    private var noteRepository: NoteRepository? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
        
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        
        // Initialize repositories
        val database = AppDatabase.getDatabase(this)
        taskRepository = TaskRepository(database.taskDao(), this)
        noteRepository = NoteRepository(
            database.noteDao(),
            database.taskDao(),
            database.chatMessageDao(),
            database.reminderDao()
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WidgetCreationOverlayService started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_SHOW_TASK_CREATOR -> showCreatorOverlay(MODE_TASK)
            ACTION_SHOW_NOTE_CREATOR -> showCreatorOverlay(MODE_NOTE)
            ACTION_HIDE_OVERLAY -> hideCreatorOverlay()
        }
        
        return START_NOT_STICKY
    }
    
    private fun showCreatorOverlay(mode: String) {
        if (!canDrawOverlays(this)) {
            Log.w(TAG, "Cannot show overlay - permission not granted")
            return
        }
        
        // Clean up existing overlay if any (without stopping service)
        cleanupOverlay()
        
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // Create the overlay view using Compose
            overlayView = ComposeView(this).apply {
                // Set up lifecycle owner and saved state registry for system overlay
                setViewTreeLifecycleOwner(this@WidgetCreationOverlayService)
                setViewTreeSavedStateRegistryOwner(this@WidgetCreationOverlayService)
                
                setContent {
                    CreationOverlayContent(
                        mode = mode,
                        onDismiss = { hideCreatorOverlay() },
                        onTaskCreated = { title, description, priority, dueDate ->
                            createTask(title, description, priority, dueDate)
                            hideCreatorOverlay()
                        },
                        onNoteCreated = { title, content, imageUri ->
                            createNote(title, content, imageUri)
                            hideCreatorOverlay()
                        }
                    )
                }
            }
            
            // Set up window parameters for full-screen overlay
            val layoutParams = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.CENTER
                x = 0
                y = 0
            }
            
            // Add the view to window manager
            windowManager?.addView(overlayView, layoutParams)
            
            Log.d(TAG, "Creation overlay shown for mode: $mode")
            
            // Auto-hide after 30 seconds to prevent system issues
            hideJob = CoroutineScope(Dispatchers.Main).launch {
                delay(30000)
                hideCreatorOverlay()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing creation overlay", e)
            stopSelf()
        }
    }
    
    private fun hideCreatorOverlay() {
        cleanupOverlay()
        stopSelf()
    }
    
    private fun cleanupOverlay() {
        try {
            hideJob?.cancel()
            
            overlayView?.let { view ->
                windowManager?.removeView(view)
                overlayView = null
            }
            
            windowManager = null
            Log.d(TAG, "Creation overlay hidden")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up creation overlay", e)
        }
    }
    
    private fun createTask(title: String, description: String, priority: String, dueDate: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                taskRepository?.insertTask(task)
                Log.d(TAG, "Task created: $title")
                
                // Update all widgets to reflect new task
                SimpleWidgetProvider.updateAllWidgets(this@WidgetCreationOverlayService)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating task", e)
            }
        }
    }
    
    private fun createNote(title: String, content: String, imageUri: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val note = Note(
                    title = title,
                    transcript = content,
                    snippet = content,
                    imagePath = imageUri,
                    createdAt = System.currentTimeMillis()
                )
                noteRepository?.insertNote(note)
                Log.d(TAG, "Note created: $title")
                
                // Update all widgets to reflect new note
                SimpleWidgetProvider.updateAllWidgets(this@WidgetCreationOverlayService)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating note", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        cleanupOverlay()
    }
}

@Composable
fun CreationOverlayContent(
    mode: String,
    onDismiss: () -> Unit,
    onTaskCreated: (String, String, String, Long) -> Unit,
    onNoteCreated: (String, String, String?) -> Unit
) {
    // Semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }, // Tap outside to dismiss
        contentAlignment = Alignment.BottomCenter
    ) {
        // Animate slide up
        var visible by remember { mutableStateOf(false) }
        val animatedOffset by animateFloatAsState(
            targetValue = if (visible) 0f else 1f,
            animationSpec = tween(300),
            label = "slide_animation"
        )
        
        LaunchedEffect(Unit) {
            visible = true
        }
        
        // Bottom sheet content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset(y = (animatedOffset * 300).dp)
                .clickable { }, // Prevent dismiss when clicking inside
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (mode == WidgetCreationOverlayService.MODE_TASK) "Add New Task" else "Add New Note",
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on mode
                if (mode == WidgetCreationOverlayService.MODE_TASK) {
                    OverlayTaskCreator(
                        onCreateTask = onTaskCreated,
                        onDismiss = onDismiss
                    )
                } else {
                    OverlayNoteCreator(
                        onCreateNote = onNoteCreated,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayTaskCreator(
    onCreateTask: (String, String, String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val priorities = listOf("Low", "Medium", "High", "Urgent")
    
    Column {
        // Title input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Add Your Title", color = Color(0xFFB0B0B0)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF8C00),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF8C00)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Task description...", color = Color(0xFFB0B0B0)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF8C00),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF8C00)
            ),
            minLines = 2,
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority selector
        var showPriorityDropdown by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = priority,
                onValueChange = { },
                readOnly = true,
                placeholder = { Text("Priority", color = Color(0xFFB0B0B0)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPriorityDropdown = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF8C00),
                    unfocusedBorderColor = Color(0xFF4A4A5E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFF8C00)
                ),
                trailingIcon = {
                    IconButton(onClick = { showPriorityDropdown = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select priority",
                            tint = Color.White
                        )
                    }
                }
            )
            
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                containerColor = Color(0xFFFF8C00)
            ),
            enabled = title.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Create Task",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayNoteCreator(
    onCreateNote: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    Column {
        // Title input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Note title...", color = Color(0xFFB0B0B0)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF8C00),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF8C00)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content input
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("Start writing your note...", color = Color(0xFFB0B0B0)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF8C00),
                unfocusedBorderColor = Color(0xFF4A4A5E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFF8C00)
            ),
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save button
        Button(
            onClick = {
                if (title.isNotBlank() || content.isNotBlank()) {
                    val noteTitle = if (title.isNotBlank()) title else "Untitled Note"
                    val noteContent = if (content.isNotBlank()) content else title
                    onCreateNote(noteTitle.trim(), noteContent.trim(), null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF8C00)
            ),
            enabled = title.isNotBlank() || content.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Create Note",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}