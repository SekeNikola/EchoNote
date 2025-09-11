package com.example.app.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app.R
import com.example.app.viewmodel.NoteViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVoiceScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val isListening by viewModel.isListening.collectAsState()
    val voiceText by viewModel.voiceText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isSpeaking = false // Remove reference to missing isSpeaking
    val context = LocalContext.current
    
    // Auto-start listening when screen opens and start new session
    LaunchedEffect(Unit) {
        viewModel.clearVoiceSession() // Clear previous conversation
        viewModel.startListening(context)
    }
    
    // Cleanup when leaving screen
    // No endVoiceSession needed
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "Voice Assistant",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            IconButton(
                onClick = { navController.navigate("ai_chat") }
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color(0xFFFF8C00),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Enhanced Voice Orb with Pulsing Animation
        VoiceOrb(
            isListening = isListening,
            isProcessing = isProcessing,
            isSpeaking = isSpeaking,
            onClick = {
                if (isListening) {
                    viewModel.stopListening()
                    navController.navigateUp()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Removed auto-restart listening to allow for proper conversation flow
        // Users can manually tap to continue the conversation
        
        // Action Buttons - simplified
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isListening || isProcessing) {
                OutlinedButton(
                    onClick = { 
                        viewModel.stopListening()
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun VoiceOrb(
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    LottieVoiceOrb(
        isListening = isListening,
        isProcessing = isProcessing,
        isSpeaking = isSpeaking,
        onClick = onClick
    )
}

@Composable
fun LottieVoiceOrb(
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load the Lottie composition from raw resources
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.orb)
    )
    
    // Animation state based on current mode
    val animationState = when {
        isSpeaking -> Int.MAX_VALUE // Continuous animation when AI is speaking
        isProcessing -> Int.MAX_VALUE // Continuous animation when processing
        isListening -> Int.MAX_VALUE // Continuous animation when listening
        else -> 1 // Single iteration when idle (Lottie requires positive number)
    }
    
    // Animation speed based on state
    val animationSpeed = when {
        isSpeaking -> 2.0f // Fastest when AI is speaking
        isProcessing -> 1.5f // Faster when processing
        isListening -> 1.0f // Normal speed when listening
        else -> 0.5f // Slower when idle
    }
    
    // Pulsing scale animation for speaking/talking state
    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            isSpeaking -> 1.15f // Larger pulse when AI is speaking
            isListening || isProcessing -> 1.1f
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSpeaking) 600 else 800, // Faster pulse when speaking
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier
            .size(200.dp)
            .scale(pulseScale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Lottie animation - only show if composition is loaded
        composition?.let {
            LottieAnimation(
                composition = it,
                iterations = animationState,
                speed = animationSpeed,
                modifier = Modifier
                    .size(180.dp)
                    .fillMaxSize()
            )
        } ?: run {
            // Fallback: Show a simple circle if Lottie fails to load
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = when {
                            isSpeaking -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6), // Blue for speaking
                                    Color(0xFF1E40AF)
                                )
                            )
                            isProcessing -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B), // Orange for processing
                                    Color(0xFFD97706)
                                )
                            )
                            isListening -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981), // Green for listening
                                    Color(0xFF059669)
                                )
                            )
                            else -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF8C00), // Purple for idle
                                    Color(0xFFFF7F00)
                                )
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isSpeaking -> Icons.Default.RecordVoiceOver // Speaking icon
                        isProcessing -> Icons.Default.Psychology // Brain icon for thinking
                        isListening -> Icons.Default.Mic
                        else -> Icons.Default.MicNone
                    },
                    contentDescription = when {
                        isSpeaking -> "AI is speaking"
                        isProcessing -> "Processing"
                        isListening -> "Listening"
                        else -> "Start listening"
                    },
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Optional: Add a subtle overlay for different states
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Color.Blue.copy(alpha = 0.15f), // Blue tint when AI is speaking
                        shape = CircleShape
                    )
            )
        } else if (isProcessing) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Color(0xFFF59E0B).copy(alpha = 0.1f), // Orange tint when processing
                        shape = CircleShape
                    )
            )
        } else if (isListening) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Color.Green.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }
}

fun drawWaveform(phase: Float, drawScope: DrawScope) {
    val width = drawScope.size.width
    val height = drawScope.size.height
    val centerY = height / 2
    
    with(drawScope) {
        val waveLength = width / 8
        val amplitude = height / 8
        
        repeat(8) { i ->
            val x = (i * waveLength) + (phase / 360f * waveLength)
            val y = centerY + sin((x / waveLength + phase / 60f) * 2 * Math.PI).toFloat() * amplitude
            
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
