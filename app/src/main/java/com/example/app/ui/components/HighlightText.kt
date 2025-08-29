package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HighlightText(text: String) {
    Text(
        text = text,
        color = Color(0xFFFFC107),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .background(Color(0xFF222222))
            .padding(4.dp)
    )
}
