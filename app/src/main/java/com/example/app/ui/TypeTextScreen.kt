package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
fun TypeTextScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var titleText by remember { mutableStateOf("") }
    var bodyText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Create Text Note", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (titleText.isNotBlank() || bodyText.isNotBlank()) {
                            coroutineScope.launch {
                                isProcessing = true
                                processingStatus = "Processing text note..."
                                try {
                                    val fullText = if (titleText.isNotBlank() && bodyText.isNotBlank()) {
                                        "Title: $titleText\n\nContent: $bodyText"
                                    } else if (titleText.isNotBlank()) {
                                        titleText
                                    } else {
                                        bodyText
                                    }
                                    viewModel.processTextNote(fullText)
                                    processingStatus = "Note saved successfully!"
                                    kotlinx.coroutines.delay(1000)
                                    onNavigateBack()
                                } catch (e: Exception) {
                                    processingStatus = "Error saving note: ${e.message}"
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = !isProcessing && (titleText.isNotBlank() || bodyText.isNotBlank())
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = if (titleText.isNotBlank() || bodyText.isNotBlank()) Color(0xFF4CAF50) else Color(0xFFB0B0B0))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF222222))
        )

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(processingStatus, color = Color(0xFFB0B0B0))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Title Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Title (Optional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB0B0B0),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter a title for your note...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                // Body Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Content",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB0B0B0),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = bodyText,
                            onValueChange = { bodyText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            placeholder = { Text("Start typing your note content here...") },
                            maxLines = Int.MAX_VALUE,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            "ℹ️ Note Processing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your text will be analyzed to extract summaries and tasks automatically.",
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }
            }
        }
    }
}



