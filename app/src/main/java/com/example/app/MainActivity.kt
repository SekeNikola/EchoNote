package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.app.data.AppDatabase
import com.example.app.data.NoteRepository
import com.example.app.navigation.LogionNavGraph
import com.example.app.ui.ApiKeyDialog
import com.example.app.ui.theme.LogionTheme
import com.example.app.viewmodel.NoteViewModel
import com.example.app.util.ApiKeyProvider
import com.example.app.util.ApiKeyValidator
import com.example.app.network.RetrofitInstance
import com.example.app.server.ServerService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // All permissions granted - trigger recomposition by invalidating
            } else {
                // Some permissions denied - explain to the user
                permissions.entries.forEach { permission ->
                    if (!permission.value) {
                        // Permission denied - could show specific feedback
                    }
                }
            }
        }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Start the server service
		val serverIntent = Intent(this, ServerService::class.java)
		startService(serverIntent)

		// Handle widget intents
		val widgetAction = intent.getStringExtra("widget_action")

		setContent {
					LogionTheme {
						val snackbarHostState = remember { SnackbarHostState() }
						val coroutineScope = rememberCoroutineScope()
						
						// Move state variables to Box level for access across components
						var showPermissionDialog by remember { mutableStateOf(false) }
						var pendingPermissions by remember { mutableStateOf<Array<String>>(emptyArray()) }
						
						androidx.compose.foundation.layout.Box {
							Surface {
								val navController = rememberNavController()
						val context = applicationContext
						val db = AppDatabase.getDatabase(context)
						val repo = NoteRepository(db.noteDao(), db.taskDao(), db.chatMessageDao())
						val app = requireNotNull(application) as android.app.Application
						val viewModel: NoteViewModel = viewModel(
							factory = object : ViewModelProvider.Factory {
								override fun <T : ViewModel> create(modelClass: Class<T>): T {
									@Suppress("UNCHECKED_CAST")
									return NoteViewModel(repo, app) as T
								}
							}
						)
						// Initialize RetrofitInstance with context
						RetrofitInstance.init(context)
						
						// Check if we need to show permission dialog
						val permissionsToCheck = mutableListOf<String>().apply {
							// Essential audio and camera permissions
							add(Manifest.permission.RECORD_AUDIO)
							add(Manifest.permission.CAMERA)
							
							// Storage permissions based on Android version
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
								// Android 13+ granular media permissions
								add(Manifest.permission.READ_MEDIA_IMAGES)
								add(Manifest.permission.READ_MEDIA_AUDIO)
								add(Manifest.permission.READ_MEDIA_VIDEO)
							} else {
								// Android 12 and below
								add(Manifest.permission.READ_EXTERNAL_STORAGE)
								if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
									// Android 9 and below
									add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
								}
							}
							
							// Audio settings for better recording quality
							add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
							
							// Optional but useful permissions
							add(Manifest.permission.VIBRATE)
						}
						
						var showApiKeyDialog by remember { mutableStateOf(ApiKeyProvider.getApiKey(context) == null) }
						
						// Simple permission checking - recalculate when needed
						val permissionsToRequest = permissionsToCheck.filter { permission ->
							ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED
						}
						
						// Show permission dialog when API key dialog closes and permissions are needed
						LaunchedEffect(showApiKeyDialog) {
							if (!showApiKeyDialog) {
								// API key dialog just closed, check permissions
								val currentPermissionsNeeded = permissionsToCheck.filter { permission ->
									ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED
								}
								if (currentPermissionsNeeded.isNotEmpty() && !showPermissionDialog) {
									pendingPermissions = currentPermissionsNeeded.toTypedArray()
									showPermissionDialog = true
								}
							}
						}
						
						// Auto-close permission dialog when all permissions are granted
						LaunchedEffect(showPermissionDialog) {
							if (showPermissionDialog) {
								val currentPermissionsNeeded = permissionsToCheck.filter { permission ->
									ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED
								}
								if (currentPermissionsNeeded.isEmpty()) {
									showPermissionDialog = false
								}
							}
						}
						
						// Show dialogs in priority order - API key first, then permissions
						when {
							showApiKeyDialog -> {
								ApiKeyDialog(
									onDismiss = {},
									onApiKeySaved = {
										showApiKeyDialog = false
										RetrofitInstance.init(context)
									}
								)
							}
							showPermissionDialog -> {
								com.example.app.ui.PermissionRationaleDialog(
									onDismiss = { showPermissionDialog = false },
									onGrantPermissions = {
										showPermissionDialog = false
										if (pendingPermissions.isNotEmpty()) {
											requestPermissionLauncher.launch(pendingPermissions)
										}
									}
								)
							}
						}
						
						if (!showApiKeyDialog) {
							RetrofitInstance.init(context)
							
							// Validate existing API key on startup
							var hasValidatedKey by remember { mutableStateOf(false) }
							LaunchedEffect(Unit) {
								val existingKey = ApiKeyProvider.getApiKey(context)
								if (existingKey != null && !hasValidatedKey) {
									coroutineScope.launch {
										try {
											val isValid = ApiKeyValidator.validateOpenAIKey(context, existingKey)
											val message = if (isValid) {
												"OpenAI key valid ✓"
											} else {
												"OpenAI key invalid ✗"
											}
											snackbarHostState.showSnackbar(
												message = message,
												duration = SnackbarDuration.Short
											)
											hasValidatedKey = true
										} catch (e: Exception) {
											snackbarHostState.showSnackbar(
												message = "OpenAI key invalid ✗",
												duration = SnackbarDuration.Short
											)
											hasValidatedKey = true
										}
									}
								}
							}
							
							// Determine starting destination based on widget action
							val startDestination = when (widgetAction) {
								"record_audio" -> "recording"
								"upload_audio" -> "uploadAudio"
								"take_picture" -> "imageCapture"
								"upload_image" -> "uploadImage"
								"type_text" -> "typeText"
								"videos" -> "videoUrl"
								"web_page" -> "webPage"
								"upload_files" -> "documentUpload"
								"assistant" -> "voiceCommand"
								else -> "home"
							}
							
							LogionNavGraph(navController, viewModel, startDestination)
						}
					}
					
					// Snackbar host for showing API key validation results
					SnackbarHost(
						hostState = snackbarHostState,
						modifier = androidx.compose.ui.Modifier
							.fillMaxWidth()
							.padding(16.dp),
						snackbar = { snackbarData ->
							Snackbar(
								snackbarData = snackbarData,
								containerColor = if (snackbarData.visuals.message.contains("valid ✓")) {
									Color(0xFF4CAF50)
								} else {
									Color(0xFFF44336)
								},
								contentColor = Color.White
							)
						}
					)
					
					// Show permission dialog above everything else
					if (showPermissionDialog) {
						com.example.app.ui.PermissionRationaleDialog(
							onDismiss = { showPermissionDialog = false },
							onGrantPermissions = {
								showPermissionDialog = false
								if (pendingPermissions.isNotEmpty()) {
									requestPermissionLauncher.launch(pendingPermissions)
								}
							}
						)
					}
				}
			}
		}
	}
}

