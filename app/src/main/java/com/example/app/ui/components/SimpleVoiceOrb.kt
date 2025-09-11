package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimpleVoiceOrb(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    isActive: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = if (isActive) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF8C00),
                            Color(0xFF3B82F6)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF8C00).copy(alpha = 0.8f),
                            Color(0xFF3B82F6).copy(alpha = 0.8f)
                        )
                    )
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow effect
        Box(
            modifier = Modifier
                .size(size * 0.6f)
                .background(
                    Color.White.copy(alpha = if (isActive) 0.3f else 0.2f),
                    CircleShape
                )
        )
        
        // Core orb
        Box(
            modifier = Modifier
                .size(size * 0.3f)
                .background(
                    Color.White.copy(alpha = if (isActive) 0.9f else 0.7f),
                    CircleShape
                )
        )
    }
}
