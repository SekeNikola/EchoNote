package com.example.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.app.data.ChatMessage
import com.example.app.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import android.net.Uri
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Handle incoming image from navigation
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(Unit) {
        savedStateHandle?.get<String>("selectedImageUri")?.let { uriString ->
            selectedImageUri = Uri.parse(uriString)
            // Clear the saved state to prevent re-showing on navigation
            savedStateHandle.remove<String>("selectedImageUri")
        }
    }
    
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
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
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Logion AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Your AI assistant",
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }
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
                        // Clear current chat and start new one
                        viewModel.clearChatHistory()
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New chat",
                        tint = Color(0xFF8B5CF6)
                    )
                }
                
                IconButton(
                    onClick = { navController.navigate("ai_voice") }
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = Color(0xFF8B5CF6)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A2E)
            )
        )
        
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    WelcomeMessage()
                }
            }
            
            items(chatMessages) { message ->
                ChatMessageItem(
                    message = message,
                    isUser = message.isUser
                )
            }
            
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        // Input Area with image preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2A2A3E),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Image preview (if image is selected)
                selectedImageUri?.let { uri ->
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
                                painter = rememberAsyncImagePainter(uri),
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
                    verticalAlignment = Alignment.Bottom
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
                            .padding(8.dp)
                    ) {
                        // Options in a row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(8.dp)
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
                            
                            // Photos option
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
                                    Icons.Default.Photo,
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
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Ask me anything...",
                            color = Color(0xFFB0B0B0)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF404056),
                        cursorColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                FloatingActionButton(
                    onClick = {
                        if ((inputText.isNotBlank() || selectedImageUri != null) && !isLoading) {
                            coroutineScope.launch {
                                // Send message with optional image context
                                val messageToSend = if (selectedImageUri != null) {
                                    if (inputText.isBlank()) {
                                        "I've shared an image with you. Can you describe what you see?"
                                    } else {
                                        "$inputText [User has shared an image]"
                                    }
                                } else {
                                    inputText
                                }
                                
                                viewModel.sendChatMessage(messageToSend)
                                selectedImageUri = null
                                inputText = ""
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = if ((inputText.isNotBlank() || selectedImageUri != null) && !isLoading) {
                        Color(0xFF8B5CF6)
                    } else {
                        Color(0xFF404056)
                    }
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                }
            }
        }
    }
}

@Composable
fun WelcomeMessage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
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
                Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Hello! I'm Logion AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "I can help you create notes, tasks, and organize your thoughts. Just ask me anything!",
            fontSize = 16.sp,
            color = Color(0xFFB0B0B0),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick suggestions
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuggestionChip(
                text = "Create a task for tomorrow",
                onClick = { /* Handle suggestion */ }
            )
            SuggestionChip(
                text = "Take notes from my meeting",
                onClick = { /* Handle suggestion */ }
            )
            SuggestionChip(
                text = "Organize my thoughts about the project",
                onClick = { /* Handle suggestion */ }
            )
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF2A2A3E),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            color = if (isUser) Color(0xFF8B5CF6) else Color(0xFF2A2A3E),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.content,
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp
            )
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color(0xFF8B5CF6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
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
                Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Surface(
            color = Color(0xFF2A2A3E),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if (System.currentTimeMillis() / 500 % 3 == index.toLong()) 1f else 0.3f,
                        animationSpec = androidx.compose.animation.core.tween(500),
                        label = "typing_dot_$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color.White.copy(alpha = alpha),
                                shape = CircleShape
                            )
                    )
                    
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}
