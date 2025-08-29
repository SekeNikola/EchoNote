
package com.example.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.NoteViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun RecordingScreen(
    viewModel: NoteViewModel,
    onStopRecording: () -> Unit,
    onHighlight: () -> Unit
) {
    val isRecording by viewModel.isRecording.observeAsState(false)
    val liveTranscript by viewModel.liveTranscript.observeAsState("")
    val context = LocalContext.current
    val amplitude by viewModel.amplitude.observeAsState(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recording indicator
            if (isRecording) {
                // Animate mic icon size based on real amplitude
                val minSize = 24f
                val maxSize = 64f
                val norm = (amplitude / 32767f).coerceIn(0f, 1f)
                val size = minSize + (maxSize - minSize) * norm
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .background(Color.Red, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = if (isRecording) "Recording..." else "Ready to record",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = liveTranscript,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .background(Color(0xFF1F1F1F))
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
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
                            viewModel.stopRecordingAndTranscribe()
                            onStopRecording()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                    ) {
                        Text("Stop", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.startRecording(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                    ) {
                        Text("Start", color = Color.White)
                    }
                }
            }
        }
    }
}
