package com.example.app.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.viewmodel.NoteViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Function to create temporary file for camera
fun createImageFile(context: android.content.Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir("Pictures")
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    imageUri: String? = null,
    source: String? = null
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(imageUri == null && source == null) }
    
    // Initialize with passed imageUri if available
    LaunchedEffect(imageUri) {
        imageUri?.let {
            selectedImageUri = Uri.parse(it)
            showImageOptions = false
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            selectedImageUri = capturedImageUri
            showImageOptions = false
            // Immediately navigate back with the image
            navController.previousBackStackEntry?.savedStateHandle?.set("selectedImageUri", capturedImageUri.toString())
            navController.popBackStack()
        } else {
            navController.popBackStack()
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showImageOptions = false
            // Immediately navigate back with the image
            navController.previousBackStackEntry?.savedStateHandle?.set("selectedImageUri", uri.toString())
            navController.popBackStack()
        } else {
            navController.popBackStack()
        }
    }
    
    // Auto-launch camera or gallery based on source
    LaunchedEffect(source) {
        when (source) {
            "camera" -> {
                capturedImageUri = createImageFile(context)
                cameraLauncher.launch(capturedImageUri!!)
            }
            "gallery" -> {
                galleryLauncher.launch("image/*")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Image Preview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (selectedImageUri != null) {
                IconButton(
                    onClick = {
                        // TODO: Save image to note
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color(0xFF8B5CF6)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (showImageOptions) {
            // Show image selection options
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose an option",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Camera option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        capturedImageUri = createImageFile(context)
                        cameraLauncher.launch(capturedImageUri!!)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(40.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Column {
                            Text(
                                text = "Take Photo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Use camera to capture",
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0B0)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gallery option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "Gallery",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(40.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Column {
                            Text(
                                text = "Choose from Gallery",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Select existing photo",
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0B0)
                            )
                        }
                    }
                }
            }
        } else {
            // Show selected image
            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A3E)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Retake/Choose again button
                    OutlinedButton(
                        onClick = { showImageOptions = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Again")
                    }
                    
                    // Save button
                    Button(
                        onClick = {
                            // TODO: Save image and create note
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Note")
                    }
                }
            }
        }
    }
}
