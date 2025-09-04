
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.ui.*
import com.example.app.viewmodel.NoteViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recording : Screen("recording")
    data class NoteDetail(val noteId: Long) : Screen("noteDetail/{noteId}") {
        fun createRoute(noteId: Long) = "noteDetail/$noteId"
    }
    object VoiceCommand : Screen("voiceCommand")
    object UploadAudio : Screen("uploadAudio")
    object ImageCapture : Screen("imageCapture")
    object UploadImage : Screen("uploadImage")
    object TypeText : Screen("typeText")
    object VideoUrl : Screen("videoUrl")
    object WebPage : Screen("webPage")
    object DocumentUpload : Screen("documentUpload")
}

@Composable
fun EchoNoteNavGraph(
    navController: NavHostController,
    viewModel: NoteViewModel,
    startDestination: String = Screen.Home.route
) {
    NavHost(navController, startDestination = startDestination) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNoteClick = { noteId -> navController.navigate(Screen.NoteDetail(noteId).createRoute(noteId)) },
                onRecordClick = { navController.navigate(Screen.Recording.route) },
                onNavigateToUploadAudio = { navController.navigate(Screen.UploadAudio.route) },
                onNavigateToImageCapture = { navController.navigate(Screen.ImageCapture.route) },
                onNavigateToUploadImage = { navController.navigate(Screen.UploadImage.route) },
                onNavigateToTypeText = { navController.navigate(Screen.TypeText.route) },
                onNavigateToVideoUrl = { navController.navigate(Screen.VideoUrl.route) },
                onNavigateToWebPage = { navController.navigate(Screen.WebPage.route) },
                onNavigateToDocumentUpload = { navController.navigate(Screen.DocumentUpload.route) }
            )
        }
        composable(Screen.Recording.route) {
            RecordingScreen(
                viewModel = viewModel,
                onStopRecording = { navController.popBackStack() },
                onHighlight = { /* TODO: highlight moment */ }
            )
        }
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
            NoteDetailScreen(
                noteId = noteId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.VoiceCommand.route) {
            VoiceCommandOverlay(
                viewModel = viewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(Screen.UploadAudio.route) {
            UploadAudioScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ImageCapture.route) {
            ImageCaptureScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.UploadImage.route) {
            UploadImageScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TypeText.route) {
            TypeTextScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.VideoUrl.route) {
            VideoUrlScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.WebPage.route) {
            WebPageScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DocumentUpload.route) {
            DocumentUploadScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
