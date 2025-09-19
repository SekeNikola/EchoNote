package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.app.worker.ReminderScheduler
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

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Check if overlay permission was granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // Permission granted - could show success message
                } else {
                    // Permission denied - could show explanation
                }
            }
        }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Start the server service as foreground service
		ServerService.startService(this)
		
		// Schedule recurring notifications (8AM daily reminders, etc.)
		ReminderScheduler.scheduleRecurringNotifications(this)
		
		// Request notification permission for Android 13+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
				!= PackageManager.PERMISSION_GRANTED) {
				requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
			}
		}
		
		// Request to ignore battery optimizations for continuous background operation
		requestBatteryOptimizationExemption()

		// Request overlay permission for voice assistant orb
		requestOverlayPermission()

		// Handle widget intents
		val widgetAction = intent.getStringExtra("widget_action")
		
		// Handle navigation intents (from VoiceAssistantTriggerActivity)
		val navigateTo = intent.getStringExtra("navigate_to")
		
		// Handle voice assistant activation
		val startVoiceAssistant = intent.getBooleanExtra("start_voice_assistant", false)

		setContent {
					LogionTheme {
						val snackbarHostState = remember { SnackbarHostState() }
						val coroutineScope = rememberCoroutineScope()
						
						// Voice assistant state
						var shouldStartVoiceAssistant by remember { mutableStateOf(startVoiceAssistant) }
						
						// Move state variables to Box level for access across components
						var showPermissionDialog by remember { mutableStateOf(false) }
						var pendingPermissions by remember { mutableStateOf<Array<String>>(emptyArray()) }
						
						// Monitor shared preferences for voice assistant triggers
						LaunchedEffect(Unit) {
							val sharedPref = getSharedPreferences("voice_prefs", MODE_PRIVATE)
							while (true) {
								kotlinx.coroutines.delay(500) // Check every 500ms
								val triggerVoice = sharedPref.getBoolean("trigger_voice", false)
								
								if (triggerVoice) {
									shouldStartVoiceAssistant = true
									sharedPref.edit().putBoolean("trigger_voice", false).apply()
								}
							}
						}
						
						androidx.compose.foundation.layout.Box {
							Surface {
								val navController = rememberNavController()
						val context = applicationContext
						val db = AppDatabase.getDatabase(context)
						val repo = NoteRepository(db.noteDao(), db.taskDao(), db.chatMessageDao(), db.reminderDao())
						val app = requireNotNull(application)
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
							LaunchedEffect(Unit) {
								val existingKey = ApiKeyProvider.getApiKey(context)
								if (existingKey != null) {
									val prefs = getSharedPreferences("logion_prefs", MODE_PRIVATE)
									val hasShownSuccessValidation = prefs.getBoolean("has_shown_success_validation", false)
									
									coroutineScope.launch {
										try {
											val isValid = ApiKeyValidator.validateOpenAIKey(context, existingKey)
											
											// Only show snackbar for invalid keys OR first time valid key
											if (!isValid) {
												snackbarHostState.showSnackbar(
													message = "OpenAI key invalid ✗",
													duration = SnackbarDuration.Short
												)
											} else if (isValid && !hasShownSuccessValidation) {
												snackbarHostState.showSnackbar(
													message = "OpenAI key valid ✓",
													duration = SnackbarDuration.Short
												)
												// Mark that we've shown the success validation
												prefs.edit().putBoolean("has_shown_success_validation", true).apply()
											}
										} catch (e: Exception) {
											snackbarHostState.showSnackbar(
												message = "OpenAI key invalid ✗",
												duration = SnackbarDuration.Short
											)
										}
									}
								}
							}
							
							// Determine starting destination based on widget action or navigation intent
							val startDestination = when {
								navigateTo == "ai_voice" -> "ai_voice"
								widgetAction == "record_audio" -> "recording"
								widgetAction == "upload_audio" -> "uploadAudio"
								widgetAction == "take_picture" -> "imageCapture"
								widgetAction == "upload_image" -> "uploadImage"
								widgetAction == "type_text" -> "typeText"
								widgetAction == "videos" -> "videoUrl"
								widgetAction == "web_page" -> "webPage"
								widgetAction == "upload_files" -> "documentUpload"
								widgetAction == "assistant" -> "voiceCommand"
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
	
	private fun requestBatteryOptimizationExemption() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				val powerManager = getSystemService(POWER_SERVICE) as PowerManager
				val packageName = packageName
				
				if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
					// Request user to disable battery optimization for this app
					val intent = Intent().apply {
						action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
						data = Uri.parse("package:$packageName")
					}
					
					try {
						startActivity(intent)
					} catch (e: Exception) {
						// If the specific intent fails, open general battery optimization settings
						val generalIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
						startActivity(generalIntent)
					}
				}
			}
		} catch (e: Exception) {
			// Log the error but don't crash the app
			android.util.Log.e("MainActivity", "Error requesting battery optimization exemption", e)
		}
	}
	
	private fun requestOverlayPermission() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (!Settings.canDrawOverlays(this)) {
					val intent = Intent(
						Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:$packageName")
					)
					overlayPermissionLauncher.launch(intent)
				}
			}
		} catch (e: Exception) {
			android.util.Log.e("MainActivity", "Error requesting overlay permission", e)
		}
	}
	
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		
		// Handle voice assistant activation when app is already running
		if (intent?.getBooleanExtra("start_voice_assistant", false) == true ||
			intent?.action == "android.intent.action.VOICE_ASSISTANT") {
			
			// Trigger voice assistant - you can use a shared preference or broadcast
			// to communicate with your Compose UI
			val sharedPref = getSharedPreferences("voice_prefs", MODE_PRIVATE)
			sharedPref.edit().putBoolean("trigger_voice", true).apply()
			sharedPref.edit().putBoolean("show_voice_orb", true).apply()
		}
		
		// Handle voice orb display request
		if (intent?.getBooleanExtra("show_voice_orb", false) == true) {
			val sharedPref = getSharedPreferences("voice_prefs", MODE_PRIVATE)
			sharedPref.edit().putBoolean("show_voice_orb", true).apply()
		}
	}
}

