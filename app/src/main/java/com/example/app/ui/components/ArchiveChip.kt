package com.example.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ArchiveChip(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, color = Color(0xFFB0B0B0)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1F1F1F))
    )
}
