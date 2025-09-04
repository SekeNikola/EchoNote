package com.example.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun DocumentUploadScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedDocumentUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("") }
    
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            selectedDocumentUri = it
            // Extract filename from URI
            selectedFileName = uri.path?.substringAfterLast("/") ?: "Selected Document"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Upload Files", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF222222))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedDocumentUri == null) {
                // Upload Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = "Document",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Select Document",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Choose a PDF or Word document to extract content and create summary with tasks",
                            color = Color(0xFFB0B0B0),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { documentPickerLauncher.launch("application/pdf") },
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PDF File", color = Color.White)
                            }
                            Button(
                                onClick = { documentPickerLauncher.launch("application/vnd.openxmlformats-officedocument.wordprocessingml.document") },
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Word Doc", color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { documentPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0B0B0))
                        ) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse All Files", color = Color.White)
                        }
                    }
                }
            } else {
                // Selected file section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.InsertDriveFile,
                            contentDescription = "File",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Document Selected",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            selectedFileName,
                            fontSize = 14.sp,
                            color = Color(0xFFB0B0B0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(processingStatus, color = Color(0xFFB0B0B0), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isProcessing = true
                                            processingStatus = "Extracting text from document..."
                                            try {
                                                viewModel.processDocument(selectedDocumentUri!!, context)
                                                processingStatus = "Document processed successfully!"
                                                kotlinx.coroutines.delay(1000)
                                                onNavigateBack()
                                            } catch (e: Exception) {
                                                processingStatus = "Error processing document: ${e.message}"
                                                isProcessing = false
                                                kotlinx.coroutines.delay(3000)
                                                isProcessing = false
                                                processingStatus = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Process Document", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { 
                                        selectedDocumentUri = null
                                        selectedFileName = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Choose Different File", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            
            if (selectedDocumentUri == null) {
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
                            "ℹ️ Supported Formats",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• PDF documents (.pdf)\n• Microsoft Word (.docx)\n• Plain text files (.txt)\n• Rich text format (.rtf)",
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The system will extract text content and generate summaries with actionable tasks.",
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}



