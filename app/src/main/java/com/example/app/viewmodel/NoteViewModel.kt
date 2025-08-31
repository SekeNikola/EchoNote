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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.speech.SpeechRecognizer
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.os.Bundle

class NoteViewModel(private val repository: NoteRepository, app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {
    fun stopAndSaveNote() {
        isRecording.value = false
        speechRecognizer?.stopListening()
        val transcript = liveTranscript.value ?: ""
        val summaryText = summary.value ?: ""
        if (transcript.isNotBlank()) {
            viewModelScope.launch {
                val title = generateTitle(summaryText)
                val note = Note(
                    title = title,
                    snippet = summaryText,
                    transcript = transcript
                )
                repository.noteDao.insert(note)
            }
        }
        liveTranscript.value = ""
        summary.value = ""
    }
    fun setReminder(noteId: Long) {
        // TODO: Implement reminder logic
    }

    fun deleteNote(id: Long) = viewModelScope.launch {
        repository.noteDao.deleteById(id)
    }
    val notes = repository.getAllNotes().asLiveData()
    val searchQuery = MutableLiveData("")
    val isRecording = MutableLiveData(false)
    val liveTranscript = MutableLiveData("")
    val summary = MutableLiveData("")
    val isVoiceOverlayVisible = MutableLiveData(false)
    val aiResponse = MutableLiveData("")
    val amplitude = MutableLiveData(0)

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    suspend fun generateTitle(summary: String): String = withContext(Dispatchers.IO) {
        val prompt = "Generate a concise, relevant title for this note: $summary"
        val request = GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(Message(role = "user", content = prompt))
        )
        val response = RetrofitInstance.api.summarizeText(request)
        response.body()?.choices?.firstOrNull()?.message?.content?.trim('"', '\n', ' ', '.') ?: "Untitled"
    }
    init {
        tts = TextToSpeech(app.applicationContext, this)
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
        speechRecognizer?.destroy()
        super.onCleared()
    }

    fun archiveNote(id: Long) = viewModelScope.launch {
        repository.archiveNote(id)
    }

    // AUDIO + AI
    fun startSpeechRecognition(context: Context) {
        // Clear transcript and summary at start
        liveTranscript.value = ""
        summary.value = ""
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                if (isRecording.value == true) {
                    speechRecognizer?.startListening(createRecognizerIntent())
                }
            }
            override fun onError(error: Int) {
                if (isRecording.value == true) {
                    speechRecognizer?.startListening(createRecognizerIntent())
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.joinToString(" ") ?: ""
                // Only set transcript to avoid duplication
                liveTranscript.value = transcript
                summary.value = generateSummary(transcript)
                if (isRecording.value == true) {
                    speechRecognizer?.startListening(createRecognizerIntent())
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.joinToString(" ") ?: ""
                // Only show partial, do not append to main transcript
                liveTranscript.value = partial
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(listener)
        isRecording.value = true
        speechRecognizer?.startListening(createRecognizerIntent())
    }

    private fun createRecognizerIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        return intent
    }
    private fun generateSummary(text: String): String {
        // Simple summary: first 10 words or less
        return text.split(" ").take(10).joinToString(" ") + if (text.split(" ").size > 10) "..." else ""
    }

    fun stopSpeechRecognition() {
        isRecording.value = false
        speechRecognizer?.stopListening()
        liveTranscript.value = ""
        summary.value = ""
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
