package com.example.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app.util.ApiKeyProvider

@Composable
fun ApiKeyDialog(onDismiss: () -> Unit, onApiKeySaved: () -> Unit) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf("") }
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
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                ApiKeyProvider.saveApiKey(context, apiKey)
                onApiKeySaved()
            }) {
                Text("Save")
            }
        },
        containerColor = Color(0xFF222222)
    )
}
