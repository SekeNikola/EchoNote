
package com.example.app.ui
import com.example.app.data.createdAtFormattedDate
import com.example.app.data.createdAtFormattedTime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.app.viewmodel.NoteViewModel
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

enum class PlaybackState { Idle, Playing, Paused }

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val note by viewModel.getNoteById(noteId).observeAsState()
    val title = note?.title ?: ""
    val transcript = note?.transcript ?: ""
    val audioPath = note?.audioPath
    val context = LocalContext.current
    val date = note?.createdAtFormattedDate() ?: ""
    val time = note?.createdAtFormattedTime() ?: ""
    val transcriptScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp) // leave space for sticky actions
        ) {
        // Top bar: Back arrow and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (note != null) {
                    BasicTextField(
                        value = note!!.title,
                        onValueChange = { viewModel.updateNoteTitle(note!!.id, it) },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }
        }
        // Date and time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = time,
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
        }
    Spacer(modifier = Modifier.height(16.dp))
        // Summary section
        Text(
            text = "Summary",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (note?.snippet?.isNotBlank() == true) {
            Text(
                text = note!!.snippet,
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Transcript section
        Text(
            text = "Transcript",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (transcript.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Scrollable transcript with custom scrollbar
                Box(Modifier.fillMaxSize()) {
                    val scrollState = transcriptScrollState
                    Text(
                        text = transcript,
                        color = Color(0xFFB0B0B0),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .background(Color(0xFF181818))
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    // Custom vertical scrollbar
                    if (scrollState.maxValue > 0) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 2.dp)
                        ) {
                            val proportion = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                            val visibleRatio = size.height / (size.height + scrollState.maxValue)
                            val scrollbarHeight = size.height * visibleRatio
                            val scrollbarTop = (size.height - scrollbarHeight) * proportion
                            drawRoundRect(
                                color = Color.Gray.copy(alpha = 0.5f),
                                topLeft = Offset(x = 0f, y = scrollbarTop),
                                size = androidx.compose.ui.geometry.Size(width = size.width, height = scrollbarHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(120.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        // No Spacer.weight(1f) here, so actions stay sticky
    }
        // Sticky action bar
        Row(
            modifier = Modifier
                .then(androidx.compose.ui.Modifier.align(Alignment.BottomCenter))
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.readAloud(noteId) }) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = "Read Aloud", tint = Color.White)
                }
                Text("Read", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.setReminder(noteId) }) {
                    Icon(Icons.Outlined.Schedule, contentDescription = "Set Reminder", tint = Color.White)
                }
                Text("Remind", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.archiveNote(noteId) }) {
                    Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = Color.White)
                }
                Text("Archive", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.toggleFavorite(note!!) }) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (note?.isFavorite == true) Color(0xFFFFC107) else Color.White
                    )
                }
                Text("Star", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { note?.let { viewModel.deleteNote(it.id); onBack() } }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                }
                Text("Delete", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}