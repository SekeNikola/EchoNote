package com.example.app.ui

import androidx.compose.animation.core.*
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
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
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
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Status Text
        Text(
            text = when {
                isProcessing -> "Thinking..."
                isListening -> "I'm listening..."
                voiceText.isNotEmpty() && !isProcessing -> "Processing complete"
                else -> "Tap to start"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when {
                isProcessing -> "I'm preparing your response..."
                isListening -> "Speak now, I'm ready to help"
                voiceText.isNotEmpty() && !isProcessing -> "Great! I'll respond shortly"
                else -> "Tell me what you need help with"
            },
            fontSize = 14.sp,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Animated Voice Orb
        VoiceOrb(
            isListening = isListening,
            isProcessing = isProcessing,
            onClick = {
                if (!isProcessing) {
                    if (isListening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening(context)
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Voice Text Display
        if (voiceText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "You said:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = voiceText,
                        fontSize = 16.sp,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    
                    // Show processing or AI response status
                    if (isProcessing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "AI is thinking...",
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Auto-restart listening after processing (but only once per interaction)
        LaunchedEffect(isProcessing, voiceText) {
            if (!isProcessing && voiceText.isNotEmpty()) {
                // Wait a moment then restart listening for continuous conversation
                kotlinx.coroutines.delay(3000) // Wait 3 seconds
                if (!isListening && !isProcessing && voiceText.isNotEmpty()) {
                    // Clear the text and restart listening
                    viewModel.clearVoiceText()
                    kotlinx.coroutines.delay(500) // Small delay before restarting
                    viewModel.startListening(context)
                }
            }
        }
        
        // Action Buttons - simplified
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isListening || isProcessing) {
                OutlinedButton(
                    onClick = { 
                        viewModel.stopListening()
                        // Also clear the voice text if we're stopping
                        if (!isProcessing) {
                            viewModel.clearVoiceText()
                        }
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
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb")
    
    // Pulsing animation for listening
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Rotation animation for processing
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isProcessing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Wave animation for listening
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isListening) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Outer ripple rings for listening
        if (isListening) {
            repeat(3) { index ->
                val rippleScale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            delayMillis = index * 400,
                            easing = EaseOut
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple_$index"
                )
                
                val rippleAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000,
                            delayMillis = index * 400,
                            easing = EaseOut
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "ripple_alpha_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(rippleScale)
                        .background(
                            Color(0xFF8B5CF6).copy(alpha = rippleAlpha),
                            shape = CircleShape
                        )
                )
            }
        }
        
        // Wave visualization for listening
        if (isListening) {
            Canvas(
                modifier = Modifier.size(160.dp)
            ) {
                drawWaveform(wavePhase, this)
            }
        }
        
        // Main orb
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = when {
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
                                Color(0xFF8B5CF6), // Purple for idle
                                Color(0xFF3B82F6)
                            )
                        )
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isProcessing -> Icons.Default.Psychology // Brain icon for thinking
                    isListening -> Icons.Default.Mic
                    else -> Icons.Default.MicNone
                },
                contentDescription = when {
                    isProcessing -> "Processing"
                    isListening -> "Listening"
                    else -> "Start listening"
                },
                tint = Color.White,
                modifier = Modifier.size(48.dp)
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
