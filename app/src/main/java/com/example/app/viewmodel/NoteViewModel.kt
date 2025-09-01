package com.example.app.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.app.data.Note
import com.example.app.data.NoteRepository
import kotlinx.coroutines.launch
import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.app.audio.AudioRecorder
import com.example.app.audio.CompressedAudioRecorder
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
    /**
     * Use OpenAI GPT to extract summary and tasks from transcript.
     * The prompt asks for a JSON response: {"summary": "...", "tasks": [ ... ]}
     */
    suspend fun extractSummaryAndTasksWithOpenAI(transcript: String): Pair<String, List<String>>? {
        val prompt = """
            Summarize the following text and extract any tasks as a checklist. Respond in JSON: {\"summary\": \"...\", \"tasks\": [ ... ]}\n\nText:\n$transcript
        """.trimIndent()
        val request = com.example.app.network.GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                com.example.app.network.Message(role = "system", content = "You are a helpful assistant that summarizes notes and extracts tasks as a checklist."),
                com.example.app.network.Message(role = "user", content = prompt)
            )
        )
        return try {
            val response = com.example.app.network.RetrofitInstance.api.summarizeText(request)
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    val json = org.json.JSONObject(content)
                    val summary = json.optString("summary", "")
                    val tasks = if (json.has("tasks")) {
                        val arr = json.getJSONArray("tasks")
                        List(arr.length()) { arr.getString(it) }
                    } else emptyList()
                    Pair(summary, tasks)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
    fun updateNoteSnippet(noteId: Long, snippet: String) = viewModelScope.launch {
        repository.updateNoteSnippet(noteId, snippet)
    }

    fun updateChecklistState(noteId: Long, checklistState: String) = viewModelScope.launch {
        repository.updateChecklistState(noteId, checklistState)
    }
    private val compressedAudioRecorder = CompressedAudioRecorder(app.applicationContext)
    private var compressedAudioFile: File? = null
    fun stopAndSaveNote() {
        isRecording.value = false
        speechRecognizer?.stopListening()
        val transcript = fullTranscript.value
        if (transcript.isNotBlank()) {
            viewModelScope.launch {
                val result = extractSummaryAndTasksWithOpenAI(transcript)
                val summaryOut = result?.first ?: ""
                val tasks = result?.second ?: emptyList<String>()
                val json = org.json.JSONObject()
                json.put("summary", summaryOut)
                if (tasks.isNotEmpty()) json.put("tasks", org.json.JSONArray(tasks))
                val title = generateTitle(summaryOut)
                val note = Note(
                    title = title,
                    snippet = json.toString(),
                    transcript = transcript
                )
                repository.noteDao.insert(note)
            }
        }
        // Compress and send audio to OpenAI after recording
        compressAndSendAudioToOpenAI()
        _fullTranscript.value = ""
        summary.value = ""
    }

    private fun compressAndSendAudioToOpenAI() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Assume you have a raw audio file path (e.g., from AudioRecorder or elsewhere)
                val rawAudioPath = getRawAudioFilePath()
                if (rawAudioPath != null) {
                    val rawFile = File(rawAudioPath)
                    val compressedFile = File(rawFile.parent, rawFile.nameWithoutExtension + "_compressed.m4a")
                    val compressed = compressedAudioRecorder.startRecording(compressedFile)
                    // Simulate compression by copying or re-encoding (if needed)
                    // For this example, assume startRecording does the job
                    compressedAudioRecorder.stopRecording()
                    compressedAudioFile = compressedFile
                    // Send to OpenAI
                    sendCompressedAudioToOpenAI(compressedFile)
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Compression/Send failed", e)
            }
        }
    }

    private suspend fun sendCompressedAudioToOpenAI(file: File) {
        try {
            val requestFile = file.asRequestBody("audio/mp4".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            // Call your OpenAIService here (replace with your actual API call)
            // Example: val response = RetrofitInstance.api.transcribeAudio(body)
            // Handle response as needed
        } catch (e: Exception) {
            Log.e("NoteViewModel", "OpenAI audio send failed", e)
        }
    }

    private fun getRawAudioFilePath(): String? {
        // TODO: Implement logic to get the path to the raw audio file just recorded
        // This may depend on your AudioRecorder implementation
        return null
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
    val summary = MutableLiveData("")
    val isVoiceOverlayVisible = MutableLiveData(false)
    val aiResponse = MutableLiveData("")
    val amplitude = MutableLiveData(0)

    // New transcript handling
    private val _fullTranscript = MutableStateFlow("")
    val fullTranscript: StateFlow<String> = _fullTranscript

    fun appendTranscript(newText: String, isFinal: Boolean) {
        _fullTranscript.update { current ->
            if (isFinal) {
                (current + " " + newText).trim()
            } else {
                current
            }
        }
    }

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
    _fullTranscript.value = ""
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
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                data?.firstOrNull()?.let { finalResult ->
                    appendTranscript(finalResult, isFinal = true)
                                        // Generate summary using OpenAI API, always as JSON
                                        viewModelScope.launch {
                                                val rules = """
You are an assistant that reformats transcripts into JSON for a note-taking app.

Rules:
1. If the transcript contains action items like 'I need to...', 'remind me to...', 'buy...', 'call...', 'go to...', or similar, ALWAYS extract them into a JSON array called 'tasks'.
2. If the transcript has general conversation or thoughts but no clear tasks, return a JSON object with a single 'summary' field.
3. The JSON must be valid and contain ONLY the JSON object — no explanations, no extra text.
4. Do NOT skip any tasks mentioned.
5. Each task must be a short, clear string.

Examples:

Input:
"I need to buy bread and milk and also call mom."

Output:
{
    \"tasks\": [\"Buy bread\", \"Buy milk\", \"Call mom\"]
}

Input:
"Today I met John and we discussed the project timeline."

Output:
{
    \"summary\": \"Met John and discussed the project timeline.\"
}
""".trimIndent()
                                                val req = GPTRequest(
                                                        messages = listOf(
                                                                Message(role = "system", content = rules),
                                                                Message(role = "user", content = _fullTranscript.value)
                                                        )
                                                )
                                                val response = RetrofitInstance.api.summarizeText(req)
                                                summary.value = response.body()?.choices?.firstOrNull()?.message?.content?.trim() ?: ""
                                        }
                }
                if (isRecording.value == true) {
                    speechRecognizer?.startListening(createRecognizerIntent())
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                data?.firstOrNull()?.let { partial ->
                    appendTranscript(partial, isFinal = false)
                }
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
    _fullTranscript.value = ""
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
