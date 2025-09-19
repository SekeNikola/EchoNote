package com.example.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriorityChip(priority: String, modifier: Modifier = Modifier) {
    val bg = when (priority.lowercase()) {
        "urgent" -> Color(0xFFE91E63) // Pink
        "high" -> Color(0xFFFF5722)   // Deep orange
        "medium" -> Color(0xFFFF9800) // Orange
        "low" -> Color(0xFF4CAF50)    // Green
        else -> Color(0xFF9E9E9E)      // Grey
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = priority,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
