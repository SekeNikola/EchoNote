package com.example.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ReminderChip(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, color = Color.White) },
        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF03DAC6))
    )
}
