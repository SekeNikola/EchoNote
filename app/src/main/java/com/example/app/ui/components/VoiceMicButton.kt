package com.example.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VoiceMicButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF03DAC6),
        contentColor = Color.White,
        modifier = modifier
    ) {
        Icon(Icons.Default.Mic, contentDescription = "Voice Command")
    }
}
