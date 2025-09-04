package com.example.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app.util.ApiKeyProvider
import com.example.app.util.ApiKeyValidator
import kotlinx.coroutines.launch

@Composable
fun ApiKeyDialog(onDismiss: () -> Unit, onApiKeySaved: () -> Unit) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isValidKey by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when needed
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    Box {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Enter OpenAI API Key", color = Color.White) },
            text = {
                Column {
                    TextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = { Text("sk-...", color = Color(0xFFB0B0B0)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isValidating
                    )
                    if (isValidating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Validating...", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (apiKey.isNotBlank()) {
                            isValidating = true
                            coroutineScope.launch {
                                try {
                                    val isValid = ApiKeyValidator.validateOpenAIKey(context, apiKey)
                                    isValidating = false
                                    
                                    if (isValid) {
                                        ApiKeyProvider.saveApiKey(context, apiKey)
                                        snackbarMessage = "OpenAI key valid ✓"
                                        isValidKey = true
                                        showSnackbar = true
                                        
                                        // Wait for snackbar to show, then close dialog
                                        kotlinx.coroutines.delay(1500)
                                        onApiKeySaved()
                                    } else {
                                        snackbarMessage = "OpenAI key invalid ✗"
                                        isValidKey = false
                                        showSnackbar = true
                                    }
                                } catch (e: Exception) {
                                    isValidating = false
                                    snackbarMessage = "OpenAI key invalid ✗"
                                    isValidKey = false
                                    showSnackbar = true
                                }
                            }
                        }
                    },
                    enabled = !isValidating && apiKey.isNotBlank()
                ) {
                    Text("Validate & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF222222)
        )
        
        // Snackbar positioned at the bottom of the dialog
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            snackbar = { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = if (isValidKey) Color(0xFF4CAF50) else Color(0xFFF44336),
                    contentColor = Color.White
                )
            }
        )
    }
}



