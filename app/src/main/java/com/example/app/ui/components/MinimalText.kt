package com.example.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MinimalText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Int = 16
) {
    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        modifier = modifier
    )
}
