


package com.example.app.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.app.data.Note
import com.example.app.data.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeout
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
import com.example.app.util.ApiKeyProvider
import com.example.app.utils.OpenAITTS
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.Locale
import com.example.app.util.NetworkUtils
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.ByteArrayOutputStream
import java.io.InputStream
import org.jsoup.Jsoup
import java.net.URL
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.content.Intent
import com.example.app.data.ChatMessage
import com.example.app.data.Task
import kotlinx.coroutines.flow.asStateFlow
import android.speech.RecognizerIntent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.os.Bundle
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import android.util.Base64

class NoteViewModel(private val repository: NoteRepository, app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {
    /**
     * Get an AI assistant response from OpenAI for the given transcript.
     */
    fun getAssistantResponse(transcript: String) = viewModelScope.launch {
        // Add user message to chat history with empty response
        _assistantChatHistory.update { it + (transcript to "") }
        try {
            // Get all notes (latest 10 for context) directly from Flow
            val notesList = repository.getAllNotes()
                .firstOrNull()?.take(10) ?: emptyList()
            val notesContext = if (notesList.isNotEmpty()) {
                notesList.joinToString("\n\n") { n ->
                    "Title: ${n.title}\nContent: ${n.transcript.ifBlank { n.snippet }}"
                }
            } else "No notes available."
            
            // Build conversation messages including history
            val messages = mutableListOf<Message>()
            messages.add(Message(role = "system", content = """
                You are a helpful AI assistant for Logion. You can:
                1. Answer general questions and provide information
                2. Help with the user's notes (provided below)
                3. Help create lists (shopping, grocery, travel, todo, etc.) when specifically requested
                
                IMPORTANT CONVERSATION GUIDELINES:
                - Focus on natural conversation first - don't rush to create notes or lists
                - Only create notes/lists when users explicitly ask or when they share substantial content to save
                - When helping with lists, build them step by step but let users decide when to save
                - Don't auto-save anything - let users control when content is saved
                - Be patient and let conversations develop naturally
                
                User's notes (for reference only):
                $notesContext
            """.trimIndent()))
            
            // Add conversation history (excluding the current message which has empty AI response)
            val currentHistory = _assistantChatHistory.value
            currentHistory.dropLast(1).forEach { (userMsg, aiMsg) ->
                messages.add(Message(role = "user", content = userMsg))
                if (aiMsg.isNotBlank()) {
                    messages.add(Message(role = "assistant", content = aiMsg))
                }
            }
            
            // Add current user message
            messages.add(Message(role = "user", content = transcript))
            
            val req = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(req)
            val aiText = response.body()?.choices?.firstOrNull()?.message?.content?.trim() ?: ""
            
            // Check for list completion and auto-save
            checkForListCompletion(transcript, aiText)
            
            // Update last chat entry with AI response
            _assistantChatHistory.update { history ->
                if (history.isNotEmpty())
                    history.dropLast(1) + (history.last().first to aiText)
                else history
            }
            aiResponse.value = aiText
        } catch (e: Exception) {
            val errorMsg = "Sorry, I couldn't get a response."
            _assistantChatHistory.update { history ->
                if (history.isNotEmpty())
                    history.dropLast(1) + (history.last().first to errorMsg)
                else history
            }
            aiResponse.value = errorMsg
        }
    }
    fun updateSummaryWithOpenAI(noteId: Long, transcript: String) = viewModelScope.launch {
        val result = extractSummaryAndTasksWithOpenAI(transcript)
        val summaryOut = result?.first ?: ""
        val tasks = result?.second ?: emptyList<String>()
        val json = org.json.JSONObject()
        json.put("summary", summaryOut)
        if (tasks.isNotEmpty()) json.put("tasks", org.json.JSONArray(tasks))
        // Save the new summary and tasks as snippet
        updateNoteSnippet(noteId, json.toString())
    }
    fun updateTranscript(noteId: Long, transcript: String) = viewModelScope.launch {
        repository.updateTranscript(noteId, transcript)
    }
    /**
     * Use OpenAI GPT to extract summary and tasks from transcript.
     * The prompt asks for a JSON response: {"summary": "...", "tasks": [ ... ]}
     */
    suspend fun extractSummaryAndTasksWithOpenAI(transcript: String): Pair<String, List<String>>? {
        Log.d("NoteViewModel", "=== EXTRACT SUMMARY START ===")
        Log.d("NoteViewModel", "Input transcript: '$transcript'")
        
        val prompt = """
            Summarize the following text and extract any tasks as a checklist. 
            IMPORTANT: Only extract actual tasks or items mentioned by the user, NOT instructions from the AI assistant.
            Ignore any AI responses that mention "save", "saving", "I'll save", etc.
            Focus only on real actionable items or things the user wants to do/get/remember.
            Respond in JSON: {\"summary\": \"...\", \"tasks\": [ ... ]}\n\nText:\n$transcript
        """.trimIndent()
        
        Log.d("NoteViewModel", "Generated prompt: '$prompt'")
        
        val request = com.example.app.network.GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                com.example.app.network.Message(role = "system", content = "You are a helpful assistant that summarizes notes and extracts tasks as a checklist. Only extract actual tasks or items mentioned by the user. Do not extract AI assistant responses or instructions like 'save note', 'I'll save this', etc. Focus on real actionable items."),
                com.example.app.network.Message(role = "user", content = prompt)
            )
        )
        
        Log.d("NoteViewModel", "Making API request...")
        
        return try {
            val response = com.example.app.network.RetrofitInstance.api.summarizeText(request)
            Log.d("NoteViewModel", "API response received. Success: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                Log.d("NoteViewModel", "Raw API response content: '$content'")
                
                if (!content.isNullOrBlank()) {
                    try {
                        // Clean the content by removing markdown code blocks
                        val cleanContent = content
                            .replace("```json", "")
                            .replace("```", "")
                            .trim()
                        
                        Log.d("NoteViewModel", "Cleaned JSON content: '$cleanContent'")
                        
                        val json = org.json.JSONObject(cleanContent)
                        var summary = json.optString("summary", "")
                        val tasks = if (json.has("tasks")) {
                            val arr = json.getJSONArray("tasks")
                            List(arr.length()) { arr.getString(it) }
                        } else emptyList()
                        
                        // Clean up summary by removing brackets at beginning and end
                        summary = cleanBracketsFromText(summary)
                        
                        Log.d("NoteViewModel", "Parsed summary: '$summary'")
                        Log.d("NoteViewModel", "Parsed tasks: $tasks")
                        Log.d("NoteViewModel", "=== EXTRACT SUMMARY SUCCESS ===")
                        
                        Pair(summary, tasks)
                    } catch (e: Exception) {
                        Log.e("NoteViewModel", "JSON parsing error", e)
                        null
                    }
                } else {
                    Log.w("NoteViewModel", "API response content was null or blank")
                    null
                }
            } else {
                Log.w("NoteViewModel", "API request failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== EXTRACT SUMMARY ERROR ===", e)
            null
        }
    }
    
    // Helper function to clean brackets and quotes from summaries
    private fun cleanBracketsFromText(text: String): String {
        var cleanText = text.trim()
        
        // Keep cleaning until no more changes occur (handle multiple layers)
        var previousText: String
        do {
            previousText = cleanText
            
            // Remove brackets
            if (cleanText.startsWith("[") && cleanText.endsWith("]")) {
                cleanText = cleanText.substring(1, cleanText.length - 1).trim()
            }
            // Also remove brackets if they appear with other characters
            cleanText = cleanText.replace(Regex("^\\[\\s*"), "").replace(Regex("\\s*\\]$"), "")
            
            // Remove quotes (single or double)
            if (cleanText.startsWith("\"") && cleanText.endsWith("\"")) {
                cleanText = cleanText.substring(1, cleanText.length - 1).trim()
            }
            if (cleanText.startsWith("'") && cleanText.endsWith("'")) {
                cleanText = cleanText.substring(1, cleanText.length - 1).trim()
            }
            
            // Remove various prefixes that might appear
            cleanText = cleanText.replace(Regex("^(?i)(summary|description|analysis|content)\\s*:?\\s*"), "")
            
            cleanText = cleanText.trim()
            
        } while (cleanText != previousText && cleanText.isNotEmpty())
        
        return cleanText
    }

    // Helper function to clean extracted OCR text for better AI processing
    private fun cleanExtractedText(text: String): String {
        return text
            .trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces/newlines with single space
            .replace(Regex("[^a-zA-Z0-9\\s.,!?-]"), " ") // Remove unusual characters but keep basic punctuation
            .replace(Regex("\\b[A-Z]{1}[a-z]*[A-Z]{1,}[a-z]*\\b"), "") // Remove likely OCR artifacts (mixed case gibberish)
            .replace(Regex("\\b[A-Z]{3,}\\b")) { match ->
                // For all-caps words > 2 chars, try to make them more readable
                val word = match.value
                if (word.length > 8) "" // Very long caps likely gibberish
                else word.lowercase().replaceFirstChar { it.uppercase() }
            }
            .trim()
    }

    // Helper function to lightly clean OCR text for transcript display (preserve original text better)
    private fun cleanOCRForTranscript(text: String): String {
        return text
            .trim()
            .replace(Regex("\\s+"), " ") // Just normalize whitespace
            .trim()
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
                
                // Use smart title generation
                val title = generateSmartTitle(transcript)
                
                val note = Note(
                    title = title,
                    snippet = json.toString(),
                    transcript = transcript
                )
                repository.noteDao.insert(note)
                
                // Sync to web server for real-time updates
                try {
                    val serverNote = com.example.app.server.ServerNote(
                        id = java.util.UUID.randomUUID().toString(),
                        title = title,
                        body = transcript,
                        updatedAt = System.currentTimeMillis().toString()
                    )
                    com.example.app.server.KtorServer.addNoteWithBroadcast(serverNote)
                    Log.d("NoteViewModel", "Synced note to web server: $title")
                } catch (e: Exception) {
                    Log.w("NoteViewModel", "Failed to sync note to web server", e)
                }
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

    // Assistant chat history
    private val _assistantChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val assistantChatHistory: StateFlow<List<Pair<String, String>>> = _assistantChatHistory

    // Auto-close assistant callback
    private val _shouldCloseAssistant = MutableStateFlow(false)
    val shouldCloseAssistant: StateFlow<Boolean> = _shouldCloseAssistant

    // AI Chat functionality
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Voice interface functionality
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceText = MutableStateFlow("")
    val voiceText: StateFlow<String> = _voiceText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Voice session chat history - separate from regular chat
    private val _voiceSessionHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val voiceSessionHistory: StateFlow<List<ChatMessage>> = _voiceSessionHistory.asStateFlow()

    private val _shouldAutoRestart = MutableStateFlow(true)
    val shouldAutoRestart: StateFlow<Boolean> = _shouldAutoRestart.asStateFlow()

    // Tasks functionality
    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    // Speech Recognition
    private var speechRecognizer: SpeechRecognizer? = null

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
    private var openAITTS: OpenAITTS? = null
    private var recognizerIntent: Intent? = null

    suspend fun generateTitle(summary: String): String = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== GENERATE TITLE START ===")
        Log.d("NoteViewModel", "Input summary for title: '$summary'")
        
        val prompt = "Generate a concise, relevant title for this note: $summary"
        Log.d("NoteViewModel", "Title prompt: '$prompt'")
        
        val request = GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(Message(role = "user", content = prompt))
        )
        
        try {
            Log.d("NoteViewModel", "Making title API request...")
            val response = RetrofitInstance.api.summarizeText(request)
            Log.d("NoteViewModel", "Title API response received. Success: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val rawTitle = response.body()?.choices?.firstOrNull()?.message?.content
                Log.d("NoteViewModel", "Raw title response: '$rawTitle'")
                
                val cleanTitle = rawTitle?.trim('"', '\n', ' ', '.') ?: "Untitled"
                Log.d("NoteViewModel", "Clean title result: '$cleanTitle'")
                Log.d("NoteViewModel", "=== GENERATE TITLE SUCCESS ===")
                
                cleanTitle
            } else {
                Log.w("NoteViewModel", "Title API request failed with code: ${response.code()}")
                "Untitled"
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== GENERATE TITLE ERROR ===", e)
            "Untitled"
        }
    }
    init {
        tts = TextToSpeech(app.applicationContext, this)
        // Initialize OpenAI TTS if API key is available
        val apiKey = ApiKeyProvider.getApiKey(app.applicationContext)
        if (!apiKey.isNullOrBlank()) {
            openAITTS = OpenAITTS(app.applicationContext, apiKey)
        }
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
        stopListening()
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
                        var summaryText = response.body()?.choices?.firstOrNull()?.message?.content?.trim() ?: ""
                        
                        // Clean up any brackets from the summary
                        summaryText = summaryText.trim()
                        if (summaryText.startsWith("[") && summaryText.endsWith("]")) {
                            summaryText = summaryText.substring(1, summaryText.length - 1).trim()
                        }
                        summaryText = summaryText.replace(Regex("^\\[\\s*"), "").replace(Regex("\\s*\\]$"), "")
                        
                        summary.value = summaryText
                    }
                    // Also get AI assistant response for the transcript
                    getAssistantResponse(finalResult)
                }
                if (isRecording.value == true) {
                    speechRecognizer?.startListening(createRecognizerIntent())
                }
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                data?.firstOrNull()?.let { partial ->
                    appendTranscript(partial, isFinal = false)
                }
            }
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
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

    // Clear assistant chat history
    fun clearAssistantChat() {
        _assistantChatHistory.value = emptyList()
        _shouldCloseAssistant.value = false
    }

    // Smart list detection and completion
    private var isCreatingList = false
    private var currentListItems = mutableListOf<String>()
    private var listType = ""

    private suspend fun checkForListCompletion(userInput: String, aiResponse: String) {
        val lowerInput = userInput.lowercase()
        val lowerAiResponse = aiResponse.lowercase()
        
        // Detect list creation intent
        if (!isCreatingList && (lowerInput.contains("create") || lowerInput.contains("make") || lowerInput.contains("need")) && 
            (lowerInput.contains("list") || lowerInput.contains("shopping") || lowerInput.contains("grocery") || 
             lowerInput.contains("travel") || lowerInput.contains("todo") || lowerInput.contains("checklist"))) {
            
            isCreatingList = true
            currentListItems.clear()
            
            // Extract list type
            listType = when {
                lowerInput.contains("shopping") -> "Shopping List"
                lowerInput.contains("grocery") -> "Grocery List" 
                lowerInput.contains("travel") -> "Travel List"
                lowerInput.contains("todo") -> "Todo List"
                lowerInput.contains("checklist") -> "Checklist"
                else -> "List"
            }
            
            Log.d("NoteViewModel", "Started creating $listType")
            return
        }
        
        // If we're in list creation mode
        if (isCreatingList) {
            // Only save and close if user explicitly says they're done AND AI confirms
            if ((lowerInput.contains("yes") || lowerInput.contains("that's all") || 
                lowerInput.contains("that's it") || lowerInput.contains("done") ||
                lowerInput.contains("finish") || lowerInput.contains("complete")) &&
                (lowerAiResponse.contains("saved") || lowerAiResponse.contains("created") || 
                 lowerAiResponse.contains("note") && lowerAiResponse.contains("you"))) {
                
                // Only save if we have items and AI confirms saving
                if (currentListItems.isNotEmpty()) {
                    saveListAsNote()
                }
                
                // Reset list state but don't auto-close
                isCreatingList = false
                currentListItems.clear()
                listType = ""
                
                // Don't auto-close - let conversation continue
                // _shouldCloseAssistant.value = true
                
                Log.d("NoteViewModel", "List completed and saved automatically")
                return
            }
            
            // Extract items from user input (skip questions/confirmations)
            if (!lowerInput.contains("?") && !lowerAiResponse.contains("is that all")) {
                val items = extractListItems(userInput)
                currentListItems.addAll(items)
                Log.d("NoteViewModel", "Added items: $items, Total: ${currentListItems.size}")
            }
            
            // Check if AI is asking for confirmation
            if (lowerAiResponse.contains("is that all") || lowerAiResponse.contains("anything else") || 
                lowerAiResponse.contains("is there more") || lowerAiResponse.contains("that's your list")) {
                Log.d("NoteViewModel", "AI asking for confirmation - ready to save list")
            }
        }
    }

    private fun extractListItems(input: String): List<String> {
        val items = mutableListOf<String>()
        val text = input.trim()
        
        // Split by common separators and clean up
        val potentialItems = text.split(",", "and", "&", "\n")
        
        potentialItems.forEach { item ->
            val cleanItem = item.trim()
                .removePrefix("add")
                .removePrefix("include") 
                .removePrefix("also")
                .removePrefix("i need")
                .removePrefix("i want")
                .trim()
            
            if (cleanItem.isNotBlank() && cleanItem.length > 1) {
                items.add(cleanItem.replaceFirstChar { it.uppercase() })
            }
        }
        
        return items
    }

    private suspend fun saveListAsNote() {
        if (currentListItems.isEmpty()) return
        
        try {
            val listContent = buildString {
                appendLine("# $listType")
                appendLine()
                currentListItems.forEachIndexed { index, item ->
                    appendLine("${index + 1}. $item")
                }
                appendLine()
                appendLine("Created with Logion Assistant")
            }
            
            // Use smart title generation for lists
            val smartTitle = generateSmartTitle(currentListItems.joinToString(", "))
            
            val note = Note(
                title = smartTitle,
                snippet = listContent,
                transcript = currentListItems.joinToString(", "),
                audioPath = null
            )
            
            repository.noteDao.insert(note)
            Log.d("NoteViewModel", "List saved: $smartTitle with ${currentListItems.size} items")
            
            // Sync to web server for real-time updates
            try {
                val serverNote = com.example.app.server.ServerNote(
                    id = java.util.UUID.randomUUID().toString(),
                    title = smartTitle,
                    body = currentListItems.joinToString(", "),
                    updatedAt = System.currentTimeMillis().toString()
                )
                com.example.app.server.KtorServer.addNoteWithBroadcast(serverNote)
                Log.d("NoteViewModel", "Synced list to web server: $smartTitle")
            } catch (e: Exception) {
                Log.w("NoteViewModel", "Failed to sync list to web server", e)
            }
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error saving list: ${e.message}")
        }
    }

    // Save chat conversation as a note - same logic as stopAndSaveNote()
    fun saveChatAsNote() = viewModelScope.launch {
        try {
            val chatMessages = _chatMessages.value
            Log.d("NoteViewModel", "=== SAVE CHAT START ===")
            Log.d("NoteViewModel", "Chat messages size: ${chatMessages.size}")
            
            if (chatMessages.isNotEmpty()) {
                // Convert chat messages to transcript format
                val transcript = chatMessages.joinToString("\n\n") { message ->
                    if (message.isUser) "User: ${message.content}" else "Assistant: ${message.content}"
                }
                
                Log.d("NoteViewModel", "Full transcript: '$transcript'")
                
                // Use the same logic as stopAndSaveNote() - extract summary and tasks with OpenAI
                Log.d("NoteViewModel", "Calling extractSummaryAndTasksWithOpenAI...")
                val result = extractSummaryAndTasksWithOpenAI(transcript)
                Log.d("NoteViewModel", "OpenAI extraction result: $result")
                
                val summaryOut = result?.first ?: ""
                val tasks = result?.second ?: emptyList<String>()
                
                Log.d("NoteViewModel", "Extracted summary: '$summaryOut'")
                Log.d("NoteViewModel", "Extracted tasks: $tasks")
                
                // If OpenAI failed, use fallback
                val finalSummary = if (summaryOut.isBlank()) {
                    "Chat conversation about various topics"
                } else summaryOut
                
                // Create JSON snippet same as recording
                val json = org.json.JSONObject()
                json.put("summary", finalSummary)
                if (tasks.isNotEmpty()) json.put("tasks", org.json.JSONArray(tasks))
                
                Log.d("NoteViewModel", "JSON snippet created: '${json.toString()}'")
                
                // Generate title using smart title generation
                Log.d("NoteViewModel", "Calling generateSmartTitle with transcript: '$transcript'")
                val generatedTitle = generateSmartTitle(transcript)
                Log.d("NoteViewModel", "Generated title result: '$generatedTitle'")
                
                // If title generation failed, use fallback
                val finalTitle = if (generatedTitle.isBlank() || generatedTitle == "Untitled") {
                    "Chat - ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                } else generatedTitle
                
                Log.d("NoteViewModel", "Final title: '$finalTitle'")
                Log.d("NoteViewModel", "Final summary: '$finalSummary'")
                
                // Create and save the note - exact same as recording
                val note = Note(
                    title = finalTitle,
                    snippet = json.toString(),
                    transcript = transcript
                )
                
                Log.d("NoteViewModel", "About to insert note...")
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "Note inserted successfully!")
                
                // Sync to web server for real-time updates
                try {
                    val serverNote = com.example.app.server.ServerNote(
                        id = java.util.UUID.randomUUID().toString(),
                        title = finalTitle,
                        body = transcript,
                        updatedAt = System.currentTimeMillis().toString()
                    )
                    com.example.app.server.KtorServer.addNoteWithBroadcast(serverNote)
                    Log.d("NoteViewModel", "Synced chat note to web server: $finalTitle")
                } catch (e: Exception) {
                    Log.w("NoteViewModel", "Failed to sync chat note to web server", e)
                }
                
                Log.d("NoteViewModel", "=== SAVE CHAT SUCCESS ===")
                
            } else {
                Log.d("NoteViewModel", "Chat messages are empty - nothing to save")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE CHAT ERROR ===", e)
            Log.e("NoteViewModel", "Error details: ${e.message}")
            Log.e("NoteViewModel", "Error stack: ${e.stackTraceToString()}")
        }
    }
    
    // Save chat conversation as a task
    fun saveChatAsTask() = viewModelScope.launch {
        try {
            val chatMessages = _chatMessages.value
            Log.d("NoteViewModel", "=== SAVE CHAT AS TASK START ===")
            Log.d("NoteViewModel", "Chat messages size: ${chatMessages.size}")
            
            if (chatMessages.isNotEmpty()) {
                // Get the latest user message and AI response
                val lastUserMessage = chatMessages.findLast { it.isUser }?.content ?: ""
                val lastAiResponse = chatMessages.findLast { !it.isUser }?.content ?: ""
                
                // Use the existing createTaskFromChat method
                createTaskFromChat(lastUserMessage, lastAiResponse)
                
                Log.d("NoteViewModel", "=== SAVE CHAT AS TASK SUCCESS ===")
            } else {
                Log.d("NoteViewModel", "Chat messages are empty - nothing to save as task")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE CHAT AS TASK ERROR ===", e)
            Log.e("NoteViewModel", "Error details: ${e.message}")
        }
    }
    
    // Save chat conversation for continuation
    fun saveChatAsChat(messages: List<ChatMessage>) = viewModelScope.launch {
        try {
            Log.d("NoteViewModel", "=== SAVE CHAT FOR CONTINUATION START ===")
            Log.d("NoteViewModel", "Chat messages size: ${messages.size}")
            
            if (messages.isNotEmpty()) {
                // Create a title based on the first few messages
                val firstUserMessage = messages.find { it.isUser }?.content ?: "New Chat"
                val title = generateTitle(firstUserMessage.take(50))
                
                // Create a JSON object to store the chat messages for continuation
                val chatData = org.json.JSONObject()
                val messagesArray = org.json.JSONArray()
                
                messages.forEach { message ->
                    val messageObj = org.json.JSONObject()
                    messageObj.put("content", message.content)
                    messageObj.put("isUser", message.isUser)
                    messageObj.put("timestamp", message.timestamp)
                    messagesArray.put(messageObj)
                }
                
                chatData.put("type", "chat_continuation")
                chatData.put("messages", messagesArray)
                chatData.put("savedAt", System.currentTimeMillis())
                
                // Create a note with special chat continuation format
                val note = Note(
                    title = title,
                    snippet = chatData.toString(),
                    transcript = "Chat conversation: $title"
                )
                
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "=== SAVE CHAT FOR CONTINUATION SUCCESS ===")
                
                // Don't clear chat - keep it visible for user
                // _chatMessages.value = emptyList()  // Removed this line
                
            } else {
                Log.d("NoteViewModel", "Chat messages are empty - nothing to save")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE CHAT FOR CONTINUATION ERROR ===", e)
            Log.e("NoteViewModel", "Error details: ${e.message}")
        }
    }
    
    // Load saved chat for continuation
    fun loadChatForContinuation(noteId: Long) = viewModelScope.launch {
        try {
            Log.d("NoteViewModel", "=== LOAD CHAT FOR CONTINUATION START ===")
            
            repository.getNoteById(noteId).collect { note ->
                if (note != null) {
                    try {
                        val chatData = org.json.JSONObject(note.snippet)
                        if (chatData.getString("type") == "chat_continuation") {
                            val messagesArray = chatData.getJSONArray("messages")
                            val chatMessages = mutableListOf<ChatMessage>()
                            
                            for (i in 0 until messagesArray.length()) {
                                val messageObj = messagesArray.getJSONObject(i)
                                val message = ChatMessage(
                                    content = messageObj.getString("content"),
                                    isUser = messageObj.getBoolean("isUser"),
                                    timestamp = messageObj.getLong("timestamp")
                                )
                                chatMessages.add(message)
                            }
                            
                            _chatMessages.value = chatMessages
                            Log.d("NoteViewModel", "=== LOAD CHAT FOR CONTINUATION SUCCESS ===")
                            Log.d("NoteViewModel", "Loaded ${chatMessages.size} messages")
                        } else {
                            Log.w("NoteViewModel", "Note is not a chat continuation type")
                        }
                    } catch (jsonException: Exception) {
                        Log.e("NoteViewModel", "Error parsing chat data", jsonException)
                    }
                } else {
                    Log.w("NoteViewModel", "Note not found with ID: $noteId")
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== LOAD CHAT FOR CONTINUATION ERROR ===", e)
        }
    }
    
    // ======================= NEW PROCESSING FUNCTIONS =======================
    
    // Upload Audio Processing
    suspend fun processUploadedAudio(audioUri: android.net.Uri, context: android.content.Context) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS UPLOADED AUDIO START ===")
        
        try {
            // Read audio file and convert to speech
            val audioTranscript = transcribeUploadedAudio(audioUri, context)
            Log.d("NoteViewModel", "Audio transcript: $audioTranscript")
            
            if (audioTranscript.isNotBlank()) {
                // Use same processing as voice recording
                val result = extractSummaryAndTasksWithOpenAI(audioTranscript)
                val title = generateTitle(result?.first ?: "Uploaded Audio Note")
                
                val note = Note(
                    title = title,
                    snippet = result?.second?.toString() ?: """{"summary":"Uploaded audio note"}""",
                    transcript = audioTranscript
                )
                
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "=== PROCESS UPLOADED AUDIO SUCCESS ===")
            } else {
                throw Exception("Could not transcribe audio file")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS UPLOADED AUDIO ERROR ===", e)
            throw e
        }
    }
    
    private suspend fun transcribeUploadedAudio(audioUri: android.net.Uri, context: android.content.Context): String {
        // TODO: Implement audio file transcription using Speech Recognition or external API
        // For now, return placeholder - you would integrate with Google Cloud Speech API or similar
        return "Transcribed content from uploaded audio file would appear here. Integration with speech recognition service needed."
    }
    
    // Image Processing with Visual Analysis
    suspend fun processImageWithOCR(bitmap: android.graphics.Bitmap, context: android.content.Context) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS IMAGE VISUAL ANALYSIS START ===")
        
        try {
            // First, try to extract any text using OCR - get both raw and cleaned versions
            val rawOCRText = performOCRRaw(bitmap, context)
            val cleanedOCRText = cleanExtractedText(rawOCRText)
            Log.d("NoteViewModel", "OCR raw text: $rawOCRText")
            Log.d("NoteViewModel", "OCR cleaned text: $cleanedOCRText")
            
            // Convert bitmap to base64 for OpenAI Vision API
            val base64Image = bitmapToBase64(bitmap)
            
            // Get visual description from OpenAI using cleaned text
            val visualDescription = analyzeImageWithOpenAI(base64Image, cleanedOCRText)
            Log.d("NoteViewModel", "Visual description: $visualDescription")
            
            // Check if the visual description indicates an error
            if (visualDescription.isNotBlank() && 
                !visualDescription.contains("API key not configured") &&
                !visualDescription.contains("error", ignoreCase = true) &&
                !visualDescription.contains("failed", ignoreCase = true) &&
                !visualDescription.contains("timeout", ignoreCase = true)) {
                
                processImageContent(visualDescription, rawOCRText, "Image Analysis")
                Log.d("NoteViewModel", "=== PROCESS IMAGE VISUAL ANALYSIS SUCCESS ===")
            } else {
                // Fallback to OCR-only processing if visual analysis fails
                Log.w("NoteViewModel", "Visual analysis failed, falling back to OCR-only: $visualDescription")
                
                if (rawOCRText.isNotBlank()) {
                    val fallbackSummary = "Text extracted from image using OCR. Visual analysis was not available."
                    processImageContent(fallbackSummary, rawOCRText, "Image Text (OCR)")
                    Log.d("NoteViewModel", "=== PROCESS IMAGE OCR FALLBACK SUCCESS ===")
                } else {
                    throw Exception("No text found in image and visual analysis failed: $visualDescription")
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS IMAGE VISUAL ANALYSIS ERROR ===", e)
            throw e
        }
    }
    
    suspend fun processImageWithOCR(imageUri: android.net.Uri, context: android.content.Context) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS IMAGE URI VISUAL ANALYSIS START ===")
        
        try {
            // First, try to extract any text using OCR - get both raw and cleaned versions
            val rawOCRText = performOCRRaw(imageUri, context)
            val cleanedOCRText = cleanExtractedText(rawOCRText)
            Log.d("NoteViewModel", "OCR raw text: $rawOCRText")
            Log.d("NoteViewModel", "OCR cleaned text: $cleanedOCRText")
            
            // Convert URI to bitmap then to base64 for OpenAI Vision API
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                throw Exception("Could not decode image from URI")
            }
            
            val base64Image = bitmapToBase64(bitmap)
            
            // Get visual description from OpenAI using cleaned text
            val visualDescription = analyzeImageWithOpenAI(base64Image, cleanedOCRText)
            Log.d("NoteViewModel", "Visual description: $visualDescription")
            
            // Check if the visual description indicates an error
            if (visualDescription.isNotBlank() && 
                !visualDescription.contains("API key not configured") &&
                !visualDescription.contains("error", ignoreCase = true) &&
                !visualDescription.contains("failed", ignoreCase = true) &&
                !visualDescription.contains("timeout", ignoreCase = true)) {
                
                processImageContent(visualDescription, rawOCRText, "Image Analysis")
                Log.d("NoteViewModel", "=== PROCESS IMAGE URI VISUAL ANALYSIS SUCCESS ===")
            } else {
                // Fallback to OCR-only processing if visual analysis fails
                Log.w("NoteViewModel", "Visual analysis failed, falling back to OCR-only: $visualDescription")
                
                if (rawOCRText.isNotBlank()) {
                    val fallbackSummary = "Text extracted from image using OCR. Visual analysis was not available."
                    processImageContent(fallbackSummary, rawOCRText, "Image Text (OCR)")
                    Log.d("NoteViewModel", "=== PROCESS IMAGE URI OCR FALLBACK SUCCESS ===")
                } else {
                    throw Exception("No text found in image and visual analysis failed: $visualDescription")
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS IMAGE URI VISUAL ANALYSIS ERROR ===", e)
            throw e
        }
    }
    
    private suspend fun performOCR(bitmap: android.graphics.Bitmap, context: android.content.Context): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image: InputImage = InputImage.fromBitmap(bitmap, 0)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                        val cleanedText = cleanExtractedText(visionText.text)
                        continuation.resume(cleanedText)
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.e("NoteViewModel", "OCR failed for bitmap", e)
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error setting up OCR for bitmap", e)
                continuation.resumeWithException(e)
            }
        }
    }

    // New function to get raw OCR text for transcript
    private suspend fun performOCRRaw(bitmap: android.graphics.Bitmap, context: android.content.Context): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image: InputImage = InputImage.fromBitmap(bitmap, 0)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                        val rawText = cleanOCRForTranscript(visionText.text)
                        continuation.resume(rawText)
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.e("NoteViewModel", "OCR failed for bitmap", e)
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error setting up OCR for bitmap", e)
                continuation.resumeWithException(e)
            }
        }
    }
    
    private suspend fun performOCR(imageUri: android.net.Uri, context: android.content.Context): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                // Try to create InputImage from URI
                try {
                    val image = InputImage.fromFilePath(context, imageUri)
                    
                    recognizer.process(image)
                        .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                            val cleanedText = cleanExtractedText(visionText.text)
                            continuation.resume(cleanedText)
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.e("NoteViewModel", "OCR failed for URI", e)
                            continuation.resumeWithException(e)
                        }
                } catch (e: Exception) {
                    Log.w("NoteViewModel", "Direct URI processing failed, trying bitmap fallback", e)
                    // Fallback: try to load as bitmap first
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    if (inputStream != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        
                        val image = InputImage.fromBitmap(bitmap, 0)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                                val cleanedText = cleanExtractedText(visionText.text)
                                continuation.resume(cleanedText)
                            }
                            .addOnFailureListener { e: Exception ->
                                Log.e("NoteViewModel", "OCR failed for bitmap fallback", e)
                                continuation.resumeWithException(e)
                            }
                    } else {
                        Log.e("NoteViewModel", "Could not open input stream for URI")
                        continuation.resumeWithException(IOException("Could not open input stream for URI"))
                    }
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error setting up OCR for URI", e)
                continuation.resumeWithException(e)
            }
        }
    }

    // Raw OCR function for URI that preserves original text for transcript
    private suspend fun performOCRRaw(imageUri: android.net.Uri, context: android.content.Context): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                // Try to create InputImage from URI
                try {
                    val image = InputImage.fromFilePath(context, imageUri)
                    
                    recognizer.process(image)
                        .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                            val rawText = cleanOCRForTranscript(visionText.text)
                            continuation.resume(rawText)
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.e("NoteViewModel", "OCR failed for URI", e)
                            continuation.resumeWithException(e)
                        }
                } catch (e: Exception) {
                    Log.w("NoteViewModel", "Direct URI processing failed, trying bitmap fallback", e)
                    // Fallback: try to load as bitmap first
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    if (inputStream != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        
                        val image = InputImage.fromBitmap(bitmap, 0)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                                val rawText = cleanOCRForTranscript(visionText.text)
                                continuation.resume(rawText)
                            }
                            .addOnFailureListener { e: Exception ->
                                Log.e("NoteViewModel", "OCR failed for bitmap fallback", e)
                                continuation.resumeWithException(e)
                            }
                    } else {
                        Log.e("NoteViewModel", "Could not open input stream for URI")
                        continuation.resumeWithException(IOException("Could not open input stream for URI"))
                    }
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error setting up OCR for URI", e)
                continuation.resumeWithException(e)
            }
        }
    }
    
    // Helper function to convert bitmap to base64 for OpenAI Vision API
    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        // Resize bitmap if it's too large to avoid API limits and reduce processing time
        val maxSize = 1536 // Increased maximum size for better object recognition
        val resizedBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            
            Log.d("NoteViewModel", "Resizing bitmap from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")
            android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
        
        val outputStream = java.io.ByteArrayOutputStream()
        // Use higher quality for better object recognition
        resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        
        Log.d("NoteViewModel", "Generated base64 string length: ${base64String.length}")
        Log.d("NoteViewModel", "Compressed image size: ${byteArray.size} bytes")
        
        return base64String
    }
    
    // Analyze image content using OpenAI Vision API
    private suspend fun analyzeImageWithOpenAI(base64Image: String, extractedText: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // Increased timeout
                    .readTimeout(120, TimeUnit.SECONDS)     // Increased timeout for image processing
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val apiKey = com.example.app.util.ApiKeyProvider.getApiKey(getApplication())
                if (apiKey.isNullOrBlank()) {
                    Log.e("NoteViewModel", "OpenAI API key is not configured")
                    return@withContext "OpenAI API key not configured. Please set your API key in settings."
                }

                Log.d("NoteViewModel", "Starting OpenAI Vision API call with image size: ${base64Image.length} chars")

                // Create a comprehensive prompt for image analysis
                val prompt = buildString {
                    append("You are an expert at identifying objects, products, and items in images. Analyze this image carefully and provide a detailed, conversational description.")
                    append("\n\nBe very specific about:")
                    append("\n- EXACTLY what objects you can identify (if it's a mouse, say 'computer mouse' or 'PC mouse')")
                    append("\n- Brand names, model numbers, or product labels you can see")
                    append("\n- Colors, materials, and design features")
                    append("\n- The purpose and function of items shown")
                    append("\n- Any technical specifications or features visible")
                    append("\n- Text, logos, or markings on the items")
                    
                    append("\n\nIMPORTANT: Don't use generic terms. Instead of saying 'device' or 'object', identify the specific item:")
                    append("\n- If it's a computer mouse, say 'computer mouse' or 'PC mouse'")
                    append("\n- If it's a keyboard, say 'keyboard'")
                    append("\n- If it's a phone, identify the brand and model if possible")
                    append("\n- If it's food, name the specific food item")
                    append("\n- If it's an app interface, identify the app name")
                    
                    append("\n\nWrite in a natural, conversational tone. Be confident in your identifications - if you can clearly see it's a mouse, say so definitively.")
                    
                    if (extractedText.isNotBlank()) {
                        append("\n\nI can also see this text in the image: \"$extractedText\". ")
                        append("Use this text to help identify brands, models, or provide additional context about what's shown.")
                    }
                    
                    append("\n\nIf you can identify where someone might buy this item or similar products, mention that as well.")
                }

                // Updated to use gpt-4o which supports vision
                val requestBody = JSONObject().apply {
                    put("model", "gpt-4o")  // Updated model name
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("type", "text")
                                    put("text", prompt)
                                })
                                put(JSONObject().apply {
                                    put("type", "image_url")
                                    put("image_url", JSONObject().apply {
                                        put("url", "data:image/jpeg;base64,$base64Image")
                                        put("detail", "high")
                                    })
                                })
                            })
                        })
                    })
                    put("max_tokens", 2000)  // Increased for more detailed object identification
                    put("temperature", 0.3)  // Lower temperature for more consistent, accurate identification
                }

                Log.d("NoteViewModel", "Sending request to OpenAI Vision API...")

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("NoteViewModel", "OpenAI Vision API Response Code: ${response.code}")
                Log.d("NoteViewModel", "OpenAI Vision API Response: $responseBody")

                when {
                    response.isSuccessful && responseBody != null -> {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val choices = jsonResponse.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val message = choices.getJSONObject(0).getJSONObject("message")
                                val content = message.getString("content").trim()
                                Log.d("NoteViewModel", "Successfully extracted content: ${content.take(100)}...")
                                return@withContext content
                            } else {
                                Log.e("NoteViewModel", "No choices in API response")
                                return@withContext "OpenAI API returned empty response"
                            }
                        } catch (e: Exception) {
                            Log.e("NoteViewModel", "Failed to parse OpenAI response JSON", e)
                            return@withContext "Failed to parse API response: ${e.message}"
                        }
                    }
                    response.code == 401 -> {
                        Log.e("NoteViewModel", "API key authentication failed")
                        return@withContext "Invalid OpenAI API key. Please check your API key configuration."
                    }
                    response.code == 429 -> {
                        Log.e("NoteViewModel", "API rate limit exceeded")
                        return@withContext "OpenAI API rate limit exceeded. Please try again later."
                    }
                    response.code == 400 -> {
                        Log.e("NoteViewModel", "Bad request to OpenAI API: $responseBody")
                        return@withContext "Invalid request to OpenAI API. The image might be too large or unsupported format."
                    }
                    else -> {
                        Log.e("NoteViewModel", "OpenAI API call failed with code: ${response.code}, body: $responseBody")
                        return@withContext "OpenAI API error (${response.code}): ${responseBody?.take(200) ?: "Unknown error"}"
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("NoteViewModel", "Timeout calling OpenAI Vision API", e)
                return@withContext "Request timed out. The image analysis is taking too long - please try with a smaller image."
            } catch (e: java.net.UnknownHostException) {
                Log.e("NoteViewModel", "Network error calling OpenAI Vision API", e)
                return@withContext "Network error. Please check your internet connection and try again."
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Unexpected error analyzing image with OpenAI", e)
                return@withContext "Unexpected error analyzing image: ${e.message}"
            }
        }
    }
    
    // Simple Vision API wrapper for chat context
    private suspend fun analyzeImageWithVision(bitmap: android.graphics.Bitmap): String {
        return try {
            val base64Image = bitmapToBase64(bitmap)
            analyzeImageWithOpenAI(base64Image, "")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error in analyzeImageWithVision", e)
            "I can see an image was uploaded, but I'm having trouble analyzing it right now."
        }
    }
    
    // Type Text Processing
    suspend fun processTextNote(textContent: String) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS TEXT NOTE START ===")
        
        try {
            processExtractedContent(textContent, "Text Note")
            Log.d("NoteViewModel", "=== PROCESS TEXT NOTE SUCCESS ===")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS TEXT NOTE ERROR ===", e)
            throw e
        }
    }
    
    // Video URL Processing
    suspend fun processVideoUrl(videoUrl: String) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS VIDEO URL START ===")
        
        try {
            val videoSummary = extractVideoSummary(videoUrl)
            Log.d("NoteViewModel", "Video summary: $videoSummary")
            
            if (videoSummary.isNotBlank()) {
                processExtractedContent(videoSummary, "Video Summary")
                Log.d("NoteViewModel", "=== PROCESS VIDEO URL SUCCESS ===")
            } else {
                throw Exception("Could not extract video information")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS VIDEO URL ERROR ===", e)
            throw e
        }
    }
    
    private suspend fun extractVideoSummary(videoUrl: String): String {
        // TODO: Implement video metadata extraction
        // You would integrate with YouTube Data API or video parsing libraries
        return """
        Video URL: $videoUrl
        
        Video information would be extracted here including:
        - Title and description
        - Duration and view count
        - Channel information
        - Generated summary based on metadata
        
        Integration with YouTube Data API or similar service needed for full functionality.
        """.trimIndent()
    }
    
    // Web Page Processing
    suspend fun processWebPageUrl(webUrl: String) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS WEB PAGE START ===")
        
        try {
            val webContent = fetchWebPageContent(webUrl)
            Log.d("NoteViewModel", "Web content extracted: ${webContent.take(200)}...")
            
            if (webContent.isNotBlank()) {
                processExtractedContent(webContent, "Web Article")
                Log.d("NoteViewModel", "=== PROCESS WEB PAGE SUCCESS ===")
            } else {
                throw Exception("Could not fetch web page content")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS WEB PAGE ERROR ===", e)
            throw e
        }
    }
    
    private suspend fun fetchWebPageContent(webUrl: String): String {
        return try {
            val doc = org.jsoup.Jsoup.connect(webUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            // Extract main content
            val title = doc.title()
            val content = doc.select("p, h1, h2, h3, h4, h5, h6, li").text()
            
            """
            Title: $title
            
            Content:
            $content
            """.trimIndent()
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error fetching web content", e)
            throw Exception("Failed to fetch web page: ${e.message}")
        }
    }
    
    // Document Processing (PDF/Word)
    suspend fun processDocument(documentUri: android.net.Uri, context: android.content.Context) = withContext(Dispatchers.IO) {
        Log.d("NoteViewModel", "=== PROCESS DOCUMENT START ===")
        
        try {
            val documentText = extractDocumentText(documentUri, context)
            Log.d("NoteViewModel", "Document text extracted: ${documentText.take(200)}...")
            
            if (documentText.isNotBlank()) {
                processExtractedContent(documentText, "Document")
                Log.d("NoteViewModel", "=== PROCESS DOCUMENT SUCCESS ===")
            } else {
                throw Exception("Could not extract text from document")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== PROCESS DOCUMENT ERROR ===", e)
            throw e
        }
    }
    
    private suspend fun extractDocumentText(documentUri: android.net.Uri, context: android.content.Context): String {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(documentUri)
            
            when {
                mimeType == "application/pdf" -> {
                    // TODO: Implement PDF text extraction using PDFBox
                    "PDF text extraction would be implemented here using PDFBox library."
                }
                mimeType?.contains("word") == true -> {
                    // TODO: Implement Word document text extraction
                    "Word document text extraction would be implemented here using Apache POI or similar."
                }
                mimeType?.startsWith("text") == true -> {
                    // Plain text file
                    contentResolver.openInputStream(documentUri)?.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    } ?: ""
                }
                else -> {
                    throw Exception("Unsupported document format: $mimeType")
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error extracting document text", e)
            throw Exception("Failed to extract document text: ${e.message}")
        }
    }
    
    // Common processing function for all extracted content
    private suspend fun processExtractedContent(content: String, contentType: String) {
        val result = extractSummaryAndTasksWithOpenAI(content)
        var summary = result?.first ?: "Extracted content from $contentType"
        
        // Clean up any remaining brackets from summary
        summary = cleanBracketsFromText(summary)
        
        val title = generateTitle(summary)
        
        val note = Note(
            title = title,
            snippet = result?.second?.toString() ?: """{"summary":"$contentType content"}""",
            transcript = content
        )
        
        repository.noteDao.insert(note)
    }

    // Specialized processing function for image content
    private suspend fun processImageContent(summary: String, ocrText: String, contentType: String) {
        // Clean up any remaining brackets from summary
        val cleanedSummary = cleanBracketsFromText(summary)
        
        Log.d("NoteViewModel", "Original summary: '$summary'")
        Log.d("NoteViewModel", "Cleaned summary: '$cleanedSummary'")
        
        val title = generateTitle(cleanedSummary)
        
        val note = Note(
            title = title,
            snippet = """{"summary":"$cleanedSummary"}""",
            transcript = ocrText.ifBlank { "No text detected in image" }
        )
        
        Log.d("NoteViewModel", "Stored snippet: '${note.snippet}'")
        Log.d("NoteViewModel", "Stored transcript: '${note.transcript}'")
        
        repository.noteDao.insert(note)
    }
    
    // ======================= END NEW PROCESSING FUNCTIONS =======================
    
    // Simple test function to verify saving works
    fun saveTestChatNote() = viewModelScope.launch {
        try {
            Log.d("NoteViewModel", "Saving test chat note")
            val note = Note(
                title = "Test Chat Note",
                snippet = """{"summary": "This is a test chat note created to verify the save functionality works."}""",
                transcript = "User: Test message\n\nAssistant: This is a test response."
            )
            repository.noteDao.insert(note)
            Log.d("NoteViewModel", "Test chat note saved successfully")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error saving test chat note", e)
        }
    }

    // ======================= AI CHAT FUNCTIONALITY =======================
    
    // Immediate ChatGPT-like response function with conversation context
    fun askAssistant(question: String) = viewModelScope.launch {
        // Add user message immediately
        val userMessage = ChatMessage(content = question, isUser = true)
        repository.insertChatMessage(userMessage)
        
        // Set loading state immediately
        _isAiLoading.value = true
        
        try {
            // Get current conversation history for context
            val conversationHistory = _chatMessages.value.takeLast(10) // Last 10 messages for context
            
            // Build messages with conversation context
            val messages = mutableListOf<Message>()
            
            // Add system prompt with context awareness
            messages.add(Message(role = "system", content = """
                You are ChatGPT, an expert AI assistant.
                - Answer immediately in a clear, direct, conversational style.
                - Be human-like but concise.
                - Always remember conversation context and refer to previously discussed topics.
                - When user says "it", "that", "this" etc., assume they're referring to the last discussed subject.
                - Do not say "it depends what it refers to" unless absolutely no context is available.
                - Answer directly and conversationally, like ChatGPT would.
                - Keep track of what we're talking about across messages.
            """.trimIndent()))
            
            // Add conversation history for context (excluding the current message we just added)
            conversationHistory.dropLast(1).forEach { chatMsg ->
                messages.add(Message(
                    role = if (chatMsg.isUser) "user" else "assistant",
                    content = chatMsg.content
                ))
            }
            
            // Add current user question
            messages.add(Message(role = "user", content = question))
            
            val request = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(request)
            val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?.trim() 
                ?: "Sorry, I couldn't process your request."
            
            // Add AI response immediately
            val aiMessage = ChatMessage(content = aiResponse, isUser = false)
            repository.insertChatMessage(aiMessage)
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error in askAssistant", e)
            val errorMessage = ChatMessage(
                content = "Sorry, I encountered an error. Please try again.",
                isUser = false
            )
            repository.insertChatMessage(errorMessage)
        } finally {
            _isAiLoading.value = false
        }
    }
    
    fun sendChatMessage(message: String) = viewModelScope.launch {
        // Add user message
        val userMessage = ChatMessage(content = message, isUser = true)
        repository.insertChatMessage(userMessage)
        
        // Set loading state
        _isAiLoading.value = true
        
        try {
            // Get current hour for contextual greetings
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val timeContext = when (currentHour) {
                in 5..11 -> "morning"
                in 12..17 -> "afternoon" 
                in 18..21 -> "evening"
                else -> "night"
            }
            
            // Prepare messages for OpenAI
            val messages = mutableListOf<Message>()
            
            // Get current chat messages (should be empty or minimal after clearing)
            val currentChatMessages = _chatMessages.value.takeLast(10)
            
            // Only check for images in the current conversation context
            val hasRecentImages = currentChatMessages.any { !it.imageUri.isNullOrEmpty() }
            
            messages.add(Message(role = "system", content = """
                You are Logion AI, a friendly and helpful personal assistant. 
                
                Current context: It's currently $timeContext time for the user.
                
                ${if (hasRecentImages && currentChatMessages.isNotEmpty()) {
                    "IMPORTANT: The user has shared images in this current conversation. When they ask follow-up questions about images, refer back to the detailed descriptions you provided earlier."
                } else {
                    "You can see and analyze images when users share them. Answer the user's question directly."
                }}
                
                Your role:
                - Answer questions directly and accurately
                - Have natural, helpful conversations with users
                - When users share images, describe what you see clearly
                - Stay focused on the current conversation topic
                - Be conversational and engaging
                
                Key behaviors:
                - Answer the user's current question directly
                - If they ask about text topics (like "where is the moon"), provide factual information
                - Only mention images if the user actually shared an image in this conversation
                - Don't assume there are images unless explicitly shared
                - Provide helpful, accurate information based on the actual question asked
            """.trimIndent()))
            
            // Add current chat history (should be minimal after clearing)
            currentChatMessages.forEach { chatMsg ->
                val messageContent = if (chatMsg.imageUri.isNullOrEmpty()) {
                    chatMsg.content
                } else {
                    if (chatMsg.isUser) {
                        "${chatMsg.content} [User shared an image: ${chatMsg.imageUri}]"
                    } else {
                        // This is the AI's response to an image - keep the full detailed description
                        chatMsg.content
                    }
                }
                
                messages.add(Message(
                    role = if (chatMsg.isUser) "user" else "assistant",
                    content = messageContent
                ))
            }
            
            val request = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(request)
            val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?.trim() 
                ?: "Sorry, I couldn't process your request."
            
            // Add AI response
            val aiMessage = ChatMessage(content = aiResponse, isUser = false)
            repository.insertChatMessage(aiMessage)
            
            // Check for note/task creation requests in text chat too
            checkForNoteTaskCreation(message, aiResponse)
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error sending chat message", e)
            val errorMessage = ChatMessage(
                content = "Sorry, I'm having trouble connecting. Please try again.",
                isUser = false
            )
            repository.insertChatMessage(errorMessage)
        } finally {
            _isAiLoading.value = false
        }
    }
    
    // Helper function to check for note/task creation in both voice and text chat
    private suspend fun checkForNoteTaskCreation(userMessage: String, aiResponse: String) {
        val userWantsNote = userMessage.contains("create a note", ignoreCase = true) ||
                           userMessage.contains("make a note", ignoreCase = true) ||
                           userMessage.contains("save this", ignoreCase = true) ||
                           userMessage.contains("note this", ignoreCase = true) ||
                           userMessage.contains("write this down", ignoreCase = true) ||
                           userMessage.contains("save as a note", ignoreCase = true) ||
                           userMessage.contains("note down", ignoreCase = true)
        
        val userWantsTask = userMessage.contains("create a task", ignoreCase = true) ||
                           userMessage.contains("make a task", ignoreCase = true) ||
                           userMessage.contains("add a task", ignoreCase = true) ||
                           userMessage.contains("task to", ignoreCase = true) ||
                           userMessage.contains("remind me to", ignoreCase = true) ||
                           userMessage.contains("need to do", ignoreCase = true)
        
        val aiConfirmsNote = aiResponse.contains("I'll create a note", ignoreCase = true) ||
                            aiResponse.contains("I'll make a note", ignoreCase = true) ||
                            aiResponse.contains("creating a note", ignoreCase = true) ||
                            aiResponse.contains("making a note", ignoreCase = true)
        
        val aiConfirmsTask = aiResponse.contains("I'll create a task", ignoreCase = true) ||
                            aiResponse.contains("creating a task", ignoreCase = true) ||
                            aiResponse.contains("I'll make a task", ignoreCase = true) ||
                            aiResponse.contains("making a task", ignoreCase = true)
        
        // Priority: explicit user intent > AI confirmation
        when {
            userWantsTask || aiConfirmsTask -> {
                createTaskFromChat(userMessage, aiResponse)
            }
            userWantsNote || aiConfirmsNote -> {
                saveChatAsNote(userMessage, aiResponse)
            }
            // If neither is explicitly requested, don't create anything - just chat
        }
    }
    
    // Create task from regular chat
    private suspend fun createTaskFromChat(userMessage: String, aiResponse: String) {
        try {
            // Extract task content from user message
            val taskContent = extractTaskFromMessage(userMessage)
            
            val task = Task(
                title = taskContent.take(100), // Limit title length
                description = "", // Remove automatic descriptions
                priority = "Medium",
                dueDate = System.currentTimeMillis(), // Set to today so it shows up in "Today's Tasks"
                duration = "",
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            repository.insertTask(task)
            Log.d("NoteViewModel", "Created task from chat: ${task.title}")
            Log.d("NoteViewModel", "Task due date: ${task.dueDate}, current time: ${System.currentTimeMillis()}")
            
            // Sync to web server in real-time
            try {
                val serverTask = com.example.app.server.ServerTask(
                    id = task.id.toString(),
                    title = task.title,
                    body = task.description,
                    done = task.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(task.updatedAt).toString()
                )
                com.example.app.server.KtorServer.addTaskWithBroadcast(serverTask)
                Log.d("NoteViewModel", "Task from chat synced to web server")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to sync chat task to web server", e)
            }
            
            // Refresh tasks list to ensure it shows up immediately
            loadTasks()
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating task from chat", e)
        }
    }
    
    // Extract the actual task content from a message containing task creation request
    private fun extractTaskFromMessage(message: String): String {
        // If the message is just "task please" or similar, check conversation history
        if (message.trim().lowercase().matches(Regex("task\\s*please?|task|please"))) {
            // Look for task content in recent chat history
            val recentMessages = _chatMessages.value.takeLast(5)
            for (chatMessage in recentMessages.reversed()) {
                if (chatMessage.isUser) {
                    val extractedFromHistory = extractTaskFromMessageContent(chatMessage.content)
                    if (extractedFromHistory != chatMessage.content && extractedFromHistory.isNotBlank()) {
                        return extractedFromHistory
                    }
                }
            }
            return "New task"
        }
        
        return extractTaskFromMessageContent(message)
    }
    
    private fun extractTaskFromMessageContent(message: String): String {
        // Common patterns for task creation requests
        val patterns = listOf(
            // "I need to fix the car" -> "fix the car"
            Regex("I\\s+need\\s+to\\s+(.+?)(?:\\s+(?:create|make|add)\\s+(?:a\\s+)?task.*?|$)", RegexOption.IGNORE_CASE),
            // "I have to fix the car" -> "fix the car"
            Regex("I\\s+have\\s+to\\s+(.+?)(?:\\s+(?:create|make|add)\\s+(?:a\\s+)?task.*?|$)", RegexOption.IGNORE_CASE),
            // "I want to fix the car" -> "fix the car"
            Regex("I\\s+want\\s+to\\s+(.+?)(?:\\s+(?:create|make|add)\\s+(?:a\\s+)?task.*?|$)", RegexOption.IGNORE_CASE),
            // "I should fix the car" -> "fix the car"
            Regex("I\\s+should\\s+(.+?)(?:\\s+(?:create|make|add)\\s+(?:a\\s+)?task.*?|$)", RegexOption.IGNORE_CASE),
            // "create a task to fix the car" -> "fix the car"
            Regex("(?:create|make|add)\\s+(?:a\\s+)?task\\s+to\\s+(.+)", RegexOption.IGNORE_CASE),
            // "create a task for fixing the car" -> "fixing the car"
            Regex("(?:create|make|add)\\s+(?:a\\s+)?task\\s+for\\s+(.+)", RegexOption.IGNORE_CASE),
            // "fix the car create a task for that" -> "fix the car"
            Regex("(.+?)\\s+(?:create|make|add)\\s+(?:a\\s+)?task", RegexOption.IGNORE_CASE)
        )
        
        // Try each pattern to extract task content
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val extracted = match.groupValues[1].trim()
                if (extracted.isNotBlank() && !extracted.matches(Regex("\\b(?:that|this|it|please)\\b", RegexOption.IGNORE_CASE))) {
                    return extracted
                }
            }
        }
        
        // If no pattern matches, clean up the message
        val cleanedMessage = message
            .replace(Regex("(?:create|make|add)\\s+(?:a\\s+)?task", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(?:for\\s+)?(?:that|this|it)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\bplease\\b", RegexOption.IGNORE_CASE), "")
            .trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
        
        return if (cleanedMessage.isNotBlank()) cleanedMessage else message.take(50)
    }
    
    // Save chat conversation as note
    private suspend fun saveChatAsNote(userMessage: String, aiResponse: String) {
        try {
            val note = Note(
                title = "Chat Note - ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                snippet = "User: $userMessage",
                transcript = "User: $userMessage\n\nAI: $aiResponse",
                createdAt = System.currentTimeMillis()
            )
            
            repository.insertNote(note)
            Log.d("NoteViewModel", "Saved chat as note: ${note.title}")
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error saving chat as note", e)
        }
    }

    fun sendChatMessageWithImage(message: String, imageUri: android.net.Uri, context: android.content.Context) = viewModelScope.launch {
        // Add user message with image context - store the imageUri for thumbnail display
        val userMessage = ChatMessage(
            content = if (message.isBlank()) "I've shared an image with you" else message,
            isUser = true,
            imageUri = imageUri.toString()
        )
        repository.insertChatMessage(userMessage)
        
        // Set loading state
        _isAiLoading.value = true
        
        try {
            // Process image with Vision API to get description
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                // Get image analysis from OpenAI Vision API
                val imageAnalysis = analyzeImageWithVision(bitmap)
                
                // Also extract any text with OCR for additional context
                val ocrText = performOCRRaw(imageUri, context)
                
                // If the user didn't provide a message, use the image analysis directly
                // If they did provide a message, combine their question with the image analysis
                val aiResponse = if (message.isBlank() || message == "I've shared an image with you") {
                    // User just shared an image without a specific question
                    buildString {
                        append(imageAnalysis)
                        if (ocrText.isNotBlank()) {
                            append("\n\nI can also see this text in the image: \"$ocrText\"")
                        }
                    }
                } else {
                    // User asked a specific question about the image
                    // Get current hour for contextual greetings
                    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    val timeContext = when (currentHour) {
                        in 5..11 -> "morning"
                        in 12..17 -> "afternoon" 
                        in 18..21 -> "evening"
                        else -> "night"
                    }
                    
                    // Create detailed context for the question
                    val imageContext = buildString {
                        append("Based on what I can see in the image: $imageAnalysis")
                        if (ocrText.isNotBlank()) {
                            append("\nText visible in the image: \"$ocrText\"")
                        }
                    }
                    
                    // Use GPT to answer the specific question with image context
                    val messages = mutableListOf<Message>()
                    messages.add(Message(role = "system", content = """
                        You are Logion AI, a helpful assistant that can analyze images and answer questions about them.
                        
                        Current context: It's currently $timeContext time for the user.
                        
                        The user has uploaded an image and asked a question about it. Here's what I can see in the image:
                        $imageContext
                        
                        Answer their question based on what you can see in the image. Be specific, helpful, and conversational.
                        
                        Remember this image analysis for any follow-up questions in this conversation.
                    """.trimIndent()))
                    
                    // Add recent chat history (last 5 messages for context) with image awareness
                    _chatMessages.value.takeLast(5).forEach { chatMsg ->
                        val messageContent = if (chatMsg.imageUri.isNullOrEmpty()) {
                            chatMsg.content
                        } else {
                            if (chatMsg.isUser) {
                                "${chatMsg.content} [User shared an image in this message]"
                            } else {
                                chatMsg.content
                            }
                        }
                        
                        messages.add(Message(
                            role = if (chatMsg.isUser) "user" else "assistant",
                            content = messageContent
                        ))
                    }
                    
                    val request = GPTRequest(messages = messages)
                    val response = RetrofitInstance.api.summarizeText(request)
                    response.body()?.choices?.firstOrNull()?.message?.content?.trim() 
                        ?: "I can see the image you uploaded. Based on what I see: $imageAnalysis"
                }
                
                // Add AI response with the image analysis
                val aiMessage = ChatMessage(content = aiResponse, isUser = false)
                repository.insertChatMessage(aiMessage)
                
                // Check for note/task creation requests in image messages too
                checkForNoteTaskCreation(message, aiResponse)
                
            } else {
                // Fallback if image can't be processed
                val errorMessage = ChatMessage(
                    content = "I can see you uploaded an image, but I'm having trouble processing it. Could you try uploading it again?",
                    isUser = false
                )
                repository.insertChatMessage(errorMessage)
            }
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error processing image for chat", e)
            val errorMessage = ChatMessage(
                content = "I can see your image upload, but I'm having trouble analyzing it right now. Please try again.",
                isUser = false
            )
            repository.insertChatMessage(errorMessage)
        } finally {
            _isAiLoading.value = false
        }
    }
    
    fun clearChatHistory() = viewModelScope.launch {
        try {
            repository.clearChatHistory()
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error clearing chat history", e)
        }
    }

    // ======================= VOICE INTERFACE FUNCTIONALITY =======================
    
    // Clear voice session to start fresh conversation
    fun clearVoiceSession() {
        _voiceSessionHistory.value = emptyList()
        _voiceText.value = ""
    }
    
    fun startListening(context: Context) {
        if (_isListening.value || _isProcessing.value) return
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    _isListening.value = true
                }
                
                override fun onBeginningOfSpeech() {
                    // Stop TTS when user starts speaking
                    stopSpeaking()
                }
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                
                override fun onError(error: Int) {
                    _isListening.value = false
                    Log.e("NoteViewModel", "Speech recognition error: $error")
                }
                
                override fun onResults(results: android.os.Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _voiceText.value = text
                        // Auto-process the voice command when speech ends
                        processVoiceCommand(text)
                    }
                }
                
                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _voiceText.value = text
                }
                
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            
            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.startListening(intent)
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error starting speech recognition", e)
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
        stopSpeaking() // Also stop TTS when stopping listening
    }
    
    fun clearVoiceText() {
        _voiceText.value = ""
    }
    
    // Voice session management
    fun startNewVoiceSession() {
        Log.d("NoteViewModel", "Starting new voice session")
        _voiceSessionHistory.value = emptyList()
        clearVoiceText()
        stopSpeaking() // Stop any ongoing TTS
        _shouldAutoRestart.value = true // Enable auto-restart for this session
    }
    
    fun endVoiceSession() {
        Log.d("NoteViewModel", "Ending voice session")
        stopListening()
        stopSpeaking()
        _shouldAutoRestart.value = false
    }
    
    fun saveVoiceSessionAsNote() = viewModelScope.launch {
        try {
            val sessionHistory = _voiceSessionHistory.value
            Log.d("NoteViewModel", "=== SAVE VOICE SESSION START ===")
            Log.d("NoteViewModel", "Voice session messages: ${sessionHistory.size}")
            
            if (sessionHistory.isNotEmpty()) {
                // Convert voice session to transcript format
                val transcript = sessionHistory.joinToString("\n\n") { message ->
                    if (message.isUser) "User: ${message.content}" else "Assistant: ${message.content}"
                }
                
                Log.d("NoteViewModel", "Voice session transcript: '$transcript'")
                
                // Extract summary and tasks
                val result = extractSummaryAndTasksWithOpenAI(transcript)
                val summaryOut = result?.first ?: ""
                val tasks = result?.second ?: emptyList<String>()
                
                Log.d("NoteViewModel", "Extracted summary: '$summaryOut'")
                Log.d("NoteViewModel", "Extracted tasks: $tasks")
                
                // Use fallback if OpenAI fails
                val finalSummary = if (summaryOut.isBlank()) {
                    "Voice conversation with AI Assistant"
                } else summaryOut
                
                // Create JSON snippet
                val json = org.json.JSONObject()
                json.put("summary", finalSummary)
                if (tasks.isNotEmpty()) json.put("tasks", org.json.JSONArray(tasks))
                
                // Generate smart title
                val generatedTitle = generateSmartTitle(transcript)
                val finalTitle = if (generatedTitle.isBlank() || generatedTitle == "Untitled") {
                    "Voice Chat - ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                } else generatedTitle
                
                // Create and save note
                val note = Note(
                    title = finalTitle,
                    snippet = json.toString(),
                    transcript = transcript
                )
                
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "Voice session note saved successfully: '$finalTitle'")
                Log.d("NoteViewModel", "=== SAVE VOICE SESSION SUCCESS ===")
                
            } else {
                Log.d("NoteViewModel", "Voice session is empty - nothing to save")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE VOICE SESSION ERROR ===", e)
        }
    }
    
    private fun speakText(text: String) {
        try {
            // Use OpenAI TTS if available, otherwise fall back to Android TTS
            if (openAITTS != null) {
                Log.d("NoteViewModel", "Using OpenAI TTS: $text")
                
                // Get preferred voice from SharedPreferences
                val sharedPrefs = getApplication<Application>().getSharedPreferences("app_preferences", Application.MODE_PRIVATE)
                val preferredVoice = sharedPrefs.getString("preferred_voice", "alloy") ?: "alloy"
                
                openAITTS?.speak(
                    text = text,
                    voice = preferredVoice,
                    onReady = {
                        Log.d("NoteViewModel", "OpenAI TTS started speaking")
                        _isSpeaking.value = true
                    },
                    onComplete = {
                        Log.d("NoteViewModel", "OpenAI TTS finished - auto-restarting listening")
                        _isSpeaking.value = false
                        // Auto-restart listening after TTS finishes
                        viewModelScope.launch {
                            delay(500) // Small delay after TTS completes
                            if (!_isListening.value && !_isProcessing.value && _shouldAutoRestart.value) {
                                startListening(getApplication<Application>().applicationContext)
                            }
                        }
                    },
                    onError = { error ->
                        Log.e("NoteViewModel", "OpenAI TTS error: $error")
                        _isSpeaking.value = false
                        // Fall back to Android TTS on error
                        useAndroidTTS(text)
                    }
                )
            } else {
                // Use Android TTS as fallback
                useAndroidTTS(text)
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error in speakText", e)
            useAndroidTTS(text)
        }
    }
    
    private fun useAndroidTTS(text: String) {
        try {
            if (ttsReady && tts != null) {
                // Set speech parameters for more natural voice
                tts?.setSpeechRate(1.1f) // Slightly faster for natural flow
                tts?.setPitch(1.05f) // Slightly higher pitch for friendliness
                
                // Make text more natural
                val naturalText = makeTextMoreNatural(text)
                
                val utteranceId = "TTS_ID_${System.currentTimeMillis()}"
                
                // Add listener for when TTS finishes
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("NoteViewModel", "Android TTS started")
                        _isSpeaking.value = true
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        Log.d("NoteViewModel", "Android TTS finished - auto-restarting listening")
                        _isSpeaking.value = false
                        // Auto-restart listening after AI finishes speaking
                        viewModelScope.launch {
                            delay(500) // Small delay to let TTS fully finish
                            if (!_isListening.value && !_isProcessing.value) {
                                startListening(getApplication<Application>().applicationContext)
                            }
                        }
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e("NoteViewModel", "Android TTS error - auto-restarting listening anyway")
                        _isSpeaking.value = false
                        viewModelScope.launch {
                            delay(500)
                            if (!_isListening.value && !_isProcessing.value) {
                                startListening(getApplication<Application>().applicationContext)
                            }
                        }
                    }
                })
                
                tts?.speak(naturalText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                Log.d("NoteViewModel", "Using Android TTS: $naturalText")
            } else {
                Log.w("NoteViewModel", "TTS not ready, cannot speak text")
                // Initialize TTS if it's not ready
                if (tts == null) {
                    initializeTTS()
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error with Android TTS", e)
        }
    }
    
    private fun makeTextMoreNatural(text: String): String {
        // Make AI responses sound more natural and conversational
        return text
            .replace(". ", ". ") // Ensure proper spacing
            .replace("! ", "! ")
            .replace("? ", "? ")
            .replace(", ", ", ")
            // Remove robotic phrases and make more natural
            .replace("I am ", "I'm ")
            .replace("I will ", "I'll ")
            .replace("You are ", "You're ")
            .replace("It is ", "It's ")
            .replace("That is ", "That's ")
            .replace("We are ", "We're ")
            .replace("They are ", "They're ")
            .replace("Cannot ", "Can't ")
            .replace("Do not ", "Don't ")
            .replace("Will not ", "Won't ")
            .replace("Should not ", "Shouldn't ")
            .trim()
    }
    
    private fun stopSpeaking() {
        try {
            // Stop OpenAI TTS if it's being used
            openAITTS?.stopSpeaking()
            
            // Stop Android TTS
            tts?.stop()
            
            // Reset speaking state
            _isSpeaking.value = false
            
            Log.d("NoteViewModel", "Stopped all TTS")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error stopping TTS", e)
            // Still reset the state even if there's an error
            _isSpeaking.value = false
        }
    }
    
    private fun initializeTTS() {
        try {
            tts = TextToSpeech(getApplication<Application>().applicationContext, this)
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error initializing TTS", e)
        }
    }
    
    private fun isTaskRelated(text: String): Boolean {
        val taskKeywords = listOf(
            "need to", "have to", "must", "should", "fix", "repair", "do", "complete",
            "finish", "buy", "get", "pick up", "call", "contact", "schedule", "book",
            "appointment", "meeting", "remind me", "task", "todo", "to do"
        )
        
        return taskKeywords.any { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }
    
    private suspend fun createNoteFromVoice(originalText: String, aiResponse: String) {
        try {
            // Get the full conversation from chat messages instead of just the last exchange
            val chatMessages = _chatMessages.value
            
            if (chatMessages.isNotEmpty()) {
                // Convert chat messages to transcript format like saveChatAsNote does
                val fullTranscript = chatMessages.joinToString("\n\n") { message ->
                    if (message.isUser) "User: ${message.content}" else "Assistant: ${message.content}"
                }
                
                // Generate a smart title using the full conversation
                val generatedTitle = generateSmartTitle(fullTranscript)
                
                // Extract summary and tasks from the full conversation like saveChatAsNote does
                val result = extractSummaryAndTasksWithOpenAI(fullTranscript)
                val summaryOut = result?.first ?: "Conversation summary"
                val tasks = result?.second ?: emptyList<String>()
                
                // Create JSON snippet same as saveChatAsNote
                val json = org.json.JSONObject()
                json.put("summary", summaryOut)
                if (tasks.isNotEmpty()) json.put("tasks", org.json.JSONArray(tasks))
                
                val note = Note(
                    title = generatedTitle,
                    transcript = fullTranscript, // Full conversation, not just original text
                    snippet = json.toString()    // Proper JSON snippet with summary
                )
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "Voice note created successfully with title: $generatedTitle")
                Log.d("NoteViewModel", "Note contains full conversation with ${chatMessages.size} messages")
            } else {
                // Fallback if no chat messages (shouldn't happen, but just in case)
                val generatedTitle = generateSmartTitle(originalText)
                val note = Note(
                    title = generatedTitle,
                    transcript = "User: $originalText\n\nAssistant: $aiResponse",
                    snippet = """{"summary":"$aiResponse"}"""
                )
                repository.noteDao.insert(note)
                Log.d("NoteViewModel", "Voice note created with fallback method")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating voice note", e)
        }
    }
    
    private suspend fun generateSmartTitle(text: String): String {
        return try {
            val messages = listOf(
                Message(role = "system", content = """
                    Generate a concise, descriptive title (3-6 words max) for this note content.
                    The title should capture the main topic or action.
                    Examples:
                    - "I need to buy groceries tomorrow" -> "Grocery Shopping"
                    - "Meeting with client about project updates" -> "Client Project Meeting"
                    - "Remember to call mom about dinner plans" -> "Call Mom About Dinner"
                    - "Ideas for the new marketing campaign" -> "Marketing Campaign Ideas"
                    
                    Return ONLY the title, no quotes or extra text.
                """.trimIndent()),
                Message(role = "user", content = text)
            )
            
            val request = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(request)
            val title = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                ?.replace("\"", "") // Remove any quotes
                ?.take(50) // Limit length
                ?: generateFallbackTitle(text)
            
            title
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error generating title with AI, using fallback", e)
            generateFallbackTitle(text)
        }
    }
    
    private fun generateFallbackTitle(text: String): String {
        // Fallback title generation if AI fails
        val cleanText = text.trim()
        
        // Extract first few words as title
        val words = cleanText.split(" ").take(4)
        val title = words.joinToString(" ")
        
        return if (title.length > 30) {
            title.take(27) + "..."
        } else {
            title.ifBlank { "Voice Note - ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}" }
        }
    }
    
    // Smart task detection with conversation context
    private fun detectTaskIntent(text: String, aiResponse: String, recentHistory: List<ChatMessage>): Boolean {
        val lowerText = text.lowercase().trim()
        
        // Direct task creation requests
        val userWantsTask = text.contains("create a task", ignoreCase = true) ||
                           text.contains("make a task", ignoreCase = true) ||
                           text.contains("add a task", ignoreCase = true) ||
                           text.contains("task for that", ignoreCase = true) ||
                           text.contains("task please", ignoreCase = true) ||
                           text.matches(Regex(".*\\btask\\b.*", RegexOption.IGNORE_CASE))
        
        // Check if AI asked about creating a task and user confirmed
        val aiAskedAboutTask = recentHistory.takeLast(3).any { message ->
            !message.isUser && (
                message.content.contains("create a task", ignoreCase = true) ||
                message.content.contains("Should I create a task", ignoreCase = true) ||
                message.content.contains("want a task", ignoreCase = true) ||
                message.content.contains("task for this", ignoreCase = true) ||
                message.content.contains("save it as a note", ignoreCase = true) // This usually comes with task option
            )
        }
        
        // User confirmations after AI asked about task
        val userConfirmsTask = aiAskedAboutTask && (
            lowerText == "yes" ||
            lowerText == "task" ||
            lowerText == "create task" ||
            lowerText == "make task" ||
            lowerText == "task please" ||
            lowerText.contains("yes") && lowerText.contains("task") ||
            lowerText.startsWith("yes") ||
            lowerText == "sure" ||
            lowerText == "ok" ||
            lowerText == "okay"
        )
        
        // AI confirms task creation
        val aiConfirmsTask = aiResponse.contains("creating task", ignoreCase = true) ||
                            aiResponse.contains("I'll create a task", ignoreCase = true) ||
                            aiResponse.contains("got it", ignoreCase = true) && (userWantsTask || userConfirmsTask)
        
        Log.d("NoteViewModel", "Task detection - userWantsTask: $userWantsTask, userConfirmsTask: $userConfirmsTask, aiConfirmsTask: $aiConfirmsTask, aiAskedAboutTask: $aiAskedAboutTask")
        Log.d("NoteViewModel", "Text: '$text', AI Response: '$aiResponse'")
        
        return userWantsTask || userConfirmsTask || aiConfirmsTask
    }
    
    // Smart note detection 
    private fun detectNoteIntent(text: String, aiResponse: String): Boolean {
        val userWantsNote = text.contains("create a note", ignoreCase = true) ||
                           text.contains("make a note", ignoreCase = true) ||
                           text.contains("save as note", ignoreCase = true) ||
                           text.contains("note this", ignoreCase = true)
        
        val aiConfirmsNote = aiResponse.contains("creating a note", ignoreCase = true) ||
                            aiResponse.contains("I'll create a note", ignoreCase = true)
        
        return userWantsNote || aiConfirmsNote
    }
    
    // Extract task content with conversation context awareness
    private fun extractTaskFromConversation(currentText: String, recentHistory: List<ChatMessage>): String {
        Log.d("NoteViewModel", "Extracting task from: '$currentText'")
        Log.d("NoteViewModel", "Recent history size: ${recentHistory.size}")
        
        // Check if current text is a confirmation (yes, task, etc.)
        val isConfirmation = currentText.trim().lowercase().matches(
            Regex("^(yes|yeah|yep|yup|sure|ok|okay|please|task please|create task|make task|add task|task|do it|go ahead|let's do it).*", RegexOption.IGNORE_CASE)
        )
        
        Log.d("NoteViewModel", "Is confirmation: $isConfirmation")
        
        if (isConfirmation) {
            Log.d("NoteViewModel", "Current message is a confirmation, searching conversation for actual task...")
            
            // Look for the actual task content in recent conversation
            for (message in recentHistory.reversed()) {
                Log.d("NoteViewModel", "Checking history: isUser=${message.isUser}, content='${message.content}'")
                if (message.isUser && message.content != currentText) {
                    val taskFromHistory = extractMainActionFromMessage(message.content)
                    Log.d("NoteViewModel", "Extracted from '${message.content}': '$taskFromHistory'")
                    
                    if (taskFromHistory.isNotBlank()) {
                        Log.d("NoteViewModel", "Found task in conversation history: '$taskFromHistory'")
                        return taskFromHistory
                    }
                }
            }
            
            Log.d("NoteViewModel", "No task found in history, using fallback for confirmation")
            return "New task"
        }
        
        // Not a confirmation - try direct extraction
        val directExtraction = extractTaskFromMessageContent(currentText)
        Log.d("NoteViewModel", "Direct extraction: '$directExtraction'")
        
        if (directExtraction != currentText && directExtraction.isNotBlank() && 
            !directExtraction.matches(Regex("\\b(?:new task|task|please|that|this)\\b", RegexOption.IGNORE_CASE))) {
            Log.d("NoteViewModel", "Using direct extraction: '$directExtraction'")
            return directExtraction
        }
        
        // Look for task content in recent conversation anyway
        for (message in recentHistory.reversed()) {
            Log.d("NoteViewModel", "Checking history message: isUser=${message.isUser}, content='${message.content}'")
            if (message.isUser) {
                val taskFromHistory = extractMainActionFromMessage(message.content)
                Log.d("NoteViewModel", "Extracted from history: '$taskFromHistory'")
                if (taskFromHistory.isNotBlank() && taskFromHistory != message.content) {
                    Log.d("NoteViewModel", "Found task content in history: '$taskFromHistory' from message: '${message.content}'")
                    return taskFromHistory
                }
            }
        }
        
        // Fallback: clean up current text
        val fallback = extractTaskFromMessageContent(currentText).ifBlank { "New task" }
        Log.d("NoteViewModel", "Using fallback: '$fallback'")
        return fallback
    }
    
    // Extract the main action from a message (what the user wants to do)
    private fun extractMainActionFromMessage(message: String): String {
        val patterns = listOf(
            // "I need to go to grocery store" -> "go to grocery store"
            Regex("I\\s+need\\s+to\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "I have to go to grocery store" -> "go to grocery store"
            Regex("I\\s+have\\s+to\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "I want to go to grocery store" -> "go to grocery store"
            Regex("I\\s+want\\s+to\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "I should go to grocery store" -> "go to grocery store"
            Regex("I\\s+should\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "I must go to grocery store" -> "go to grocery store"
            Regex("I\\s+must\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "I gotta go to grocery store" -> "go to grocery store"
            Regex("I\\s+(?:gotta|got to)\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "remind me to go to grocery store" -> "go to grocery store"
            Regex("(?:remind me to|don't forget to)\\s+(.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE),
            // "go to grocery store" (direct action) -> "go to grocery store"
            Regex("^((?:go to|visit|buy|get|pick up|call|email|fix|repair|clean|organize|schedule|book|cancel|update)\\s+.+?)(?:\\s+(?:create|make|add|save|note|task).*?|[.!?]|$)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val extracted = match.groupValues[1].trim()
                if (extracted.isNotBlank()) {
                    Log.d("NoteViewModel", "Extracted action: '$extracted' from message: '$message'")
                    return extracted
                }
            }
        }
        
        Log.d("NoteViewModel", "No action pattern matched for: '$message'")
        return ""
    }
    
    // Create task with proper context and single voice response
    private suspend fun createTaskFromVoiceContext(taskContent: String, originalText: String, aiResponse: String) {
        Log.d("NoteViewModel", "=== CREATING TASK FROM VOICE CONTEXT ===")
        Log.d("NoteViewModel", "Task content: '$taskContent'")
        Log.d("NoteViewModel", "Original text: '$originalText'")
        
        try {
            val task = Task(
                title = taskContent.take(100),
                description = "", // Remove automatic descriptions
                priority = "Medium",
                dueDate = System.currentTimeMillis(),
                duration = "",
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Log.d("NoteViewModel", "Inserting task: ${task.title}")
            repository.insertTask(task)
            Log.d("NoteViewModel", "Task inserted successfully!")
            
            // Sync to web server in real-time
            try {
                val serverTask = com.example.app.server.ServerTask(
                    id = task.id.toString(),
                    title = task.title,
                    body = task.description,
                    done = task.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(task.updatedAt).toString()
                )
                com.example.app.server.KtorServer.addTaskWithBroadcast(serverTask)
                Log.d("NoteViewModel", "Voice task synced to web server")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to sync voice task to web server", e)
            }
            
            // Refresh tasks list
            loadTasks()
            Log.d("NoteViewModel", "Tasks list refreshed")
            
            // Check tasks after creation
            val allTasks = repository.getAllTasksOnce()
            Log.d("NoteViewModel", "Total tasks in database: ${allTasks.size}")
            allTasks.forEach { t ->
                Log.d("NoteViewModel", "Task: ${t.title}, Due: ${t.dueDate}, Today: ${isToday(t.dueDate)}")
            }
            
            // Single response combining AI response with task confirmation
            val confirmationResponse = if (aiResponse.contains("got it", ignoreCase = true) || 
                                            aiResponse.contains("creating", ignoreCase = true)) {
                "$aiResponse Task '${task.title}' created!"
            } else {
                "Perfect! Created task: ${task.title}"
            }
            
            speakText(confirmationResponse)
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating voice task with context", e)
            speakText("Sorry, I couldn't create that task.")
        }
    }
    
    fun processVoiceCommand(text: String) = viewModelScope.launch {
        _isProcessing.value = true
        
        try {
            // Get conversation context from recent messages
            val recentHistory = _voiceSessionHistory.value.takeLast(6) // Last 3 exchanges
            val conversationContext = if (recentHistory.isNotEmpty()) {
                "Recent conversation:\n" + recentHistory.joinToString("\n") { 
                    "${if (it.isUser) "User" else "AI"}: ${it.content}" 
                }
            } else ""
            
            // Use OpenAI to process the voice command with context
            val messages = mutableListOf<Message>()
            messages.add(Message(role = "system", content = """
                You're Logion AI - a smart, helpful assistant that understands context and conversation flow.
                
                CONVERSATION INTELLIGENCE:
                - Remember what the user mentioned in previous messages
                - If they say "fix the car" first, then "create a task for that" - extract "fix the car" as the task content
                - If they confirm with just "task please" or "yes, task" - use the main action from earlier in the conversation
                - Be contextually aware and extract the ACTUAL task content, not the creation command
                
                RESPONSE RULES:
                - Keep responses brief and natural (1-2 sentences max)
                - Use contractions and casual language
                - When creating tasks/notes, just confirm what you're doing, don't ask again
                
                TASK/NOTE DETECTION:
                - "create a task", "make a task", "add a task", "task please" → Create task
                - "create a note", "make a note", "save as note" → Create note  
                - Unclear actions like "fix the car" → Ask: "Should I create a task for this?"
                
                SMART EXTRACTION:
                - "I need to fix the car" + "create a task" → Task title: "fix the car"
                - "Buy groceries" + "task please" → Task title: "buy groceries"
                - "Create a task for that" → Look for the actual task in conversation history
                
                EXAMPLES:
                User: "I need to fix the car"
                AI: "Should I create a task for this?"
                User: "create a task for that"
                AI: "Got it! Creating task to fix the car."
                
                Current conversation context:
                $conversationContext
            """.trimIndent()))
            
            messages.add(Message(role = "user", content = text))
            
            val request = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(request)
            val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                ?: "Got it!"
            
            // Add to voice session history
            val userMessage = ChatMessage(content = text, isUser = true)
            val aiMessage = ChatMessage(content = aiResponse, isUser = false)
            _voiceSessionHistory.update { it + userMessage + aiMessage }
            
            // Smart task/note detection with context awareness
            val shouldCreateTask = detectTaskIntent(text, aiResponse, recentHistory)
            val shouldCreateNote = detectNoteIntent(text, aiResponse)
            
            Log.d("NoteViewModel", "Voice processing: text='$text', aiResponse='$aiResponse'")
            Log.d("NoteViewModel", "Should create task: $shouldCreateTask, Should create note: $shouldCreateNote")
            
            when {
                shouldCreateTask -> {
                    Log.d("NoteViewModel", "Creating task...")
                    // Use voice session history for context, not chat history
                    val voiceHistory = _voiceSessionHistory.value.takeLast(6)
                    val taskContent = extractTaskFromConversation(text, voiceHistory)
                    Log.d("NoteViewModel", "Extracted task content: '$taskContent'")
                    createTaskFromVoiceContext(taskContent, text, aiResponse)
                }
                shouldCreateNote -> {
                    Log.d("NoteViewModel", "Creating note...")
                    saveVoiceSessionAsNote()
                    speakText(aiResponse)
                }
                else -> {
                    Log.d("NoteViewModel", "Just chatting - no task/note creation")
                    // Just chat - speak the AI response
                    speakText(aiResponse)
                }
            }
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error processing voice command", e)
            val errorResponse = "Sorry, I'm having trouble with that."
            speakText(errorResponse)
        } finally {
            _isProcessing.value = false
        }
    }

    // ======================= TASK FUNCTIONALITY =======================
    
    init {
        // Load tasks when ViewModel is created
        loadTasksOnce()
        loadChatMessages()
    }
    
    private fun loadTasksOnce() = viewModelScope.launch {
        try {
            repository.getAllTasks().collect { tasks ->
                _allTasks.value = tasks
                Log.d("NoteViewModel", "Loaded ${tasks.size} tasks")
                tasks.forEach { task ->
                    Log.d("NoteViewModel", "Task: ${task.title}, due: ${task.dueDate}, isToday: ${isTaskToday(task.dueDate)}")
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error loading tasks", e)
            // Create some sample tasks if none exist
            createSampleTasks()
        }
    }
    
    private fun loadTasks() = viewModelScope.launch {
        try {
            val tasks = repository.getAllTasksOnce()
            _allTasks.value = tasks
            Log.d("NoteViewModel", "Refreshed ${tasks.size} tasks")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error refreshing tasks", e)
        }
    }
    
    private fun isTaskToday(date: Long): Boolean {
        val today = java.util.Calendar.getInstance()
        val taskDate = java.util.Calendar.getInstance().apply { timeInMillis = date }
        
        return today.get(java.util.Calendar.YEAR) == taskDate.get(java.util.Calendar.YEAR) &&
               today.get(java.util.Calendar.DAY_OF_YEAR) == taskDate.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    private fun loadChatMessages() = viewModelScope.launch {
        try {
            repository.getAllChatMessages().collect { messages ->
                _chatMessages.value = messages
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error loading chat messages", e)
        }
    }
    
    private fun createSampleTasks() = viewModelScope.launch {
        try {
            val sampleTasks = listOf(
                Task(
                    title = "Review project proposal",
                    description = "Go through the client's requirements and prepare feedback",
                    priority = "High",
                    dueDate = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
                ),
                Task(
                    title = "Grocery shopping",
                    description = "Buy ingredients for weekend dinner",
                    priority = "Medium",
                    dueDate = System.currentTimeMillis() + (4 * 60 * 60 * 1000) // 4 hours from now
                ),
                Task(
                    title = "Call dentist",
                    description = "Schedule appointment for routine checkup",
                    priority = "Low",
                    dueDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Tomorrow
                )
            )
            sampleTasks.forEach { task ->
                repository.insertTask(task)
                
                // Sync sample tasks to web server too
                try {
                    val serverTask = com.example.app.server.ServerTask(
                        id = task.id.toString(),
                        title = task.title,
                        body = task.description,
                        done = task.isCompleted,
                        updatedAt = java.time.Instant.ofEpochMilli(task.updatedAt).toString()
                    )
                    com.example.app.server.KtorServer.addTaskWithBroadcast(serverTask)
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to sync sample task to web server", e)
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating sample tasks", e)
        }
    }
    
    fun createTask(title: String, description: String, priority: String = "Medium", dueDate: Long = System.currentTimeMillis()) = viewModelScope.launch {
        try {
            val newTask = Task(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            )
            // Insert into local database
            repository.taskDao.insert(newTask)
            
            // Sync to web server in real-time
            try {
                val serverTask = com.example.app.server.ServerTask(
                    id = newTask.id.toString(),
                    title = newTask.title,
                    body = newTask.description,
                    done = newTask.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(newTask.updatedAt).toString()
                )
                com.example.app.server.KtorServer.addTaskWithBroadcast(serverTask)
                    
                Log.d("NoteViewModel", "Task created and synced to web server: $title")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to sync task to web server", e)
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating task", e)
        }
    }

fun createNote(title: String = "New Note", content: String = "") = viewModelScope.launch {
    try {
        val newNote = Note(
            title = title,
            transcript = content,
            snippet = ""
        )
        repository.noteDao.insert(newNote)  // Fixed method name
    } catch (e: Exception) {
        Log.e("NoteViewModel", "Error creating note", e)
    }
}

    fun toggleTaskComplete(taskId: Long) = viewModelScope.launch {
        try {
            val tasks = _allTasks.value
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                repository.toggleTaskComplete(taskId, !task.isCompleted)
                _allTasks.update { tasks -> 
                    tasks.map { 
                        if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it 
                    } 
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error toggling task", e)
        }
    }
    
    fun deleteTask(taskId: Long) = viewModelScope.launch {
        try {
            repository.deleteTask(taskId)
            _allTasks.update { tasks -> tasks.filter { it.id != taskId } }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error deleting task", e)
        }
    }
    
    fun updateTask(
        taskId: Long, 
        title: String, 
        description: String, 
        priority: String,
        dueDate: Long
    ) = viewModelScope.launch {
        try {
            // Get the current task first, then update it
            val currentTasks = _allTasks.value
            val currentTask = currentTasks.find { it.id == taskId }
            if (currentTask != null) {
                val updatedTask = Task(
                    id = taskId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    isCompleted = currentTask.isCompleted,
                    createdAt = currentTask.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                _allTasks.update { tasks ->
                    tasks.map { task ->
                        if (task.id == taskId) updatedTask else task
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error updating task", e)
        }
    }
    
    // Get saved chat conversations from notes
    private val _savedChats = MutableStateFlow<List<Note>>(emptyList())
    val savedChats: StateFlow<List<Note>> = _savedChats.asStateFlow()
    
    init {
        // Load saved chats when ViewModel is created
        loadSavedChats()
    }
    
    private fun loadSavedChats() = viewModelScope.launch {
        try {
            repository.getAllNotes().collect { notes ->
                // Filter notes that are saved chats (contain chat_continuation type)
                val chatNotes = notes.filter { note ->
                    try {
                        if (note.snippet.isNotBlank()) {
                            val chatData = org.json.JSONObject(note.snippet)
                            chatData.optString("type") == "chat_continuation"
                        } else false
                    } catch (e: Exception) {
                        false
                    }
                }.sortedByDescending { it.createdAt }
                
                _savedChats.value = chatNotes
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error loading saved chats", e)
        }
    }
    
    // Helper function to check if a timestamp is today
    private fun isToday(timestamp: Long): Boolean {
        val today = java.util.Calendar.getInstance()
        val taskDate = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(java.util.Calendar.YEAR) == taskDate.get(java.util.Calendar.YEAR) &&
               today.get(java.util.Calendar.DAY_OF_YEAR) == taskDate.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
