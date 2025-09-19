package com.example.app.data

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CheckboxItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)

class TaskItemListConverter {
    private val json = Json { ignoreUnknownKeys = true }
    
    @TypeConverter
    fun fromCheckboxItemList(items: List<CheckboxItem>): String {
        return json.encodeToString(items)
    }
    
    @TypeConverter
    fun toCheckboxItemList(itemsString: String): List<CheckboxItem> {
        return if (itemsString.isBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(itemsString)
            } catch (e: Exception) {
                // If parsing fails, try to parse as plain text with checkboxes
                parseCheckboxesFromText(itemsString)
            }
        }
    }
    
    private fun parseCheckboxesFromText(text: String): List<CheckboxItem> {
        val checkboxItems = mutableListOf<CheckboxItem>()
        val lines = text.split("\n")
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                // Markdown-style checkboxes: - [ ] or - [x]
                trimmedLine.startsWith("- [ ]") -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine.substring(5).trim(),
                        isChecked = false
                    ))
                }
                trimmedLine.startsWith("- [x]") || trimmedLine.startsWith("- [X]") -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine.substring(5).trim(),
                        isChecked = true
                    ))
                }
                // HTML-style checkboxes
                trimmedLine.contains("<input type=\"checkbox\"") -> {
                    val isChecked = trimmedLine.contains("checked")
                    val textStart = trimmedLine.indexOf(">") + 1
                    val textEnd = trimmedLine.lastIndexOf("<")
                    val text = if (textEnd > textStart) {
                        trimmedLine.substring(textStart, textEnd).trim()
                    } else {
                        trimmedLine.substring(textStart).trim()
                    }
                    checkboxItems.add(CheckboxItem(
                        text = text,
                        isChecked = isChecked
                    ))
                }
                // Regular text lines (non-checkbox)
                trimmedLine.isNotBlank() -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine,
                        isChecked = false
                    ))
                }
            }
        }
        
        return checkboxItems
    }
}

// Utility functions for checkbox conversion
object CheckboxUtils {
    
    fun parseCheckboxesFromText(text: String): List<CheckboxItem> {
        val checkboxItems = mutableListOf<CheckboxItem>()
        val lines = text.split("\n")
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                // Markdown-style checkboxes: - [ ] or - [x]
                trimmedLine.startsWith("- [ ]") -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine.substring(5).trim(),
                        isChecked = false
                    ))
                }
                trimmedLine.startsWith("- [x]") || trimmedLine.startsWith("- [X]") -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine.substring(5).trim(),
                        isChecked = true
                    ))
                }
                // HTML-style checkboxes
                trimmedLine.contains("<input type=\"checkbox\"") -> {
                    val isChecked = trimmedLine.contains("checked")
                    val textStart = trimmedLine.indexOf(">") + 1
                    val textEnd = trimmedLine.lastIndexOf("<")
                    val text = if (textEnd > textStart) {
                        trimmedLine.substring(textStart, textEnd).trim()
                    } else {
                        trimmedLine.substring(textStart).trim()
                    }
                    checkboxItems.add(CheckboxItem(
                        text = text,
                        isChecked = isChecked
                    ))
                }
                // Regular text lines (non-checkbox) - only add if not empty
                trimmedLine.isNotBlank() && !trimmedLine.startsWith("-") -> {
                    checkboxItems.add(CheckboxItem(
                        text = trimmedLine,
                        isChecked = false
                    ))
                }
            }
        }
        
        return checkboxItems
    }
    
    fun convertCheckboxItemsToMarkdown(items: List<CheckboxItem>): String {
        return items.joinToString("\n") { item ->
            val checkbox = if (item.isChecked) "- [x]" else "- [ ]"
            "$checkbox ${item.text}"
        }
    }
    
    fun convertCheckboxItemsToHtml(items: List<CheckboxItem>): String {
        return items.joinToString("<br>") { item ->
            val checked = if (item.isChecked) "checked" else ""
            "<div class=\"checkbox-item\">" +
                    "<input type=\"checkbox\" id=\"${item.id}\" $checked>" +
                    "<span>${item.text}</span>" +
                    "</div>"
        }
    }
    
    fun updateCheckboxState(items: List<CheckboxItem>, checkboxId: String, isChecked: Boolean): List<CheckboxItem> {
        return items.map { item ->
            if (item.id == checkboxId) {
                item.copy(isChecked = isChecked)
            } else {
                item
            }
        }
    }
}