package com.example.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

enum class VoiceAssistantState {
    IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
}

data class VoiceCommand(
    val type: VoiceCommandType,
    val content: String,
    val parameters: Map<String, String> = emptyMap()
)

enum class VoiceCommandType {
    CREATE_TASK, CREATE_NOTE, SET_REMINDER, SEARCH, READ_TASKS, UNKNOWN
}

@Composable
fun VoiceAssistantButton(
    onVoiceCommand: (VoiceCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(VoiceAssistantState.IDLE) }
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    // TTS initialization
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }
    
    // Speech recognizer
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(speechRecognizer, context) { newState, text, error ->
                state = newState
                recognizedText = text
                errorMessage = error
                
                if (newState == VoiceAssistantState.PROCESSING && text.isNotEmpty()) {
                    val command = parseVoiceCommand(text)
                    onVoiceCommand(command)
                    
                    // Provide audio feedback
                    val response = generateResponse(command)
                    tts?.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
                    state = VoiceAssistantState.SPEAKING
                    
                    // Return to idle after speaking
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000) // Approximate speaking time
                        state = VoiceAssistantState.IDLE
                    }
                }
            }
        } else {
            state = VoiceAssistantState.ERROR
            errorMessage = "Microphone permission required"
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Voice button with animation
        VoiceButton(
            state = state,
            onClick = {
                when (state) {
                    VoiceAssistantState.IDLE -> {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    VoiceAssistantState.LISTENING -> {
                        speechRecognizer.stopListening()
                        state = VoiceAssistantState.IDLE
                    }
                    VoiceAssistantState.SPEAKING -> {
                        tts?.stop()
                        state = VoiceAssistantState.IDLE
                    }
                    else -> {
                        state = VoiceAssistantState.IDLE
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status text
        Text(
            text = when (state) {
                VoiceAssistantState.IDLE -> "Tap to speak"
                VoiceAssistantState.LISTENING -> "Listening..."
                VoiceAssistantState.PROCESSING -> "Processing..."
                VoiceAssistantState.SPEAKING -> "Speaking..."
                VoiceAssistantState.ERROR -> errorMessage
            },
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (state == VoiceAssistantState.ERROR) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(max = 120.dp)
        )
        
        // Recognized text preview
        if (recognizedText.isNotEmpty() && state == VoiceAssistantState.PROCESSING) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.widthIn(max = 200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "\"$recognizedText\"",
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceButton(
    state: VoiceAssistantState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == VoiceAssistantState.LISTENING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == VoiceAssistantState.LISTENING) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        containerColor = when (state) {
            VoiceAssistantState.IDLE -> MaterialTheme.colorScheme.primary
            VoiceAssistantState.LISTENING -> MaterialTheme.colorScheme.tertiary
            VoiceAssistantState.PROCESSING -> MaterialTheme.colorScheme.secondary
            VoiceAssistantState.SPEAKING -> MaterialTheme.colorScheme.secondary
            VoiceAssistantState.ERROR -> MaterialTheme.colorScheme.error
        }
    ) {
        Icon(
            imageVector = when (state) {
                VoiceAssistantState.LISTENING -> Icons.Default.Stop
                VoiceAssistantState.SPEAKING -> Icons.Default.MicOff
                VoiceAssistantState.ERROR -> Icons.Default.MicOff
                else -> Icons.Default.Mic
            },
            contentDescription = "Voice Assistant",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha)
        )
    }
}

private fun startListening(
    speechRecognizer: SpeechRecognizer,
    context: Context,
    onStateChange: (VoiceAssistantState, String, String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your command...")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onStateChange(VoiceAssistantState.LISTENING, "", "")
        }
        
        override fun onBeginningOfSpeech() {
            onStateChange(VoiceAssistantState.LISTENING, "", "")
        }
        
        override fun onRmsChanged(rmsdB: Float) {}
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            onStateChange(VoiceAssistantState.PROCESSING, "", "")
        }
        
        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Speech recognition error"
            }
            onStateChange(VoiceAssistantState.ERROR, "", errorMsg)
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: ""
            onStateChange(VoiceAssistantState.PROCESSING, recognizedText, "")
        }
        
        override fun onPartialResults(partialResults: Bundle?) {}
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    
    speechRecognizer.startListening(intent)
}

private fun parseVoiceCommand(text: String): VoiceCommand {
    val lowerText = text.lowercase().trim()
    
    return when {
        lowerText.contains("create task") || lowerText.contains("add task") || lowerText.contains("new task") -> {
            val content = lowerText.replace(Regex("(create|add|new)\\s+task\\s*"), "").trim()
            VoiceCommand(VoiceCommandType.CREATE_TASK, content)
        }
        
        lowerText.contains("create note") || lowerText.contains("add note") || lowerText.contains("new note") -> {
            val content = lowerText.replace(Regex("(create|add|new)\\s+note\\s*"), "").trim()
            VoiceCommand(VoiceCommandType.CREATE_NOTE, content)
        }
        
        lowerText.contains("remind me") || lowerText.contains("set reminder") -> {
            val content = lowerText.replace(Regex("(remind me|set reminder)\\s*(to|about)?\\s*"), "").trim()
            VoiceCommand(VoiceCommandType.SET_REMINDER, content)
        }
        
        lowerText.contains("search") || lowerText.contains("find") -> {
            val content = lowerText.replace(Regex("(search|find)\\s*(for)?\\s*"), "").trim()
            VoiceCommand(VoiceCommandType.SEARCH, content)
        }
        
        lowerText.contains("read") && (lowerText.contains("task") || lowerText.contains("todo")) -> {
            VoiceCommand(VoiceCommandType.READ_TASKS, lowerText)
        }
        
        else -> VoiceCommand(VoiceCommandType.UNKNOWN, text)
    }
}

private fun generateResponse(command: VoiceCommand): String {
    return when (command.type) {
        VoiceCommandType.CREATE_TASK -> {
            if (command.content.isNotEmpty()) {
                "Task created: ${command.content}"
            } else {
                "Please specify what task you'd like to create"
            }
        }
        
        VoiceCommandType.CREATE_NOTE -> {
            if (command.content.isNotEmpty()) {
                "Note created: ${command.content}"
            } else {
                "Please specify what note you'd like to create"
            }
        }
        
        VoiceCommandType.SET_REMINDER -> {
            if (command.content.isNotEmpty()) {
                "Reminder set for: ${command.content}"
            } else {
                "Please specify what you'd like to be reminded about"
            }
        }
        
        VoiceCommandType.SEARCH -> {
            if (command.content.isNotEmpty()) {
                "Searching for: ${command.content}"
            } else {
                "What would you like to search for?"
            }
        }
        
        VoiceCommandType.READ_TASKS -> "Reading your tasks"
        
        VoiceCommandType.UNKNOWN -> "I didn't understand that command. Try saying 'create task', 'add note', or 'set reminder'"
    }
}