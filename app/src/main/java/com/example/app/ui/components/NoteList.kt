package com.example.app.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.app.data.Note

@Composable
fun NoteList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onFavorite: (Note) -> Unit
) {
    LazyColumn {
        items(notes) { note ->
            NoteCard(note = note, onClick = { onNoteClick(note.id) }, onFavorite = { onFavorite(note) })
        }
    }
}
