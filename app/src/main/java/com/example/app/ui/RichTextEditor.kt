package com.example.app.ui

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
import kotlinx.coroutines.launch

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
    var richTextState by remember { 
        mutableStateOf(
            RichTextState(
                text = TextFieldValue(initialText),
                checkboxes = parseCheckboxes(initialText).toMutableList()
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formatting toolbar
        FormattingToolbar(
            state = richTextState,
            onStateChange = { richTextState = it },
            onAddCheckbox = {
                val newCheckbox = CheckboxItem(
                    id = "cb_${System.currentTimeMillis()}",
                    text = "",
                    position = richTextState.text.text.length
                )
                richTextState = richTextState.copy(
                    checkboxes = richTextState.checkboxes.apply { add(newCheckbox) }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main text editor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Render text with checkboxes
                RenderRichText(
                    state = richTextState,
                    onStateChange = { richTextState = it },
                    onTextChange = onTextChange
                )
            }
        }


    }
}

@Composable
private fun FormattingToolbar(
    state: RichTextState,
    onStateChange: (RichTextState) -> Unit,
    onAddCheckbox: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FormatButton(
                icon = Icons.Default.FormatBold,
                isSelected = state.isBold,
                onClick = { onStateChange(state.copy(isBold = !state.isBold)) }
            )
        }
        
        item {
            FormatButton(
                icon = Icons.Default.FormatItalic,
                isSelected = state.isItalic,
                onClick = { onStateChange(state.copy(isItalic = !state.isItalic)) }
            )
        }
        
        item {
            FormatButton(
                icon = Icons.Default.FormatUnderlined,
                isSelected = state.isUnderline,
                onClick = { onStateChange(state.copy(isUnderline = !state.isUnderline)) }
            )
        }
        
        item {
            FormatButton(
                icon = Icons.Default.FormatStrikethrough,
                isSelected = state.isStrikethrough,
                onClick = { onStateChange(state.copy(isStrikethrough = !state.isStrikethrough)) }
            )
        }
        
        item {
            Divider(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
            )
        }
        
        item {
            FormatButton(
                icon = Icons.Default.CheckBox,
                isSelected = false,
                onClick = onAddCheckbox
            )
        }
        
        item {
            // Font size controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { 
                        if (state.fontSize > 12) {
                            onStateChange(state.copy(fontSize = state.fontSize - 2))
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease font size",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = "${state.fontSize}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = { 
                        if (state.fontSize < 24) {
                            onStateChange(state.copy(fontSize = state.fontSize + 2))
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase font size",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RenderRichText(
    state: RichTextState,
    onStateChange: (RichTextState) -> Unit,
    onTextChange: (String, List<CheckboxItem>) -> Unit
) {
    Column {
        // Render checkboxes
        state.checkboxes.forEach { checkbox ->
            CheckboxRow(
                checkbox = checkbox,
                onCheckedChange = { isChecked ->
                    val updatedCheckboxes = state.checkboxes.map { cb ->
                        if (cb.id == checkbox.id) cb.copy(isChecked = isChecked) else cb
                    }.toMutableList()
                    
                    val newState = state.copy(checkboxes = updatedCheckboxes)
                    onStateChange(newState)
                    onTextChange(newState.text.text, newState.checkboxes)
                },
                onTextChange = { newText ->
                    val updatedCheckboxes = state.checkboxes.map { cb ->
                        if (cb.id == checkbox.id) cb.copy(text = newText) else cb
                    }.toMutableList()
                    
                    val newState = state.copy(checkboxes = updatedCheckboxes)
                    onStateChange(newState)
                    onTextChange(newState.text.text, newState.checkboxes)
                },
                onRemove = {
                    val updatedCheckboxes = state.checkboxes.filter { it.id != checkbox.id }.toMutableList()
                    val newState = state.copy(checkboxes = updatedCheckboxes)
                    onStateChange(newState)
                    onTextChange(newState.text.text, newState.checkboxes)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Main text field
        OutlinedTextField(
            value = state.text,
            onValueChange = { newValue ->
                val newState = state.copy(text = newValue)
                onStateChange(newState)
                onTextChange(newValue.text, newState.checkboxes)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = state.fontSize.sp,
                fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    state.isUnderline && state.isStrikethrough -> 
                        TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    state.isUnderline -> TextDecoration.Underline
                    state.isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
            ),
            placeholder = { Text("Start typing your note...") },
            minLines = 5,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
        )
    }
}

@Composable
private fun CheckboxRow(
    checkbox: CheckboxItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkbox.isChecked,
            onCheckedChange = onCheckedChange
        )
        
        OutlinedTextField(
            value = checkbox.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Enter task...") },
            singleLine = true,
            textStyle = TextStyle(
                textDecoration = if (checkbox.isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        )
        
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove checkbox",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun parseCheckboxes(text: String): List<CheckboxItem> {
    val checkboxes = mutableListOf<CheckboxItem>()
    val checkboxPattern = Regex("""- \[([x ])\] (.+)""")
    
    checkboxPattern.findAll(text).forEachIndexed { index, match ->
        val isChecked = match.groupValues[1] == "x"
        val taskText = match.groupValues[2]
        
        checkboxes.add(
            CheckboxItem(
                id = "cb_parsed_$index",
                text = taskText,
                isChecked = isChecked,
                position = match.range.first
            )
        )
    }
    
    return checkboxes
}