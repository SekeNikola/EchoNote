package com.example.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    MinimalTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = "Search notes",
        modifier = modifier
    )
}
