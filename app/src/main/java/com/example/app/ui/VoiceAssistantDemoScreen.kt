package com.example.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.util.VoiceAssistantManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantDemoScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val shouldTriggerVoice by VoiceAssistantManager.shouldTriggerVoice.collectAsState()
    var showVoiceAssistant by remember { mutableStateOf(false) }
    
    // React to voice assistant trigger
    LaunchedEffect(shouldTriggerVoice) {
        if (shouldTriggerVoice) {
            showVoiceAssistant = true
            VoiceAssistantManager.clearTrigger()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Voice Assistant Demo") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Voice Assistant Integration",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Configure your side key to instantly activate voice commands",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // KeyMapper Setup Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "KeyMapper Setup Instructions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SetupStep(
                        number = "1",
                        title = "Install KeyMapper",
                        description = "Download KeyMapper from Google Play Store or F-Droid"
                    )
                    
                    SetupStep(
                        number = "2",
                        title = "Create New Mapping",
                        description = "Tap '+' button and record your side key press"
                    )
                    
                    SetupStep(
                        number = "3",
                        title = "Configure Broadcast Action",
                        description = """Select "Broadcast" and use:
Action: com.example.app.VOICE_ASSISTANT_TRIGGER
Package: com.example.app"""
                    )
                    
                    SetupStep(
                        number = "4",
                        title = "Test Activation",
                        description = "Press your side key to trigger voice assistant"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Test Button
            Button(
                onClick = {
                    VoiceAssistantManager.triggerVoiceAssistant()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Voice Assistant Trigger")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Alternative methods
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Alternative Activation Methods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    AlternativeMethod(
                        icon = Icons.Default.VolumeUp,
                        title = "Volume Key Long Press",
                        description = "Map volume up/down long press"
                    )
                    
                    AlternativeMethod(
                        icon = Icons.Default.SmartButton,
                        title = "Bixby Button Remap",
                        description = "Remap Bixby button (Samsung devices)"
                    )
                    
                    AlternativeMethod(
                        icon = Icons.Default.Gesture,
                        title = "Gesture Activation",
                        description = "Use gestures like double-tap back"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Voice Assistant Dialog
    if (showVoiceAssistant) {
        VoiceAssistantDialog(
            onDismiss = { showVoiceAssistant = false },
            onVoiceCommand = { command ->
                // Handle voice command
                showVoiceAssistant = false
            }
        )
    }
}

@Composable
private fun SetupStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Step number
        Surface(
            modifier = Modifier.size(32.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlternativeMethod(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceAssistantDialog(
    onDismiss: () -> Unit,
    onVoiceCommand: (VoiceCommand) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voice Assistant Activated!")
            }
        },
        text = {
            Column {
                Text("Your side key successfully triggered the voice assistant!")
                Spacer(modifier = Modifier.height(16.dp))
                
                // Embedded voice assistant
                VoiceAssistantButton(
                    onVoiceCommand = onVoiceCommand,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}