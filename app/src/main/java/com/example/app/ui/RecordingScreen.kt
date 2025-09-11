package com.example.app.ui

import android.Manifest
import android.os.Build
import kotlinx.coroutines.delay
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun RecordingScreen(
    viewModel: NoteViewModel,
    onStopRecording: () -> Unit,
    onHighlight: () -> Unit
) {
    val isRecording by viewModel.isRecording.observeAsState(false)
    val liveTranscript by viewModel.fullTranscript.collectAsState()
    val context = LocalContext.current
    val amplitude by viewModel.amplitude.observeAsState(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        // Transcript at the top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xFF1F1F1F))
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotCount = remember { mutableStateOf(1) }
                LaunchedEffect(isRecording) {
                    while (isRecording) {
                        dotCount.value = (dotCount.value % 3) + 1
                        delay(400)
                    }
                    dotCount.value = 1
                }
                val dots = (".".repeat(dotCount.value) + "   ").take(3)
                Text(
                    text = if (isRecording) liveTranscript + dots else liveTranscript,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Mic icon and recording text at the bottom, above actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp), // leave space for action buttons
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRecording) {
                val minSize = 24f
                val maxSize = 64f
                val norm = (amplitude / 32767f).coerceIn(0f, 1f)
                val size = minSize + (maxSize - minSize) * norm
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Recording",
                    tint = Color.White,
                    modifier = Modifier.size(size.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = if (isRecording) "Recording..." else "Ready to record",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Actions at the very bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onHighlight,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text("Highlight", color = Color.White)
            }
            if (isRecording) {
                Button(
                    onClick = {
                        viewModel.stopAndSaveNote()
                        onStopRecording()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                ) {
                    Text("Stop", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        // Clear transcript and summary before starting new recording
                        viewModel.stopSpeechRecognition()
                        viewModel.startSpeechRecognition(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                ) {
                    Text("Start", color = Color.White)
                }
            }
        }
    }
}


