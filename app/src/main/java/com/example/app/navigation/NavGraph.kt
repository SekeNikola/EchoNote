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
    
    // New Simple version screens
    object AiChat : Screen("ai_chat")
    object AiVoice : Screen("ai_voice")
    object Tasks : Screen("tasks")
    object Notes : Screen("notes")
    object Chats : Screen("chats")
    object Settings : Screen("settings")
    object ExportImport : Screen("export_import")
    data class TaskDetail(val taskId: Long) : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }
    
    // Enhanced features
    object AllTasks : Screen("all_tasks")
    object EnhancedNoteCreation : Screen("enhanced_note_creation")
    object VoiceAssistantDemo : Screen("voice_assistant_demo")
}

@Composable
fun LogionNavGraph(
    navController: NavHostController,
    viewModel: NoteViewModel,
    startDestination: String = Screen.Home.route
) {
    NavHost(navController, startDestination = startDestination) {
        composable(Screen.Home.route) {
            SimpleHomeScreen(
                navController = navController,
                viewModel = viewModel,
                onAddNote = {
                    viewModel.createNote()
                }
            )
        }
        
        // AI Chat Screen
        composable(Screen.AiChat.route) {
            AiChatScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // AI Voice Screen
        composable(Screen.AiVoice.route) {
            AiVoiceScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Legacy screens for existing functionality
        composable(Screen.Recording.route) {
            RecordingScreen(
                viewModel = viewModel,
                onStopRecording = { navController.popBackStack() },
                onHighlight = { /* TODO: highlight moment */ }
            )
        }
        
        composable("noteDetail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "0"
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
        
        // New Simple version screens
        composable(Screen.Tasks.route) {
            TasksScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(Screen.Notes.route) {
            NotesListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(Screen.Chats.route) {
            ChatsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(Screen.ExportImport.route) {
            ExportImportScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
            TaskDetailScreen(
                navController = navController,
                viewModel = viewModel,
                taskId = taskId
            )
        }
        
        // Enhanced features
        composable(Screen.AllTasks.route) {
            AllTasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onTaskClick = { task ->
                    navController.navigate(Screen.TaskDetail(task.id).createRoute(task.id))
                }
            )
        }
        
        composable(Screen.EnhancedNoteCreation.route) {
            EnhancedNoteCreationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.VoiceAssistantDemo.route) {
            VoiceAssistantDemoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}