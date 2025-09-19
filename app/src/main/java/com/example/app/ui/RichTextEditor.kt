package com.example.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sealed class for different editor items
sealed class EditorItem {
    data class Text(val text: String) : EditorItem()
    data class Checkbox(val text: String, val isChecked: Boolean, val id: String = "cb_${System.currentTimeMillis()}") : EditorItem()
}

// Legacy data classes for compatibility
data class RichTextState(
    val text: TextFieldValue = TextFieldValue(),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val fontSize: Int = 16,
    val checkboxes: MutableList<CheckboxItem> = mutableListOf()
)

data class CheckboxItem(
    val id: String,
    val text: String,
    var isChecked: Boolean = false,
    val position: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditor(
    initialText: String = "",
    onTextChange: (String, List<CheckboxItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse initial text into editor items
    var editorItems by remember {
        mutableStateOf(
            parseTextToEditorItems(initialText).ifEmpty { 
                listOf(EditorItem.Text("")) 
            }
        )
    }

    // Trigger callback when items change
    LaunchedEffect(editorItems) {
        val (text, checkboxes) = convertEditorItemsToOutput(editorItems)
        onTextChange(text, checkboxes)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Rich text formatting toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Text Block Button
                IconButton(
                    onClick = {
                        editorItems = editorItems + EditorItem.Text("")
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add text block",
                        tint = Color(0xFFB0B0B0)
                    )
                }
                
                // Add Checkbox Button - Main feature
                IconButton(
                    onClick = {
                        editorItems = editorItems + EditorItem.Checkbox("", false)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.CheckBox,
                        contentDescription = "Add checkbox",
                        tint = Color(0xFF4CAF50)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "${editorItems.count { it is EditorItem.Checkbox }} checkboxes",
                    color = Color(0xFFB0B0B0),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main editor area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                RichTextEditorWithCheckboxes(
                    items = editorItems,
                    onItemsChange = { newItems ->
                        editorItems = newItems
                    }
                )
            }
        }
    }
}

@Composable
private fun RichTextEditorWithCheckboxes(
    items: List<EditorItem>,
    onItemsChange: (List<EditorItem>) -> Unit
) {
    Column {
        items.forEachIndexed { index, item ->
            when (item) {
                is EditorItem.Text -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = item.text,
                                onValueChange = { newValue ->
                                    val newItems = items.toMutableList()
                                    newItems[index] = EditorItem.Text(newValue)
                                    onItemsChange(newItems)
                                },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (item.text.isEmpty()) {
                                        Text(
                                            "Enter text...",
                                            color = Color(0xFF808080),
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            
                            // Remove button for text blocks too
                            IconButton(
                                onClick = {
                                    val newItems = items.toMutableList()
                                    newItems.removeAt(index)
                                    onItemsChange(newItems)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove text block",
                                    tint = Color(0xFF808080),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                is EditorItem.Checkbox -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF383838)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    val newItems = items.toMutableList()
                                    newItems[index] = item.copy(isChecked = checked)
                                    onItemsChange(newItems)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4CAF50),
                                    uncheckedColor = Color(0xFFB0B0B0),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = item.text,
                                onValueChange = { newValue ->
                                    val newItems = items.toMutableList()
                                    newItems[index] = item.copy(text = newValue)
                                    onItemsChange(newItems)
                                },
                                textStyle = TextStyle(
                                    color = if (item.isChecked) Color(0xFFB0B0B0) else Color.White,
                                    fontSize = 16.sp,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (item.text.isEmpty()) {
                                        Text(
                                            "Enter task...",
                                            color = Color(0xFF808080),
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            
                            // Remove button
                            IconButton(
                                onClick = {
                                    val newItems = items.toMutableList()
                                    newItems.removeAt(index)
                                    onItemsChange(newItems)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove checkbox",
                                    tint = Color(0xFF808080),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Add text block button if list is empty or last item is checkbox
        if (items.isEmpty() || items.last() is EditorItem.Checkbox) {
            TextButton(
                onClick = {
                    onItemsChange(items + EditorItem.Text(""))
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add text",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add text block", color = Color(0xFFB0B0B0))
            }
        }
    }
}

// Helper functions for parsing and converting
private fun parseTextToEditorItems(text: String): List<EditorItem> {
    if (text.isEmpty()) return listOf(EditorItem.Text(""))
    
    // First try to parse as JSON
    try {
        val json = org.json.JSONObject(text)
        val textContent = json.optString("text", "")
        val checkboxesArray = json.optJSONArray("checkboxes")
        
        val items = mutableListOf<EditorItem>()
        
        // Add text content if present
        if (textContent.isNotBlank()) {
            items.add(EditorItem.Text(textContent))
        }
        
        // Add checkboxes if present
        if (checkboxesArray != null) {
            for (i in 0 until checkboxesArray.length()) {
                val checkboxJson = checkboxesArray.getJSONObject(i)
                items.add(
                    EditorItem.Checkbox(
                        text = checkboxJson.getString("text"),
                        isChecked = checkboxJson.getBoolean("checked")
                    )
                )
            }
        }
        
        return items.ifEmpty { listOf(EditorItem.Text("")) }
    } catch (e: Exception) {
        // Fallback to markdown parsing
        val lines = text.split("\n")
        val items = mutableListOf<EditorItem>()
        val textLines = mutableListOf<String>()
        
        for (line in lines) {
            if (line.matches(Regex("""- \[[x ]\] .+"""))) {
                // Found checkbox, first add any accumulated text
                if (textLines.isNotEmpty()) {
                    items.add(EditorItem.Text(textLines.joinToString("\n")))
                    textLines.clear()
                }
                
                // Add checkbox
                val isChecked = line.contains("- [x]")
                val checkboxText = line.removePrefix("- [x] ").removePrefix("- [ ] ")
                items.add(EditorItem.Checkbox(checkboxText, isChecked))
            } else {
                textLines.add(line)
            }
        }
        
        // Add remaining text if any
        if (textLines.isNotEmpty()) {
            items.add(EditorItem.Text(textLines.joinToString("\n")))
        }
        
        return items.ifEmpty { listOf(EditorItem.Text("")) }
    }
}

private fun convertEditorItemsToOutput(items: List<EditorItem>): Pair<String, List<CheckboxItem>> {
    val textParts = mutableListOf<String>()
    val checkboxes = mutableListOf<CheckboxItem>()
    
    items.forEach { item ->
        when (item) {
            is EditorItem.Text -> {
                if (item.text.isNotBlank()) {
                    textParts.add(item.text)
                }
            }
            is EditorItem.Checkbox -> {
                checkboxes.add(
                    CheckboxItem(
                        id = item.id,
                        text = item.text,
                        isChecked = item.isChecked,
                        position = 0 // Position is not used in new system
                    )
                )
            }
        }
    }
    
    return Pair(textParts.joinToString("\n\n"), checkboxes)
}