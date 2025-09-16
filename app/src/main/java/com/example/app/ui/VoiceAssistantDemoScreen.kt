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
                        description = """Download KeyMapper from Google Play Store or F-Droid. 

KeyMapper allows you to remap physical buttons like volume keys, power button, or side keys to trigger custom actions.

Google Play Store: Search "KeyMapper" by Seth Schroeder
F-Droid: Available as open source alternative"""
                    )
                    
                    SetupStep(
                        number = "2",
                        title = "Create New Mapping",
                        description = """Open KeyMapper app and create a new key mapping:

• Tap the '+' (Add) button in the bottom right
• Choose "Record trigger" and press the physical key you want to use
• Common options: Volume Down, Volume Up, Power Button (double tap), Bixby Button
• The app will detect and record your key press"""
                    )
                    
                    SetupStep(
                        number = "3",
                        title = "Configure Action Type",
                        description = """Set up the action to trigger Logion:

• In the "Choose Action" section, select "App" then "Broadcast"
• For Action, enter exactly: com.example.app.VOICE_ASSISTANT_TRIGGER
• For Package, enter exactly: com.example.app
• Leave Target Class empty
• Enable "Send to package only" if available

This broadcast will directly open Logion's voice assistant without any overlay."""
                    )
                    
                    SetupStep(
                        number = "4",
                        title = "Configure Constraints (Optional)",
                        description = """Set when the mapping should work:

• Screen State: Choose "Screen On" to only work when phone is unlocked
• App in foreground: Leave blank to work from any app
• Repeat: Set to "Don't repeat" for single activation
• Vibrate: Enable for haptic feedback when triggered"""
                    )
                    
                    SetupStep(
                        number = "5",
                        title = "Enable and Test",
                        description = """Activate your mapping and test:

• Enable the mapping with the toggle switch
• Grant any required permissions (Accessibility Service)
• Press your configured key to test
• Logion should open directly to the voice assistant screen
• No floating orb will appear - you'll go straight to voice input"""
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
            
            // Troubleshooting Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Troubleshooting",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TroubleshootingItem(
                        problem = "Key press not working",
                        solution = "• Enable Accessibility Service for KeyMapper in Settings\n• Check if mapping is enabled (toggle switch)\n• Try recording the key again"
                    )
                    
                    TroubleshootingItem(
                        problem = "App doesn't open",
                        solution = "• Verify package name is exactly: com.example.app\n• Check action name: com.example.app.VOICE_ASSISTANT_TRIGGER\n• Ensure Logion is installed and accessible"
                    )
                    
                    TroubleshootingItem(
                        problem = "Opens wrong screen",
                        solution = "• Make sure you selected 'Broadcast' not 'Open App'\n• Double-check the action string matches exactly\n• Restart KeyMapper after making changes"
                    )
                    
                    TroubleshootingItem(
                        problem = "Permissions issues",
                        solution = "• Grant notification permissions to Logion\n• Allow KeyMapper to access system settings\n• Enable 'Display over other apps' for KeyMapper"
                    )
                }
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
private fun TroubleshootingItem(
    problem: String,
    solution: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "❌ $problem",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = solution,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
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