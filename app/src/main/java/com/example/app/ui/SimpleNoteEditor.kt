package com.example.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@Composable
fun SimpleNoteEditorForBottomSheet(
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentText by remember { mutableStateOf("") }
    var currentCheckboxes by remember { mutableStateOf(mutableListOf<CheckboxDisplayItem>()) }
    
    // Update parent when content changes
    LaunchedEffect(currentText, currentCheckboxes.size, currentCheckboxes.map { "${it.text}-${it.isChecked}" }) {
        val jsonObject = JSONObject().apply {
            put("text", currentText)
            put("checkboxes", org.json.JSONArray().apply {
                currentCheckboxes.forEach { checkbox ->
                    put(JSONObject().apply {
                        put("text", checkbox.text)
                        put("checked", checkbox.isChecked)
                    })
                }
            })
        }
        onContentChange(jsonObject.toString())
    }
    
    Column(
        modifier = modifier.padding(0.dp)
    ) {
        // Add Task button - Always visible at the top
        OutlinedButton(
            onClick = {
                currentCheckboxes = currentCheckboxes.toMutableList().apply {
                    add(CheckboxDisplayItem("", false))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4CAF50)
            ),
            border = BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add task",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Task", fontSize = 16.sp)
        }
        
        // Display and edit checkboxes (if any exist) - above text field
        if (currentCheckboxes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            currentCheckboxes.forEachIndexed { index, checkboxItem ->
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
                            checked = checkboxItem.isChecked,
                            onCheckedChange = { checked ->
                                currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                    set(index, checkboxItem.copy(isChecked = checked))
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = Color(0xFFB0B0B0),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = checkboxItem.text,
                            onValueChange = { newText ->
                                currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                    set(index, checkboxItem.copy(text = newText))
                                }
                            },
                            placeholder = { Text("Enter task...", color = Color(0xFF808080)) },
                            textStyle = TextStyle(
                                color = if (checkboxItem.isChecked) Color(0xFFB0B0B0) else Color.White,
                                fontSize = 16.sp,
                                textDecoration = if (checkboxItem.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF555555),
                                cursorColor = Color(0xFF4CAF50),
                                focusedTextColor = if (checkboxItem.isChecked) Color(0xFFB0B0B0) else Color.White,
                                unfocusedTextColor = if (checkboxItem.isChecked) Color(0xFFB0B0B0) else Color.White,
                                focusedPlaceholderColor = Color(0xFF808080),
                                unfocusedPlaceholderColor = Color(0xFF808080)
                            ),
                            singleLine = true
                        )
                        
                        // Remove button
                        IconButton(
                            onClick = {
                                currentCheckboxes = currentCheckboxes.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove task",
                                tint = Color(0xFF808080),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text editing field - moved to bottom
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            OutlinedTextField(
                value = currentText,
                onValueChange = { currentText = it },
                placeholder = { Text("Enter your note text...", color = Color(0xFFB0B0B0)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF8C00),
                    unfocusedBorderColor = Color(0xFF555555),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFF8C00)
                ),
                singleLine = false
            )
        }
    }
}