
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
}

@Composable
fun EchoNoteNavGraph(
    navController: NavHostController,
    viewModel: NoteViewModel
) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNoteClick = { noteId -> navController.navigate(Screen.NoteDetail(noteId).createRoute(noteId)) },
                onRecordClick = { navController.navigate(Screen.Recording.route) }
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
    }
}
