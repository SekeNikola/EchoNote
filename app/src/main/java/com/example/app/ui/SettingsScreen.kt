package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.example.app.viewmodel.NoteViewModel
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.app.utils.OpenAITTS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val context = LocalContext.current
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf("alloy") }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    
    val voiceOptions = listOf(
        "alloy" to "Alloy",
        "echo" to "Echo", 
        "fable" to "Fable",
        "onyx" to "Onyx",
        "nova" to "Nova",
        "shimmer" to "Shimmer"
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF282828),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF282828)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Server Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val serverUrls = com.example.app.server.NgrokManager.getServerUrls()
                    
                    serverUrls.forEach { url ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF3A3A4E),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (url.startsWith("Local:")) Icons.Default.Computer else Icons.Default.Public,
                                contentDescription = null,
                                tint = Color(0xFFFF8C00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = url,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = "Use these URLs to access the web client from any device",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
            }
            
            // Permissions Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Permissions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            // Open app settings to grant permissions
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8C00)
                        )
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant Permissions",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // API Key Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "OpenAI API Key",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8C00)
                        )
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Update API Key",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Voice Assistant Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Voice Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Voice Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showVoiceDropdown,
                        onExpandedChange = { showVoiceDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = voiceOptions.find { it.first == selectedVoice }?.second ?: "Alloy",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Voice Type", color = Color(0xFFB0B0B0)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVoiceDropdown)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF8C00),
                                unfocusedBorderColor = Color(0xFF4A4A5E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF8C00)
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showVoiceDropdown,
                            onDismissRequest = { showVoiceDropdown = false },
                            modifier = Modifier.background(Color(0xFF2A2A3E))
                        ) {
                            voiceOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                color = Color.White
                                            )
                                            IconButton(
                                                onClick = {
                                                    // Play voice preview
                                                    playVoicePreview(value, context)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = "Preview",
                                                    tint = Color(0xFFFF8C00),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedVoice = value
                                        showVoiceDropdown = false
                                        // Save voice preference
                                        saveVoicePreference(value, context)
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text(
                    text = "OpenAI API Key",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your OpenAI API key to enable AI features:",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key", color = Color(0xFFB0B0B0)) },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide" else "Show",
                                    tint = Color(0xFFFF8C00)
                                )
                            }
                        },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            saveApiKey(apiKeyInput, context)
                            showApiKeyDialog = false
                            apiKeyInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8C00)
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showApiKeyDialog = false
                        apiKeyInput = ""
                        showApiKey = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF8C00)
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF2A2A3E),
            textContentColor = Color.White
        )
    }
}

private fun playVoicePreview(voice: String, context: android.content.Context) {
    try {
        // Get API key from preferences
        val sharedPrefs = context.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
        val apiKey = sharedPrefs.getString("openai_api_key", "") ?: ""
        
        if (apiKey.isNotBlank()) {
            val tts = OpenAITTS(context, apiKey)
            tts.speak(
                text = "Hello! This is how I sound with the $voice voice.",
                voice = voice,
                onReady = {
                    // Voice preview started
                },
                onError = { error ->
                    // Handle error silently or show a toast
                }
            )
        }
    } catch (e: Exception) {
        // Handle error silently
    }
}

private fun saveVoicePreference(voice: String, context: android.content.Context) {
    val sharedPrefs = context.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
    sharedPrefs.edit().putString("preferred_voice", voice).apply()
}

private fun saveApiKey(apiKey: String, context: android.content.Context) {
    val sharedPrefs = context.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
    sharedPrefs.edit().putString("openai_api_key", apiKey).apply()
}
