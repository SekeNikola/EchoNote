package com.example.app.viewmodel

import androidx.lifecycle.*
import com.example.app.data.Note
import com.example.app.data.NoteRepository
import kotlinx.coroutines.launch

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.app.audio.AudioRecorder
import com.example.app.audio.getAudioFileForUpload
import com.example.app.network.GPTRequest
import com.example.app.network.Message
import com.example.app.network.RetrofitInstance
import com.example.app.worker.ReminderScheduler
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.Locale
import com.example.app.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class NoteViewModel(private val repository: NoteRepository, app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {
    val notes = repository.getAllNotes().asLiveData()
    val searchQuery = MutableLiveData("")
    val isRecording = MutableLiveData(false)
    val liveTranscript = MutableLiveData("")
    val isVoiceOverlayVisible = MutableLiveData(false)
    val aiResponse = MutableLiveData("")
    val amplitude = MutableLiveData(0)

    private var audioRecorder: AudioRecorder? = null
    private var currentAudioPath: String? = null
    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false

    init {
        tts = TextToSpeech(app.applicationContext, this)
        audioRecorder = AudioRecorder(app.applicationContext)
        audioRecorder?.setOnAmplitudeListener(object : AudioRecorder.AmplitudeListener {
            override fun onAmplitude(amplitudeValue: Int) {
                amplitude.postValue(amplitudeValue)
            }
        })
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        repository.searchNotes(query)
    }

    fun toggleFavorite(note: Note) = viewModelScope.launch {
        repository.toggleFavorite(note)
    }

    fun getNoteById(id: Long) = repository.getNoteById(id).asLiveData()

    fun updateNoteTitle(id: Long, title: String) = viewModelScope.launch {
        repository.updateNoteTitle(id, title)
    }

    fun readAloud(id: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            note.collect { n ->
                n?.let {
                    if (ttsReady) {
                        tts?.speak(it.transcript.ifBlank { it.snippet }, TextToSpeech.QUEUE_FLUSH, null, "note_$id")
                    }
                }
            }
        }
    }
    // TextToSpeech init callback
    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onCleared() {
        tts?.shutdown()
        audioRecorder?.stopRecording() // Clean up recorder
        super.onCleared()
    }

    // --- Offline sync logic ---
    fun syncNotesIfOnline() = viewModelScope.launch {
        val context = getApplication<Application>().applicationContext
        if (NetworkUtils.isOnline(context)) {
            // TODO: Query unsynced notes from DB
            // For each unsynced note:
            // 1. Upload to server (if you have a backend)
            // 2. Mark as synced in DB
        }
    }

    fun markNoteForSync(noteId: Long) = viewModelScope.launch {
        // TODO: Mark note as needing sync in DB (e.g., set a flag)
    }

    fun setReminder(id: Long, timeMillis: Long? = null) = viewModelScope.launch {
        val note = repository.getNoteById(id)
        note.collect { n ->
            n?.let {
                val context = getApplication<Application>().applicationContext
                val delay = timeMillis ?: (n.reminderTime ?: 0) - System.currentTimeMillis()
                ReminderScheduler.scheduleReminder(context, n.id, n.title, delay)
            }
        }
    }

    fun archiveNote(id: Long) = viewModelScope.launch {
        repository.archiveNote(id)
    }

    // AUDIO + AI
    fun startRecording(context: Context) {
        val appDir = getApplication<Application>().applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (appDir == null) {
            Log.e("NoteViewModel", "Failed to get external files directory for audio recording.")
            isRecording.value = false
            liveTranscript.value = "Error: Cannot access storage for audio recording."
            return
        }
        val audioFile = File(appDir, "audio_record_${System.currentTimeMillis()}.m4a")
        currentAudioPath = audioFile.absolutePath

        val started = audioRecorder?.startRecording(audioFile) ?: false
        if (started) {
            isRecording.value = true
            liveTranscript.value = ""
        } else {
            isRecording.value = false
            liveTranscript.value = "Error: Failed to start audio recording. Please check permissions and storage."
        }
    }

    fun stopRecordingAndTranscribe() = viewModelScope.launch {
        isRecording.value = false
        audioRecorder?.stopRecording()

        val path = currentAudioPath
        var transcript = ""
        var summary = ""
        var noteTitle = "Voice Note"
        var noteSnippet = ""
        if (path == null) {
            Log.e("NoteViewModel", "currentAudioPath is null, cannot transcribe.")
            liveTranscript.value = "Error: No audio file path."
        } else {
            val audioFile = File(path)
            if (!audioFile.exists() || audioFile.length() == 0L) {
                Log.e("NoteViewModel", "Audio file missing or empty: $path")
                liveTranscript.value = "Error: No audio was recorded. File missing or empty."
            } else {
                try {
                    transcript = uploadAndTranscribe(path)
                    liveTranscript.value = transcript
                    summary = summarizeTranscript(transcript)
                    noteTitle = summary.ifBlank { "Voice Note" }
                    noteSnippet = summary
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Transcription failed", e)
                }
            }
        }
        val note = com.example.app.data.Note(
            title = noteTitle,
            snippet = noteSnippet,
            transcript = transcript,
            audioPath = path ?: "",
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.noteDao.insert(note)
        }
    }

    private suspend fun uploadAndTranscribe(path: String): String = withContext(Dispatchers.IO) {
        val file = getAudioFileForUpload(path) // This function expects a path String
        val reqFile = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, reqFile)
        val response = RetrofitInstance.api.transcribeAudio(body)
        response.body()?.text ?: ""
    }

    private suspend fun summarizeTranscript(transcript: String): String = withContext(Dispatchers.IO) {
        val req = GPTRequest(
            messages = listOf(
                Message(role = "system", content = "Summarize this note in 1-2 sentences."),
                Message(role = "user", content = transcript)
            )
        )
        val response = RetrofitInstance.api.summarizeText(req)
        response.body()?.choices?.firstOrNull()?.message?.content ?: ""
    }

    // Voice command overlay logic
    fun showVoiceOverlay(query: String) = viewModelScope.launch {
        isVoiceOverlayVisible.value = true
        val req = GPTRequest(
            messages = listOf(
                Message(role = "system", content = "You are a helpful voice note assistant."),
                Message(role = "user", content = query)
            )
        )
        val response = RetrofitInstance.api.summarizeText(req)
        aiResponse.value = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
    }

    fun hideVoiceOverlay() {
        isVoiceOverlayVisible.value = false
        aiResponse.value = ""
    }
}
