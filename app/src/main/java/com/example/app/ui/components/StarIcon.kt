package com.example.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StarIcon(isFavorite: Boolean) {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "Favorite",
        tint = if (isFavorite) Color(0xFFFFC107) else Color(0xFFB0B0B0)
    )
}
