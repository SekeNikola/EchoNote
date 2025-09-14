package com.example.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.app.data.ExportData
import com.example.app.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }
    var importMessage by remember { mutableStateOf("") }
    
    // File picker for export (save file)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            isExporting = true
            viewModel.viewModelScope.launch {
                try {
                    val exportData = viewModel.exportAllData()
                    val jsonString = exportData.toJson()
                    
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    
                    exportMessage = "Data exported successfully!"
                } catch (e: Exception) {
                    exportMessage = "Export failed: ${e.message}"
                } finally {
                    isExporting = false
                }
            }
        }
    }
    
    // File picker for import (open file)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isImporting = true
            viewModel.viewModelScope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    }
                    
                    if (jsonString != null) {
                        val importData = ExportData.fromJson(jsonString)
                        if (importData != null) {
                            viewModel.importData(importData, replaceExisting = false)
                            importMessage = "Data imported successfully! Added ${importData.notes.size} notes, ${importData.tasks.size} tasks, and ${importData.chatMessages.size} chat messages."
                        } else {
                            importMessage = "Import failed: Invalid file format"
                        }
                    } else {
                        importMessage = "Import failed: Could not read file"
                    }
                } catch (e: Exception) {
                    importMessage = "Import failed: ${e.message}"
                } finally {
                    isImporting = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export / Import") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Export all your notes, tasks, and chat messages to a JSON file that can be imported later or shared with other devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("logion_backup_$timestamp.json")
                        },
                        enabled = !isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporting...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Data")
                        }
                    }
                    
                    if (exportMessage.isNotEmpty()) {
                        Text(
                            text = exportMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (exportMessage.contains("successfully")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
            
            // Import Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Import Data",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Import notes, tasks, and chat messages from a previously exported JSON file. This will add the data to your existing content (it won't replace it).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            importLauncher.launch("application/json")
                        },
                        enabled = !isImporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importing...")
                        } else {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Data")
                        }
                    }
                    
                    if (importMessage.isNotEmpty()) {
                        Text(
                            text = importMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (importMessage.contains("successfully")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
            
            // Information Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Important Notes",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "• Exported files contain all your personal data in JSON format",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Keep backup files secure and private",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Import adds data without deleting existing content",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• You can import the same file multiple times (duplicates may occur)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}