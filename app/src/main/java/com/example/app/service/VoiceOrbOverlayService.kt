package com.example.app.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceOrbOverlayService : Service() {
    
    companion object {
        const val TAG = "VoiceOrbOverlayService"
        const val ACTION_SHOW_ORB = "com.example.app.SHOW_VOICE_ORB"
        const val ACTION_HIDE_ORB = "com.example.app.HIDE_VOICE_ORB"
        
        fun showOrb(context: Context) {
            if (canDrawOverlays(context)) {
                val intent = Intent(context, VoiceOrbOverlayService::class.java).apply {
                    action = ACTION_SHOW_ORB
                }
                context.startService(intent)
            } else {
                Log.w(TAG, "Cannot draw overlays - permission not granted")
            }
        }
        
        fun hideOrb(context: Context) {
            val intent = Intent(context, VoiceOrbOverlayService::class.java).apply {
                action = ACTION_HIDE_ORB
            }
            context.startService(intent)
        }
        
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }
    
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var hideJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VoiceOrbOverlayService started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_SHOW_ORB -> showOrbOverlay()
            ACTION_HIDE_ORB -> hideOrbOverlay()
        }
        
        return START_NOT_STICKY
    }
    
    private fun showOrbOverlay() {
        if (!canDrawOverlays(this)) {
            Log.w(TAG, "Cannot show overlay - permission not granted")
            return
        }
        
        // Hide existing overlay if any
        hideOrbOverlay()
        
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // Create the overlay view using Compose
            overlayView = ComposeView(this).apply {
                setContent {
                    VoiceOrbAnimation()
                }
            }
            
            // Set up window parameters
            val layoutParams = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.CENTER
                x = 0
                y = 0
            }
            
            // Add the view to window manager
            windowManager?.addView(overlayView, layoutParams)
            
            Log.d(TAG, "Voice orb overlay shown")
            
            // Start speech recognition
            startSpeechRecognition()
            
            // Auto-hide after 10 seconds (give more time for speech)
            hideJob = CoroutineScope(Dispatchers.Main).launch {
                delay(10000)
                hideOrbOverlay()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing voice orb overlay", e)
            stopSelf()
        }
    }
    
    private fun hideOrbOverlay() {
        try {
            hideJob?.cancel()
            
            // Stop speech recognition
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            
            overlayView?.let { view ->
                windowManager?.removeView(view)
                overlayView = null
            }
            
            windowManager = null
            Log.d(TAG, "Voice orb overlay hidden")
            stopSelf()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding voice orb overlay", e)
        }
    }
    
    private fun startSpeechRecognition() {
        try {
            // Check for audio permission
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Audio recording permission not granted")
                hideOrbOverlay()
                return
            }
            
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                Log.w(TAG, "Speech recognition not available")
                return
            }
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Could update orb intensity based on volume
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                }
                
                override fun onError(error: Int) {
                    Log.e(TAG, "Speech recognition error: $error")
                    hideOrbOverlay()
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        Log.d(TAG, "Speech result: $spokenText")
                        handleSpeechResult(spokenText)
                    }
                    hideOrbOverlay()
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Speech recognition started")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            hideOrbOverlay()
        }
    }
    
    private fun handleSpeechResult(spokenText: String) {
        Log.d(TAG, "Processing speech: $spokenText")
        
        // For now, just log the result
        // In the future, you could:
        // 1. Send the text to your AI assistant API
        // 2. Create a note/task based on the speech
        // 3. Perform voice commands
        
        // Example: Create a quick note
        try {
            // You could broadcast the speech result to be handled by the app
            val broadcastIntent = Intent("com.example.app.SPEECH_RESULT").apply {
                putExtra("speech_text", spokenText)
            }
            sendBroadcast(broadcastIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling speech result", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hideOrbOverlay()
    }
}

@Composable
fun VoiceOrbAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2196F3),
                        Color(0xFF9C27B0)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice Assistant Active",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}