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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
//                showInContextUI(...)
            }
                else -> {
                    // You can directly ask for the permission.
                    requestPermissionLauncher.launch(
                        Manifest.permission.RECORD_AUDIO)
                }
            }

			setContent {
				EchoNoteTheme {
					Surface {
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
							EchoNoteNavGraph(navController, viewModel)
						}
					}
				}
			}
		}
	}

