package com.example.app.ui.components

import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { androidx.compose.material3.Text(placeholder, color = Color(0xFFB0B0B0)) },
        modifier = modifier,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color(0xFF1F1F1F)
        )
    )
}
