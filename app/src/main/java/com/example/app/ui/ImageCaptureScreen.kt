package com.example.app.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ImageCaptureScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("") }
    
    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedImageUri = it }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedImageBitmap = bitmap
        } else {
            // User cancelled camera, go back
            onNavigateBack()
        }
    }

    // Auto-launch camera when screen opens (if permission granted)
    LaunchedEffect(cameraPermissionState.status) {
        when (cameraPermissionState.status) {
            PermissionStatus.Granted -> {
                cameraLauncher.launch(null)
            }
            is PermissionStatus.Denied -> {
                if (!cameraPermissionState.status.shouldShowRationale) {
                    // First time asking, request permission
                    cameraPermissionState.launchPermissionRequest()
                }
                // If permanently denied, will show UI to request permission
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Take Picture", color = Color.White, fontWeight = FontWeight.Bold) },
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
            if (selectedImageUri == null && capturedImageBitmap == null) {
                // Show different UI based on permission status
                when (cameraPermissionState.status) {
                    PermissionStatus.Granted -> {
                        // Camera permission granted, showing loading while camera opens
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
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Opening Camera...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Take a photo to analyze and create notes",
                                    color = Color(0xFFB0B0B0),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Camera", color = Color.White)
                                }
                            }
                        }
                    }
                    is PermissionStatus.Denied -> {
                        // Permission denied, show explanation and options
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
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Camera Permission Required",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Please grant camera permission to take photos",
                                    color = Color(0xFFB0B0B0),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { cameraPermissionState.launchPermissionRequest() },
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Grant Permission", color = Color.White)
                                    }
                                    OutlinedButton(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                                    ) {
                                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Use Gallery")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Image preview and processing
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
                        // Show image preview
                        capturedImageBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(16.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        selectedImageUri?.let { uri ->
                            // Could show image from URI here
                            Text("Image selected from gallery", color = Color(0xFFB0B0B0))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(processingStatus, color = Color(0xFFB0B0B0))
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isProcessing = true
                                            processingStatus = "Analyzing image content..."
                                            try {
                                                if (capturedImageBitmap != null) {
                                                    viewModel.processImageWithOCR(capturedImageBitmap!!, context)
                                                } else if (selectedImageUri != null) {
                                                    viewModel.processImageWithOCR(selectedImageUri!!, context)
                                                }
                                                onNavigateBack()
                                            } catch (e: Exception) {
                                                processingStatus = "Error processing image: ${e.message}"
                                                isProcessing = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Process", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { 
                                        selectedImageUri = null
                                        capturedImageBitmap = null
                                        if (cameraPermissionState.status.isGranted) {
                                            cameraLauncher.launch(null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retake")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



