package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.app.data.AppDatabase
import com.example.app.data.NoteRepository
import com.example.app.navigation.EchoNoteNavGraph
import com.example.app.ui.ApiKeyDialog
import com.example.app.ui.theme.EchoNoteTheme
import com.example.app.viewmodel.NoteViewModel
import com.example.app.util.ApiKeyProvider
import com.example.app.network.RetrofitInstance

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { permission ->
                if (permission.value) {
                    // Permission is granted. Continue the action or workflow in your app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                }
            }
        }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Handle widget intents
		val widgetAction = intent.getStringExtra("widget_action")

		setContent {
			EchoNoteTheme {
				Surface {
					var showPermissionDialog by remember { mutableStateOf(false) }
					var pendingPermissions by remember { mutableStateOf<Array<String>>(emptyArray()) }						// Show permission rationale dialog
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
						
						val navController = rememberNavController()
						val context = applicationContext
						val db = AppDatabase.getDatabase(context)
						val repo = NoteRepository(db.noteDao())
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
						
						val permissionsToRequest = permissionsToCheck.filter { permission ->
							ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED
						}
						
						// Show permission dialog if needed
						if (permissionsToRequest.isNotEmpty() && !showPermissionDialog) {
							pendingPermissions = permissionsToRequest.toTypedArray()
							showPermissionDialog = true
						}
						
						var showApiKeyDialog by remember { mutableStateOf(ApiKeyProvider.getApiKey(context) == null) }
						if (showApiKeyDialog) {
							ApiKeyDialog(
								onDismiss = {},
								onApiKeySaved = {
									showApiKeyDialog = false
									RetrofitInstance.init(context)
								}
							)
						} else {
							RetrofitInstance.init(context)
							
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
							
							EchoNoteNavGraph(navController, viewModel, startDestination)
						}
					}
				}
			}
		}
	}

