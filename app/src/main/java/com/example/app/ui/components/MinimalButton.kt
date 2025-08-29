package com.example.app.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MinimalButton(
    text: String,
    onClick: () -> Unit,
    color: Color = Color(0xFFBB86FC)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, color = Color.White)
    }
}
