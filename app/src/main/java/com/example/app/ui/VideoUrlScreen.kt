package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoUrlScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var videoUrl by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Video Summary", color = Color.Black, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = if (isProcessing) Arrangement.Center else Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isProcessing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(processingStatus, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                // URL Input Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.VideoLibrary,
                            contentDescription = "Video",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Video URL",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Enter a YouTube or video URL to generate a summary",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { 
                                videoUrl = it
                                isValidUrl = true // Reset validation state
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Paste video URL here...") },
                            isError = !isValidUrl,
                            supportingText = if (!isValidUrl) {
                                { Text("Please enter a valid video URL", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (videoUrl.isBlank()) {
                                    isValidUrl = false
                                    return@Button
                                }
                                
                                coroutineScope.launch {
                                    isProcessing = true
                                    processingStatus = "Analyzing video content..."
                                    try {
                                        viewModel.processVideoUrl(videoUrl)
                                        processingStatus = "Video summary created successfully!"
                                        kotlinx.coroutines.delay(1000)
                                        onNavigateBack()
                                    } catch (e: Exception) {
                                        processingStatus = "Error processing video: ${e.message}"
                                        isProcessing = false
                                        kotlinx.coroutines.delay(3000)
                                        isProcessing = false
                                        processingStatus = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = videoUrl.isNotBlank() && isValidUrl,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Generate Summary", color = Color.White)
                        }
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "ℹ️ Supported Platforms",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• YouTube videos\n• Vimeo links\n• Direct video URLs\n• Most video sharing platforms",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The system will extract video metadata and generate a comprehensive summary.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}
