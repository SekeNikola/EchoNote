


package com.example.app.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.app.data.Note
import com.example.app.data.Reminder
import com.example.app.data.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
import com.example.app.network.TTSRequest
import com.example.app.worker.ReminderScheduler
import com.example.app.util.ApiKeyProvider
import com.example.app.utils.OpenAITTS
import com.example.app.server.ServerTask
import com.example.app.server.ServerNote
import com.example.app.server.KtorServer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.Locale
import java.util.UUID
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
import com.example.app.data.CheckboxItem
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
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.app.worker.ReminderWorker

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
                Hi there! I'm your personal AI assistant for EchoNote - think of me as your helpful digital companion. 
                
                I'm here to chat with you naturally and help you stay organized. I can:
                1. Have friendly conversations and answer your questions
                2. Help you work with your existing notes (I can see them below)
                3. Help you create lists when you need them - shopping lists, to-dos, travel plans, you name it!
                
                IMPORTANT: When users mention multiple items (like "bread, milk, and eggs" or "create a shopping list with X, Y, Z"), format your response as a numbered or bulleted list. For example:
                
                User: "Create a shopping list with bread, milk, eggs, and tomato"
                You should respond: "I'll create that shopping list for you:
                1. Bread
                2. Milk  
                3. Eggs
                4. Tomato"
                
                Or use bullet points:
                • Bread
                • Milk
                • Eggs
                • Tomato
                
                Let's keep our conversation natural and flowing:
                - I won't rush to create notes or lists unless you actually want them
                - I'll listen to what you need and respond thoughtfully  
                - When you mention multiple items, I'll format them as lists automatically
                - When you do want to save something, just let me know and I'll help organize it
                - I believe in letting you control the pace - no pressure!
                
                Here are your current notes I can reference:
                $notesContext
                
                What's on your mind today?
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
        // Store just the summary as plain text for display in note cards
        updateNoteSnippet(noteId, summaryOut)
    }
    fun updateTranscript(noteId: Long, transcript: String) = viewModelScope.launch {
        repository.updateTranscript(noteId, transcript)
        
        // Broadcast note update to server for web sync
        try {
            // Get the updated note to send to server
            val note = repository.noteDao.getNoteById(noteId).firstOrNull()
            if (note != null) {
                val serverNote = ServerNote(
                    id = note.serverId ?: noteId.toString(),
                    title = note.title,
                    body = note.transcript.ifEmpty { note.snippet },
                    imagePath = note.imagePath,
                    updatedAt = java.time.Instant.now().toString()
                )
                KtorServer.updateNoteWithBroadcast(serverNote)
                Log.d("NoteViewModel", "Note transcript update broadcasted to server: ${note.title}")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Failed to broadcast note transcript update to server", e)
        }
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
                com.example.app.network.Message(role = "system", content = """
    You are a JSON extractor for a task manager app. Always return a single valid JSON object.

    SCHEMA:
    {
      "summary": "<short one-sentence summary>",
      "tasks": [
        {
          "title": "<task text without time/date>",
          "due": "<ISO 8601 datetime if specified, otherwise null>"
        }
      ]
    }

    RULES:
    - Normalize natural language dates/times into ISO 8601.
      Example: "tomorrow at 9am" -> "2025-09-18T09:00:00" (assuming today is 2025-09-17).
    - If the user only says a day (e.g., "next Monday"), resolve it to the next occurrence of that weekday.
    - If no time is mentioned, set "due" to null.
    - Keep "title" free of dates/times, just the action.
    - If multiple tasks are listed, create one object per task.
    - Use the input language for titles, but always use ISO datetime for "due".
    - Do not include commentary, markdown, or anything outside the JSON object.
""".trimIndent()),
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
    
    /**
     * Enhanced extraction function that creates both notes and tasks with detailed structure
     */
    suspend fun extractNotesAndTasksWithStructure(transcript: String): String? {
        Log.d("NoteViewModel", "=== EXTRACT NOTES AND TASKS START ===")
        Log.d("NoteViewModel", "Input transcript: '$transcript'")
        
        val prompt = """
            Extract notes and tasks from the following text and return a structured JSON response.
            
            Text: $transcript
        """.trimIndent()
        
        val request = com.example.app.network.GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                com.example.app.network.Message(role = "system", content = """
    You are a JSON extractor for a comprehensive note-taking app. Always return a single valid JSON object.

    SCHEMA:
    {
      "summary": "<short one-sentence summary>",
      "notes": [
        {
          "title": "<note title>",
          "body": "<detailed note content>",
          "createdAt": "<ISO 8601 datetime>"
        }
      ],
      "tasks": [
        {
          "title": "<task text without time/date>",
          "due": "<ISO 8601 datetime if specified, otherwise null>"
        }
      ]
    }

    RULES:
    - Normalize natural language dates/times into ISO 8601.
      Example: "tomorrow at 9am" -> "2025-09-18T09:00:00" (assuming today is 2025-09-17).
    - If the user only says a day (e.g., "next Monday"), resolve it to the next occurrence of that weekday.
    - If no time is mentioned for tasks, set "due" to null.
    - For notes, always set "createdAt" to current timestamp format.
    - Keep task "title" free of dates/times, just the action.
    - Extract meaningful notes from conversations, meetings, or information.
    - Use the input language for all text fields, but always use ISO datetime for timestamps.
    - Do not include commentary, markdown, or anything outside the JSON object.
    
    EXAMPLE:
    {
      "summary": "Met John about the project timeline",
      "notes": [
        {
          "title": "Meeting with John",
          "body": "Discussed project timeline, deadlines, and responsibilities",
          "createdAt": "2025-09-17T21:00:00"
        }
      ],
      "tasks": [
        {
          "title": "Prepare updated project timeline",
          "due": "2025-09-20T12:00:00"
        }
      ]
    }
""".trimIndent()),
                com.example.app.network.Message(role = "user", content = prompt)
            )
        )
        
        return try {
            val response = com.example.app.network.RetrofitInstance.api.summarizeText(request)
            Log.d("NoteViewModel", "API response received. Success: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                Log.d("NoteViewModel", "Raw API response content: '$content'")
                
                if (!content.isNullOrBlank()) {
                    // Clean the content by removing markdown code blocks
                    val cleanContent = content
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    
                    Log.d("NoteViewModel", "Cleaned JSON content: '$cleanContent'")
                    Log.d("NoteViewModel", "=== EXTRACT NOTES AND TASKS SUCCESS ===")
                    
                    cleanContent
                } else {
                    Log.w("NoteViewModel", "API response content was null or blank")
                    null
                }
            } else {
                Log.w("NoteViewModel", "API request failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== EXTRACT NOTES AND TASKS ERROR ===", e)
            null
        }
    }
    
    fun updateNoteSnippet(noteId: Long, snippet: String) = viewModelScope.launch {
        repository.updateNoteSnippet(noteId, snippet)
        
        // Broadcast the just-updated snippet to server for web UI sync to avoid stale reads
        broadcastUpdate(noteId, bodyOverride = snippet)
    }

    fun updateChecklistState(noteId: Long, checklistState: String) = viewModelScope.launch {
        repository.updateChecklistState(noteId, checklistState)
        
        // Broadcast note update to server for web sync
        broadcastUpdate(noteId)
    }
    
    private suspend fun broadcastUpdate(noteId: Long, bodyOverride: String? = null) {
        try {
            val note = repository.noteDao.getNoteById(noteId).firstOrNull()
            if (note != null) {
                val serverNote = ServerNote(
                    id = note.serverId ?: noteId.toString(),
                    title = note.title,
                    // Prefer the provided override (freshly updated content), otherwise prefer snippet over transcript.
                    body = bodyOverride ?: if (note.snippet.isNotEmpty()) note.snippet else note.transcript,
                    imagePath = note.imagePath,
                    updatedAt = java.time.Instant.now().toString()
                )
                KtorServer.updateNoteWithBroadcast(serverNote)
                Log.d("NoteViewModel", "Note broadcasted: ${note.title}")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Broadcast failed", e)
        }
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
                
                // Use smart title generation
                val title = generateSmartTitle(transcript)
                
                val note = Note(
                    title = title,
                    snippet = summaryOut, // Store just the summary as plain text
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
        try {
            // Get the note before deleting to get its title for server broadcast
            val note = repository.getNoteById(id).first()
            repository.noteDao.deleteById(id)
            
            // Broadcast deletion to server so web UI gets updated
            if (note != null) {
                KtorServer.deleteNoteWithBroadcastByTitle(note.title)
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error deleting note", e)
        }
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

    // Task vs Reminder Choice Dialog
    data class TaskReminderChoice(
        val text: String,
        val aiResponse: String,
        val context: List<ChatMessage>
    )

    // List Creation Choice Dialog
    data class ListCreationChoice(
        val userMessage: String,
        val aiResponse: String,
        val detectedItems: List<String>
    )
    
    private val _showTaskReminderChoice = MutableStateFlow<TaskReminderChoice?>(null)
    val showTaskReminderChoice: StateFlow<TaskReminderChoice?> = _showTaskReminderChoice.asStateFlow()

    private val _showListCreationChoice = MutableStateFlow<ListCreationChoice?>(null)
    val showListCreationChoice: StateFlow<ListCreationChoice?> = _showListCreationChoice.asStateFlow()

    // Multilingual support
    private val _detectedLanguage = MutableStateFlow("en") // Default to English
    val detectedLanguage: StateFlow<String> = _detectedLanguage.asStateFlow()
    
    private val _preferredLanguage = MutableStateFlow("auto") // auto, en, es, fr, de, etc.
    val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()
    
    // Supported languages for Whisper and TTS
    private val supportedLanguages = mapOf(
        "auto" to "Auto Detect",
        "en" to "English",
        "es" to "Spanish", 
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "zh" to "Chinese",
        "ar" to "Arabic",
        "hi" to "Hindi",
        "nl" to "Dutch",
        "sv" to "Swedish",
        "da" to "Danish",
        "no" to "Norwegian",
        "fi" to "Finnish",
        "sr" to "Serbian",
        "sl" to "Slovenian"
    )

    // Voice session chat history - separate from regular chat
    private val _voiceSessionHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val voiceSessionHistory: StateFlow<List<ChatMessage>> = _voiceSessionHistory.asStateFlow()

    private val _shouldAutoRestart = MutableStateFlow(true)
    val shouldAutoRestart: StateFlow<Boolean> = _shouldAutoRestart.asStateFlow()

    // Tasks functionality
    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()
    
    // Reminders functionality
    private val _allReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val allReminders: StateFlow<List<Reminder>> = _allReminders.asStateFlow()

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
        
        // Get the updated note and broadcast to server for web UI sync
        try {
            val updatedNote = repository.getNoteById(id).first()
            updatedNote?.let { note ->
                val serverNote = ServerNote(
                    id = id.toString(),
                    title = title,
                    body = note.snippet, // Use snippet content for the body
                    imagePath = note.imagePath,
                    updatedAt = getCurrentTimestamp()
                )
                
                KtorServer.updateNoteWithBroadcast(serverNote)
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Failed to broadcast note title update", e)
        }
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
    You are a strict JSON extractor for a note-taking app. OUTPUT MUST BE A SINGLE VALID JSON OBJECT ONLY — nothing else.

    SCHEMA (required):
    {
      "summary": "<short summary string (20-40 words max)>",
      "tasks": ["<task 1>", "<task 2>", ...]
    }

    RULES:
    1) If transcript contains clear action items (verbs like buy, call, email, schedule, remind, book, pay, fix, send), put each as a short imperative string in "tasks".
    2) If no action items, set "tasks": [] and put a concise summary in "summary".
    3) Tasks must be concise (e.g., "Buy bread", "Call Mom"), no numbering, no extra punctuation.
    4) Do NOT include assistant instructions, commentary, or explanations — only the JSON object.
    5) Remove duplicates and normalize capitalization only as short phrases.
    6) Return output in the same language as the transcript.

    EXAMPLES:
    Input: "I need to buy bread and milk and call mom."
    Output: {"summary":"","tasks":["Buy bread","Buy milk","Call mom"]}

    Input: "Met with John about the project timeline."
    Output: {"summary":"Met with John about the project timeline.","tasks":[]}
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
                Message(role = "system", content = "Hey there! I'm your voice assistant - I love chatting with you through speech! I understand what you're saying and I'll respond in a warm, natural way. Whether you need help with notes, tasks, or just want to have a conversation, I'm here for you. Just talk to me like you would a friend!"),
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
                // Find the most recent image from chat if any
                val recentImageUri = chatMessages.findLast { it.imageUri != null }?.imageUri
                
                // Convert content URI to actual file path if image exists
                val actualImagePath = recentImageUri?.let { uri ->
                    copyImageToAppStorage(uri)
                }
                
                // Convert chat messages to transcript format
                val transcript = chatMessages.joinToString("\n\n") { message ->
                    if (message.isUser) "User: ${message.content}" else "Assistant: ${message.content}"
                }
                
                Log.d("NoteViewModel", "Full transcript: '$transcript'")
                Log.d("NoteViewModel", "Recent image URI: $recentImageUri")
                Log.d("NoteViewModel", "Actual image path: $actualImagePath")
                
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
                    // Create a better fallback summary from the chat content
                    val userMessages = chatMessages.filter { it.isUser }.map { it.content }
                    val lastUserMessage = userMessages.lastOrNull() ?: ""
                    
                    // Take the first substantial user message or the last one
                    val summaryContent = if (lastUserMessage.length > 20) {
                        lastUserMessage.take(200) + if (lastUserMessage.length > 200) "..." else ""
                    } else {
                        userMessages.find { it.length > 10 }?.take(200) ?: "Chat conversation"
                    }
                    
                    summaryContent
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
                
                // Create and save the note with plain text summary and image
                val note = Note(
                    title = finalTitle,
                    snippet = finalSummary, // Store just the summary as plain text
                    transcript = transcript,
                    imagePath = actualImagePath
                )
                
                Log.d("NoteViewModel", "About to insert note...")
                val noteId = repository.noteDao.insert(note)
                Log.d("NoteViewModel", "Note inserted successfully with ID: $noteId")
                
                // Broadcast to server for web UI sync
                try {
                    val serverNote = ServerNote(
                        id = noteId.toString(),
                        title = finalTitle,
                        body = transcript, // Full conversation for web UI
                        imagePath = actualImagePath,
                        updatedAt = getCurrentTimestamp()
                    )
                    KtorServer.addNoteWithBroadcast(serverNote)
                    Log.d("NoteViewModel", "Note broadcasted to server successfully!")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to broadcast note to server", e)
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
    
    // Save chat conversation as a chat (preserves the conversation structure)
    fun saveChatAsChat(messages: List<ChatMessage>) = viewModelScope.launch {
        try {
            Log.d("NoteViewModel", "=== SAVE CHAT AS CHAT START ===")
            Log.d("NoteViewModel", "Chat messages size: ${messages.size}")
            
            if (messages.isNotEmpty()) {
                // Generate a unique session ID for this chat
                val sessionId = "chat_${System.currentTimeMillis()}"
                
                // Save each message with the session ID
                messages.forEach { message ->
                    val chatMessageWithSession = message.copy(
                        sessionId = sessionId,
                        id = 0 // Let Room auto-generate new IDs
                    )
                    repository.insertChatMessage(chatMessageWithSession)
                }
                
                Log.d("NoteViewModel", "=== SAVE CHAT AS CHAT SUCCESS ===")
                Log.d("NoteViewModel", "Saved ${messages.size} messages with session ID: $sessionId")
            } else {
                Log.d("NoteViewModel", "Chat messages are empty - nothing to save as chat")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE CHAT AS CHAT ERROR ===", e)
            Log.e("NoteViewModel", "Error details: ${e.message}")
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
                    snippet = result?.first ?: "Uploaded audio note", // Use summary as plain text
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
    
    // Copy image from content URI to app storage and return file path
    private fun copyImageToAppStorage(contentUri: String): String? {
        return try {
            val uri = Uri.parse(contentUri)
            val context = getApplication<Application>().applicationContext
            
            // Create app-specific directory for images
            val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EchoNote")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // Generate unique filename
            val fileName = "note_image_${System.currentTimeMillis()}.jpg"
            val imageFile = File(imagesDir, fileName)
            
            // Copy the image from content URI to our app storage
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                imageFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Log.d("NoteViewModel", "Image copied to: ${imageFile.absolutePath}")
            return imageFile.absolutePath
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Failed to copy image to app storage", e)
            null
        }
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
    append("You are an expert visual analyst. Provide a concise, structured description of the image.")
    append("\n\nOUTPUT FORMAT:")
    append("\nHeadline: One short sentence summarizing the image.")
    append("\nDetails: A brief conversational paragraph with what you can identify.")
    append("\nDetected items: A bullet list of clearly identified objects (brand/model if visible).")
    append("\nText found: If OCR text exists, include it exactly under this heading.")
    append("\n\nRULES:")
    append("\n- Be specific: prefer 'computer mouse' over 'device'.")
    append("\n- If uncertain, say 'possibly' rather than inventing details.")
    if (extractedText.isNotBlank()) {
        append("\n\nText found in image: \"$extractedText\"")
    }
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
            snippet = result?.first ?: "$contentType content", // Use summary as plain text
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
            snippet = cleanedSummary, // Store plain text summary
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
                snippet = "This is a test chat note created to verify the save functionality works.", // Plain text
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
                Hey! I'm your personal AI companion in EchoNote - nice to meet you! 
                
                Right now it's $timeContext, and I'm here to chat and help however you need.
                
                ${if (hasRecentImages && currentChatMessages.isNotEmpty()) {
                    "I noticed you shared some images with me earlier in our conversation. Feel free to ask me anything about them - I remember what we discussed!"
                } else {
                    "I love looking at images and talking about what I see, so feel free to share any photos with me anytime."
                }}
                
                Here's how I like to help:
                - I answer your questions in a friendly, down-to-earth way
                - I enjoy having real conversations - not just robotic responses
                - When you share images, I'll tell you exactly what catches my eye
                - I stay focused on what you're actually asking about
                - I'm genuinely here to make your day a little easier
                
                IMPORTANT: When users mention multiple items or ask for lists, always format them as numbered or bulleted lists:
                • Use numbered lists (1. 2. 3.) or bullet points (• - *)
                • Each item on its own line
                • Clear, concise formatting
                • When someone says "create a shopping list with X and Y" or mentions multiple items, immediately create a formatted list with those items
                • Don't ask "anything else?" or "what else?" - just create the list with what they provided
                
                LIST CREATION RULES:
                - If user says "create a list with X and Y" → immediately respond with:
                  1. X
                  2. Y
                - Don't ask for additional items unless they specifically request it
                - Format lists immediately without confirmation prompts
                
                What I always keep in mind:
                - I respond to what you're actually asking, not what I think you might want
                - If you ask about something specific (like "where is the moon"), I give you real, helpful info
                - When you mention multiple items, I format them as proper lists
                - I only talk about images when you've actually shared them with me
                - I don't make assumptions - I work with what you give me
                - My goal is to be genuinely helpful, not just sound smart
                
                So, what's going on? How can I help you today?
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
    private suspend fun checkForNoteTaskCreation(userMessage: String, aiResponse: String, imageUri: String? = null) {
        Log.d("NoteViewModel", "checkForNoteTaskCreation called - userMessage: '$userMessage'")
        Log.d("NoteViewModel", "checkForNoteTaskCreation called - aiResponse: '$aiResponse'")
        
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
        
        Log.d("NoteViewModel", "checkForNoteTaskCreation - userWantsNote: $userWantsNote, userWantsTask: $userWantsTask")
        Log.d("NoteViewModel", "checkForNoteTaskCreation - aiConfirmsNote: $aiConfirmsNote, aiConfirmsTask: $aiConfirmsTask")
        
        // Check for list detection - when user provides list items
        val listItems = detectListItems(userMessage, aiResponse)
        Log.d("NoteViewModel", "checkForNoteTaskCreation - detected list items: $listItems")
        if (listItems.isNotEmpty()) {
            Log.d("NoteViewModel", "checkForNoteTaskCreation - Creating checkbox note with ${listItems.size} items")
            // Automatically create checkbox note when list items are detected
            createCheckboxNoteFromList(userMessage, aiResponse, listItems)
            return
        }
        
        // Priority: explicit user intent > AI confirmation
        when {
            userWantsTask || aiConfirmsTask -> {
                createTaskFromChat(userMessage, aiResponse)
            }
            userWantsNote || aiConfirmsNote -> {
                saveChatAsNote(userMessage, aiResponse, imageUri)
            }
            // If neither is explicitly requested, don't create anything - just chat
        }
    }
    
    // Create task from regular chat
    private suspend fun createTaskFromChat(userMessage: String, aiResponse: String) {
        try {
            // Check if this is a reminder request instead of a task
            if (isReminderRequest(userMessage)) {
                createReminderFromChat(userMessage, aiResponse)
                return
            }
            
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
            
            // Broadcast task to server for web sync
            try {
                val serverTask = ServerTask(
                    id = java.util.UUID.randomUUID().toString(),
                    title = task.title,
                    body = task.description,
                    done = task.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(task.updatedAt).toString()
                )
                KtorServer.addTaskWithBroadcast(serverTask)
                Log.d("NoteViewModel", "Task broadcasted to server: ${task.title}")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to broadcast task to server", e)
            }
            
            // Refresh tasks list to ensure it shows up immediately
            loadTasks()
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating task from chat", e)
        }
    }
    
    // Check if the user message is asking for a reminder instead of a task
    private fun isReminderRequest(message: String): Boolean {
        val reminderKeywords = listOf(
            "remind me", "reminder", "don't forget", "remember to", "remind", "notification",
            "alert me", "notify me", "ping me", "buzz me"
        )
        
        val lowerMessage = message.lowercase()
        return reminderKeywords.any { keyword ->
            lowerMessage.contains(keyword)
        }
    }
    
    // Create reminder from chat conversation
    private suspend fun createReminderFromChat(userMessage: String, aiResponse: String) {
        try {
            val reminderContent = extractReminderFromMessage(userMessage)
            val reminderTime = parseTimeFromMessage(userMessage)
            
            val reminder = Reminder(
                title = reminderContent.take(100), // Limit title length
                description = "From AI chat: ${userMessage.take(200)}", // Add context
                reminderTime = reminderTime,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
            
            repository.insertReminder(reminder)
            Log.d("NoteViewModel", "Created reminder from chat: ${reminder.title} at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(reminderTime))}")
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating reminder from chat", e)
        }
    }
    
    // Parse time from user message 
    private fun parseTimeFromMessage(message: String): Long {
        val now = System.currentTimeMillis()
        val lowerMessage = message.lowercase()
        
        // Time patterns
        when {
            // "in 5 minutes", "in 10 mins", "in 1 hour", "in 2 hours"
            lowerMessage.contains(Regex("in\\s+(\\d+)\\s+(minute|min|minutes|mins)")) -> {
                val match = Regex("in\\s+(\\d+)\\s+(minute|min|minutes|mins)").find(lowerMessage)
                val minutes = match?.groupValues?.get(1)?.toIntOrNull() ?: 60
                return now + (minutes * 60 * 1000)
            }
            lowerMessage.contains(Regex("in\\s+(\\d+)\\s+(hour|hours|hr|hrs)")) -> {
                val match = Regex("in\\s+(\\d+)\\s+(hour|hours|hr|hrs)").find(lowerMessage)
                val hours = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
                return now + (hours * 60 * 60 * 1000)
            }
            // "at 3pm", "at 15:30", "at 9am"
            lowerMessage.contains(Regex("at\\s+(\\d{1,2})(pm|am)")) -> {
                val match = Regex("at\\s+(\\d{1,2})(pm|am)").find(lowerMessage)
                val hour = match?.groupValues?.get(1)?.toIntOrNull() ?: 12
                val isPM = match?.groupValues?.get(2) == "pm"
                val targetHour = if (isPM && hour < 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
                
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                
                // If time has passed today, set for tomorrow
                if (calendar.timeInMillis <= now) {
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
                return calendar.timeInMillis
            }
            lowerMessage.contains(Regex("at\\s+(\\d{1,2}):(\\d{2})")) -> {
                val match = Regex("at\\s+(\\d{1,2}):(\\d{2})").find(lowerMessage)
                val hour = match?.groupValues?.get(1)?.toIntOrNull() ?: 12
                val minute = match?.groupValues?.get(2)?.toIntOrNull() ?: 0
                
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                calendar.set(java.util.Calendar.MINUTE, minute)
                calendar.set(java.util.Calendar.SECOND, 0)
                
                // If time has passed today, set for tomorrow
                if (calendar.timeInMillis <= now) {
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
                return calendar.timeInMillis
            }
            // "tomorrow", "tomorrow morning"
            lowerMessage.contains("tomorrow") -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 9) // Default to 9 AM
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                return calendar.timeInMillis
            }
            // "later", "later today"
            lowerMessage.contains("later") -> {
                return now + (2 * 60 * 60 * 1000) // 2 hours from now
            }
            else -> {
                // Default to 1 hour from now
                return now + (60 * 60 * 1000)
            }
        }
    }
    
    // Extract reminder content from message
    private fun extractReminderFromMessage(message: String): String {
        val reminderPatterns = listOf(
            // "remind me to buy milk" -> "buy milk"
            Regex("remind\\s+me\\s+to\\s+(.+)", RegexOption.IGNORE_CASE),
            // "reminder to buy milk" -> "buy milk"
            Regex("reminder\\s+to\\s+(.+)", RegexOption.IGNORE_CASE),
            // "don't forget to buy milk" -> "buy milk"
            Regex("don't\\s+forget\\s+to\\s+(.+)", RegexOption.IGNORE_CASE),
            // "remember to buy milk" -> "buy milk"
            Regex("remember\\s+to\\s+(.+)", RegexOption.IGNORE_CASE),
            // "alert me about the meeting" -> "the meeting"
            Regex("(?:alert|notify|ping|buzz)\\s+me\\s+(?:about|for|to)\\s+(.+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in reminderPatterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        // If no pattern matches, return the original message
        return message.trim()
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
    
    // Detect list items from user message and AI response
    private fun detectListItems(userMessage: String, aiResponse: String): List<String> {
        val items = mutableListOf<String>()
        
        Log.d("NoteViewModel", "Detecting list items from user: '$userMessage'")
        Log.d("NoteViewModel", "AI response: '$aiResponse'")
        
        // Look for numbered lists in user message
        val numberedPattern = Regex("""(?:^|\n)\s*(\d+)[\.\)]\s*(.+?)(?=\n\s*\d+[\.\)]|\n\s*$|$)""", RegexOption.MULTILINE)
        numberedPattern.findAll(userMessage).forEach { match ->
            val item = match.groupValues[2].trim()
            if (item.isNotBlank() && item.length > 2) {
                items.add(item)
                Log.d("NoteViewModel", "Found numbered item: '$item'")
            }
        }
        
        // Look for bullet lists in user message  
        val bulletPattern = Regex("""(?:^|\n)\s*[-*•]\s*(.+?)(?=\n\s*[-*•]|\n\s*$|$)""", RegexOption.MULTILINE)
        bulletPattern.findAll(userMessage).forEach { match ->
            val item = match.groupValues[1].trim()
            if (item.isNotBlank() && item.length > 2) {
                items.add(item)
            }
        }
        
        // Look for comma-separated lists when user mentions multiple items
        if (items.isEmpty()) {
            // Pattern for "I need to buy X, Y, and Z" or "buy/get X, Y, and Z"
            val commaPattern = Regex("""(?:I\s+(?:need\s+to\s+)?|buy|get|need|purchase|take|bring|pack|remember|with|include|add)\s+(.+)""", RegexOption.IGNORE_CASE)
            val match = commaPattern.find(userMessage)
            if (match != null) {
                val listText = match.groupValues[1]
                Log.d("NoteViewModel", "Found comma pattern match: '$listText'")
                
                // First try to split by commas and "and" - improved handling
                var commaItems = listText.split(Regex(""",\s*(?:and\s+)?|,\s*|\s+and\s+|\s+&\s+"""))
                    .map { it.trim().removePrefix("and ").removePrefix("also ").trim() }
                    .filter { it.isNotBlank() && it.length > 1 && !it.matches(Regex("""^(and|or|plus|also|the|a|an)$""", RegexOption.IGNORE_CASE)) }
                
                Log.d("NoteViewModel", "Comma-split items: $commaItems")
                
                // If we don't have enough items, try splitting by spaces for simple lists
                if (commaItems.size < 2) {
                    val spaceItems = listText.split(" ")
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it.length > 1 && !it.matches(Regex("""^(and|or|plus|also|the|a|an|to|i|need|want)$""", RegexOption.IGNORE_CASE)) }
                    
                    Log.d("NoteViewModel", "Space-split items: $spaceItems")
                    if (spaceItems.size >= 2) {
                        commaItems = spaceItems
                    }
                }
                
                if (commaItems.size >= 2) {
                    items.addAll(commaItems)
                    Log.d("NoteViewModel", "Added comma items: $commaItems")
                }
            }
        }
        
        // Enhanced pattern for "create/make a [type] list with [items]"
        if (items.isEmpty()) {
            val listCreationPattern = Regex("""(?:create|make|build)\s+(?:a\s+)?(?:\w+\s+)?(?:list|checklist).*?(?:with|including?|contains?|of)\s*(.+)""", RegexOption.IGNORE_CASE)
            val match = listCreationPattern.find(userMessage)
            if (match != null) {
                val listText = match.groupValues[1]
                Log.d("NoteViewModel", "Found list creation pattern: '$listText'")
                
                val extractedItems = listText.split(Regex(""",\s*(?:and\s+)?|,\s*|\s+and\s+|\s+&\s+"""))
                    .map { it.trim().removePrefix("and ").removePrefix("also ").trim() }
                    .filter { it.isNotBlank() && it.length > 1 }
                
                Log.d("NoteViewModel", "List creation items: $extractedItems")
                
                if (extractedItems.size >= 2) {
                    items.addAll(extractedItems)
                    Log.d("NoteViewModel", "Added list creation items: $extractedItems")
                }
            }
        }
        
        // Additional pattern specifically for "shopping list with X and Y" without comma
        if (items.isEmpty()) {
            val shoppingPattern = Regex("""(?:create|make|build)\s+(?:a\s+)?(?:shopping|grocery|task|to-?do)\s+list\s+(?:with|of)\s+([^,]+\s+and\s+[^,]+)""", RegexOption.IGNORE_CASE)
            val match = shoppingPattern.find(userMessage)
            if (match != null) {
                val listText = match.groupValues[1]
                Log.d("NoteViewModel", "Found shopping list pattern: '$listText'")
                
                // Split by "and" for simple two-item lists
                val extractedItems = listText.split(Regex("""\s+and\s+|\s+&\s+""", RegexOption.IGNORE_CASE))
                    .map { it.trim() }
                    .filter { it.isNotBlank() && it.length > 1 }
                
                Log.d("NoteViewModel", "Shopping list items: $extractedItems")
                
                if (extractedItems.size >= 2) {
                    items.addAll(extractedItems)
                    Log.d("NoteViewModel", "Added shopping list items: $extractedItems")
                }
            }
        }
        
        // Also check AI response for formatted lists
        if (items.isEmpty()) {
            Log.d("NoteViewModel", "Checking AI response for lists")
            val aiNumberedPattern = Regex("""(?:^|\n)\s*(\d+)[\.\)]\s*(.+?)(?=\n\s*\d+[\.\)]|\n\s*$|$)""", RegexOption.MULTILINE)
            aiNumberedPattern.findAll(aiResponse).forEach { match ->
                val item = match.groupValues[2].trim()
                if (item.isNotBlank() && item.length > 2) {
                    items.add(item)
                    Log.d("NoteViewModel", "Found AI numbered item: '$item'")
                }
            }
            
            val aiBulletPattern = Regex("""(?:^|\n)\s*[-*•]\s*(.+?)(?=\n\s*[-*•]|\n\s*$|$)""", RegexOption.MULTILINE)
            aiBulletPattern.findAll(aiResponse).forEach { match ->
                val item = match.groupValues[1].trim()
                if (item.isNotBlank() && item.length > 2) {
                    items.add(item)
                    Log.d("NoteViewModel", "Found AI bullet item: '$item'")
                }
            }
        }
        
        // Only return items if we found at least 2 meaningful list items
        val finalItems = if (items.size >= 2) items.take(10) else emptyList()
        Log.d("NoteViewModel", "Final detected items: $finalItems")
        return finalItems
    }
    
    // Save chat conversation as note
    private suspend fun saveChatAsNote(userMessage: String, aiResponse: String, imageUri: String? = null) {
        try {
            // Convert image URI to actual file path if image exists
            val actualImagePath = imageUri?.let { uri ->
                copyImageToAppStorage(uri)
            }
            
            // Create a concise summary instead of full chat
            val summary = if (userMessage.length > 100) {
                "${userMessage.take(100)}..."
            } else {
                userMessage
            }
            
            val note = Note(
                title = "Chat Note - ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                snippet = summary,
                transcript = aiResponse, // Keep AI response as transcript
                imagePath = actualImagePath,
                createdAt = System.currentTimeMillis()
            )
            
            repository.insertNote(note)
            Log.d("NoteViewModel", "Saved chat as note: ${note.title}")
            
            // Broadcast note to server for web sync
            try {
                val serverNote = ServerNote(
                    id = java.util.UUID.randomUUID().toString(),
                    title = note.title,
                    body = note.snippet,
                    imagePath = note.imagePath,
                    updatedAt = java.time.Instant.ofEpochMilli(note.createdAt).toString()
                )
                KtorServer.addNoteWithBroadcast(serverNote)
                Log.d("NoteViewModel", "Note broadcasted to server: ${note.title}")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to broadcast note to server", e)
            }
            
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
                        Hi! I'm your AI companion who loves looking at images and chatting about them!
                        
                        Right now it's $timeContext, and you've shared an image with me. Let me tell you what I see:
                        $imageContext
                        
                        I'm here to answer your question about this image in a friendly, conversational way. I'll be specific about what I notice and give you helpful insights based on what's actually in the picture.
                        
                        I'll remember what we discussed about this image, so feel free to ask follow-up questions!
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
                checkForNoteTaskCreation(message, aiResponse, userMessage.imageUri)
                
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

    fun startNewChatSession() = viewModelScope.launch {
        try {
            repository.clearChatHistory()
            Log.d("NoteViewModel", "Started new chat session")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error starting new chat session", e)
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
                    snippet = finalSummary, // Store just the summary as plain text
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
        viewModelScope.launch {
            try {
                // Use language-aware TTS for all speech
                speakTextInLanguage(text, _detectedLanguage.value)
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error in speakText", e)
                useAndroidTTS(text)
            }
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
                    snippet = summaryOut // Store just the summary as plain text
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
                    snippet = aiResponse // Store AI response as plain text
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
    Generate a concise descriptive title (3-6 words) for this note content.
    - Capture the main topic or action.
    - Use Title Case (or natural capitalization), no punctuation, and return ONLY the title (no quotes, no extra text).
    Examples:
    "I need to buy groceries tomorrow" -> "Grocery Shopping"
    "Meeting with client about project updates" -> "Client Project Meeting"
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
            // Parse date/time from the original text
            val parsedDueDate = parseTimeFromMessage(originalText)
            Log.d("NoteViewModel", "Parsed due date: $parsedDueDate (${if (parsedDueDate > System.currentTimeMillis()) "future" else "past/now"})")
            
            val task = Task(
                title = taskContent.take(100),
                description = "", // Remove automatic descriptions
                priority = "Medium",
                dueDate = parsedDueDate,
                duration = "",
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Log.d("NoteViewModel", "Inserting task: ${task.title} due at ${task.dueDate}")
            val taskId = repository.insertTask(task)
            Log.d("NoteViewModel", "Task inserted successfully with ID: $taskId")
            
            // Schedule reminder if due date is in the future
            if (parsedDueDate > System.currentTimeMillis()) {
                val delayMillis = parsedDueDate - System.currentTimeMillis()
                try {
                    ReminderScheduler.scheduleTaskReminder(
                        getApplication<android.app.Application>(),
                        taskId,
                        taskContent,
                        delayMillis
                    )
                    Log.d("NoteViewModel", "Task reminder scheduled for ${delayMillis / 1000} seconds from now")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to schedule task reminder", e)
                }
            }
            
            // Broadcast task to server for web sync
            try {
                val serverTask = ServerTask(
                    id = java.util.UUID.randomUUID().toString(),
                    title = task.title,
                    body = task.description,
                    done = task.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(task.updatedAt).toString()
                )
                KtorServer.addTaskWithBroadcast(serverTask)
                Log.d("NoteViewModel", "Task broadcasted to server: ${task.title}")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to broadcast task to server", e)
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
    
    // ======================= VOICE REMINDER FUNCTIONALITY =======================
    
    // Smart reminder detection
    private fun detectReminderIntent(text: String): Boolean {
        val lowerText = text.lowercase().trim()
        
        return lowerText.contains("remind me") ||
               lowerText.contains("set reminder") ||
               lowerText.contains("reminder in") ||
               lowerText.contains("remind me in") ||
               lowerText.contains("set a reminder") ||
               (lowerText.contains("minute") && (lowerText.contains("remind") || lowerText.contains("reminder"))) ||
               (lowerText.contains("hour") && (lowerText.contains("remind") || lowerText.contains("reminder")))
    }
    
    // Extract reminder time in minutes from user text
    private fun extractReminderTime(text: String): Long {
        val lowerText = text.lowercase()
        
        // Pattern: "in X minutes" or "in X mins"
        val minutePattern = Regex("in\\s+(\\d+)\\s+(?:minute|minutes|min|mins)")
        val minuteMatch = minutePattern.find(lowerText)
        if (minuteMatch != null) {
            return minuteMatch.groupValues[1].toLongOrNull() ?: 5L
        }
        
        // Pattern: "in X hours" or "in X hour"
        val hourPattern = Regex("in\\s+(\\d+)\\s+(?:hour|hours|hr|hrs)")
        val hourMatch = hourPattern.find(lowerText)
        if (hourMatch != null) {
            val hours = hourMatch.groupValues[1].toLongOrNull() ?: 1L
            return hours * 60 // Convert to minutes
        }
        
        // Default to 5 minutes if no time specified
        return 5L
    }
    
    // Extract reminder content from user text
    private fun extractReminderContent(text: String): String {
        val lowerText = text.lowercase()
        
        // Pattern: "remind me to [content]"
        val remindPattern = Regex("remind\\s+me\\s+to\\s+(.+?)(?:\\s+in\\s+\\d+|$)", RegexOption.IGNORE_CASE)
        val remindMatch = remindPattern.find(text)
        if (remindMatch != null) {
            return remindMatch.groupValues[1].trim()
        }
        
        // Pattern: "set reminder [content] in X"
        val reminderPattern = Regex("set\\s+(?:a\\s+)?reminder\\s+(.+?)\\s+in\\s+\\d+", RegexOption.IGNORE_CASE)
        val reminderMatch = reminderPattern.find(text)
        if (reminderMatch != null) {
            return reminderMatch.groupValues[1].trim()
        }
        
        // Fallback: extract everything after "remind" keywords
        val fallbackPattern = Regex("(?:remind|reminder)\\s+(?:me\\s+)?(?:to\\s+)?(.+?)(?:\\s+in\\s+|$)", RegexOption.IGNORE_CASE)
        val fallbackMatch = fallbackPattern.find(text)
        if (fallbackMatch != null) {
            val content = fallbackMatch.groupValues[1].trim()
            return if (content.isNotBlank()) content else "Something important"
        }
        
        return "Something important"
    }
    
    // Detect when both task and reminder could apply - needs user choice
    private fun detectTaskOrReminderChoice(text: String): Boolean {
        val lowerText = text.lowercase().trim()
        
        // Look for phrases that could be either a task or reminder with time
        val hasTimeReference = lowerText.contains("tomorrow") ||
                               lowerText.contains("at ") ||
                               lowerText.contains("9am") ||
                               lowerText.contains("9 am") ||
                               lowerText.contains("morning") ||
                               lowerText.contains("afternoon") ||
                               lowerText.contains("evening") ||
                               lowerText.contains("tonight") ||
                               lowerText.contains("pm") ||
                               lowerText.contains("am") ||
                               Regex("\\b\\d{1,2}:\\d{2}\\b").containsMatchIn(lowerText)
        
        val hasActionableContent = lowerText.contains("fix") ||
                                   lowerText.contains("do") ||
                                   lowerText.contains("need to") ||
                                   lowerText.contains("have to") ||
                                   lowerText.contains("call") ||
                                   lowerText.contains("meet") ||
                                   lowerText.contains("buy") ||
                                   lowerText.contains("get") ||
                                   lowerText.contains("work on") ||
                                   lowerText.contains("finish") ||
                                   lowerText.contains("complete")
        
        // Exclude explicit task or reminder requests
        val isExplicitTask = lowerText.contains("create a task") ||
                            lowerText.contains("make a task") ||
                            lowerText.contains("add a task")
        
        val isExplicitReminder = lowerText.contains("remind me") ||
                               lowerText.contains("set reminder") ||
                               lowerText.contains("reminder")
        
        return hasTimeReference && hasActionableContent && !isExplicitTask && !isExplicitReminder
    }

    private fun createVoiceReminder(text: String) = viewModelScope.launch {
        try {
            val reminderMinutes = extractReminderTime(text)
            val reminderContent = extractReminderContent(text)
            
            Log.d("NoteViewModel", "Creating reminder: '$reminderContent' in $reminderMinutes minutes")
            
            // Create actual Reminder entity, not Task
            val reminder = Reminder(
                title = reminderContent,
                description = "Voice reminder set for ${reminderMinutes} minute${if (reminderMinutes != 1L) "s" else ""} from now",
                reminderTime = System.currentTimeMillis() + (reminderMinutes * 60 * 1000),
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
            
            repository.insertReminder(reminder)
            Log.d("NoteViewModel", "Reminder created: ${reminder.title}")
            
            // Schedule notification reminder using the reminder ID
            try {
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(reminderMinutes, TimeUnit.MINUTES)
                    .setInputData(workDataOf(
                        "noteTitle" to reminderContent,
                        "noteId" to reminder.id,
                        "notificationType" to "reminder"
                    ))
                    .build()
                
                WorkManager.getInstance(getApplication()).enqueue(workRequest)
                Log.d("NoteViewModel", "Reminder notification scheduled for ${reminderMinutes} minutes")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to schedule reminder notification", e)
            }
            
            val timeText = if (reminderMinutes == 1L) "1 minute" else "$reminderMinutes minutes"
            speakText("Got it! I'll remind you about '$reminderContent' in $timeText.")
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating reminder", e)
            speakText("Sorry, I couldn't set that reminder.")
        }
    }
    
    // Handle task/reminder choice dialog
    fun showTaskReminderChoiceDialog(text: String, aiResponse: String, context: List<ChatMessage>) {
        _showTaskReminderChoice.value = TaskReminderChoice(text, aiResponse, context)
    }
    
    fun chooseTask() = viewModelScope.launch {
        val choice = _showTaskReminderChoice.value
        if (choice != null) {
            _showTaskReminderChoice.value = null
            
            // Create task from the original request
            val taskContent = extractTaskFromConversation(choice.text, choice.context)
            createTaskFromVoiceContext(taskContent, choice.text, choice.aiResponse)
        }
    }
    
    fun chooseReminder() = viewModelScope.launch {
        val choice = _showTaskReminderChoice.value
        if (choice != null) {
            _showTaskReminderChoice.value = null
            
            // Create reminder from the original request
            createVoiceReminder(choice.text)
        }
    }
    
    fun dismissTaskReminderChoice() {
        _showTaskReminderChoice.value = null
    }

    // List Creation Choice Dialog Functions
    fun showListCreationChoiceDialog(userMessage: String, aiResponse: String, items: List<String>) {
        _showListCreationChoice.value = ListCreationChoice(userMessage, aiResponse, items)
    }
    
    fun createCheckboxNote() = viewModelScope.launch {
        val choice = _showListCreationChoice.value
        if (choice != null) {
            _showListCreationChoice.value = null
            
            // Create a note with checkbox items
            val checkboxContent = buildString {
                choice.detectedItems.forEach { item ->
                    appendLine("- [ ] $item")
                }
            }
            
            val title = generateSmartTitle(choice.detectedItems.joinToString(", "))
            
            val note = Note(
                title = title,
                snippet = checkboxContent,
                transcript = choice.userMessage,
                audioPath = null,
                createdAt = System.currentTimeMillis()
            )
            
            repository.noteDao.insert(note)
            Log.d("NoteViewModel", "Created checkbox note: $title with ${choice.detectedItems.size} items")
        }
    }
    
    fun createRegularNote() = viewModelScope.launch {
        val choice = _showListCreationChoice.value
        if (choice != null) {
            _showListCreationChoice.value = null
            
            // Create a regular note
            saveChatAsNote(choice.userMessage, choice.aiResponse)
        }
    }
    
    fun dismissListCreationChoice() {
        _showListCreationChoice.value = null
    }

    // Automatically create checkbox note from detected list items
    fun createCheckboxNoteFromList(userMessage: String, aiResponse: String, items: List<String>) = viewModelScope.launch {
        try {
            // Create a note with checkbox items
            val checkboxContent = buildString {
                items.forEach { item ->
                    appendLine("- [ ] $item")
                }
            }
            
            val title = generateSmartTitle(items.joinToString(", "))
            
            val note = Note(
                title = title,
                snippet = checkboxContent,
                transcript = checkboxContent, // Store the checkboxes in transcript for editing
                audioPath = null,
                createdAt = System.currentTimeMillis()
            )
            
            repository.noteDao.insert(note)
            Log.d("NoteViewModel", "Auto-created checkbox note: $title with ${items.size} items")
            
            // Clear assistant chat after creating note
            clearAssistantChat()
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating checkbox note from list", e)
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
            Message(role = "system", content = """
    You are Logion AI — a fast, context-aware assistant for short voice commands.
    CONVERSATION INTELLIGENCE:
    - Use recent context to resolve pronouns and references.
    - When user asks to create a task or confirms creation, extract the task text and reply with a single-line confirmation.
    - If the requested action is ambiguous, ask one direct clarifying question.
    RESPONSE RULES:
    - Keep responses concise (one sentence, direct).
    - When extracting a task, output it as a short imperative phrase.
    - Do not perform multiple follow-up questions; ask one focused question if needed.
    EXAMPLE:
    User: "I need to fix the car" -> AI: "Should I create a task for that?" 
    User: "Create a task for that" -> AI: "Got it — creating task: Fix the car."
""".trimIndent())

            
            messages.add(Message(role = "user", content = text))
            
            val request = GPTRequest(messages = messages)
            val response = RetrofitInstance.api.summarizeText(request)
            val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                ?: "Got it!"
            
            // Add to voice session history
            val userMessage = ChatMessage(content = text, isUser = true)
            val aiMessage = ChatMessage(content = aiResponse, isUser = false)
            _voiceSessionHistory.update { it + userMessage + aiMessage }
            
            // Smart task/note/reminder detection with context awareness
            val shouldCreateTask = detectTaskIntent(text, aiResponse, recentHistory)
            val shouldCreateNote = detectNoteIntent(text, aiResponse)
            val shouldCreateReminder = detectReminderIntent(text)
            val needsChoice = detectTaskOrReminderChoice(text)
            
            Log.d("NoteViewModel", "Voice processing: text='$text', aiResponse='$aiResponse'")
            Log.d("NoteViewModel", "Should create task: $shouldCreateTask, Should create note: $shouldCreateNote, Should create reminder: $shouldCreateReminder, Needs choice: $needsChoice")
            
            // Check for list detection in voice commands
            val listItems = detectListItems(text, aiResponse)
            if (listItems.isNotEmpty()) {
                Log.d("NoteViewModel", "Detected list with ${listItems.size} items in voice command")
                createCheckboxNoteFromList(text, aiResponse, listItems)
                speakText("I've created a note with checkboxes for your list items.")
                return@launch
            }
            
            when {
                needsChoice -> {
                    Log.d("NoteViewModel", "Showing task/reminder choice dialog...")
                    showTaskReminderChoiceDialog(text, aiResponse, recentHistory)
                    speakText("Would you like me to create a task or set a reminder for this?")
                }
                shouldCreateReminder -> {
                    Log.d("NoteViewModel", "Creating reminder...")
                    createVoiceReminder(text)
                }
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
        loadRemindersOnce()
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
            repository.getCurrentConversation().collect { messages ->
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
                dueDate = dueDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            repository.insertTask(newTask)
            
            // Broadcast task to server for web sync
            try {
                val serverTask = ServerTask(
                    id = java.util.UUID.randomUUID().toString(),
                    title = newTask.title,
                    body = newTask.description,
                    done = newTask.isCompleted,
                    updatedAt = java.time.Instant.ofEpochMilli(newTask.updatedAt).toString()
                )
                KtorServer.addTaskWithBroadcast(serverTask)
                Log.d("NoteViewModel", "Task broadcasted to server: ${newTask.title}")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Failed to broadcast task to server", e)
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating task", e)
        }
    }

fun createNote(title: String = "New Note", content: String = "", imageUri: String? = null) = viewModelScope.launch {
    try {
        // Convert image URI to actual file path if image exists
        val actualImagePath = imageUri?.let { uri ->
            copyImageToAppStorage(uri)
        }
        
        val newNote = Note(
            title = title,
            transcript = content,
            snippet = content,
            imagePath = actualImagePath,
            createdAt = System.currentTimeMillis()
        )
        repository.insertNote(newNote)
        
        // Broadcast note to server for web sync
        try {
            val serverNote = ServerNote(
                id = java.util.UUID.randomUUID().toString(),
                title = newNote.title,
                body = newNote.snippet,
                imagePath = newNote.imagePath,
                updatedAt = java.time.Instant.ofEpochMilli(newNote.createdAt).toString()
            )
            KtorServer.addNoteWithBroadcast(serverNote)
            Log.d("NoteViewModel", "Note broadcasted to server: ${newNote.title}")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Failed to broadcast note to server", e)
        }
    } catch (e: Exception) {
        Log.e("NoteViewModel", "Error creating note", e)
    }
}

fun addNoteWithBroadcast(title: String, content: String, imageUri: String? = null) = viewModelScope.launch {
    try {
        // Convert image URI to actual file path if image exists
        val actualImagePath = imageUri?.let { uri ->
            copyImageToAppStorage(uri)
        }
        
        // Create server note for broadcasting (server will handle database sync)
        val serverNote = ServerNote(
            id = UUID.randomUUID().toString(), // Generate a unique ID for the server
            title = title,
            body = content,
            imagePath = actualImagePath,
            updatedAt = System.currentTimeMillis().toString()
        )
        
        // Broadcast to server and clients (server will sync to local database)
        KtorServer.addNoteWithBroadcast(serverNote)
    } catch (e: Exception) {
        Log.e("NoteViewModel", "Error creating note with broadcast", e)
    }
}

    fun toggleTaskComplete(taskId: Long) = viewModelScope.launch {
        try {
            val tasks = _allTasks.value
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                val newCompletionStatus = !task.isCompleted
                
                // Update in repository
                repository.toggleTaskComplete(taskId, newCompletionStatus)
                
                // Broadcast task completion status to server for web sync
                try {
                    val updatedTask = task.copy(
                        isCompleted = newCompletionStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                    val serverTask = ServerTask(
                        id = updatedTask.serverId ?: updatedTask.id.toString(),
                        title = updatedTask.title,
                        body = updatedTask.description,
                        done = updatedTask.isCompleted,
                        updatedAt = java.time.Instant.ofEpochMilli(updatedTask.updatedAt).toString()
                    )
                    KtorServer.updateTaskWithBroadcast(serverTask)
                    Log.d("NoteViewModel", "Task completion status broadcasted to server: ${updatedTask.title} = ${newCompletionStatus}")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to broadcast task completion to server", e)
                }
                
                // Update local state
                _allTasks.update { tasks -> 
                    tasks.map { 
                        if (it.id == taskId) it.copy(isCompleted = newCompletionStatus) else it 
                    } 
                }
                
                Log.d("NoteViewModel", "Task $taskId completion toggled to $newCompletionStatus")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error toggling task", e)
        }
    }
    
    fun deleteTask(taskId: Long) = viewModelScope.launch {
        try {
            // Get the task from current state before deleting to get its title for server broadcast
            val task = _allTasks.value.find { it.id == taskId }
            repository.deleteTask(taskId)
            _allTasks.update { tasks -> tasks.filter { it.id != taskId } }
            
            // Broadcast deletion to server so web UI gets updated
            if (task != null) {
                KtorServer.deleteTaskWithBroadcastByTitle(task.title)
            }
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
                    updatedAt = System.currentTimeMillis(),
                    serverId = currentTask.serverId // Preserve serverId
                )
                repository.updateTask(updatedTask)
                
                // Broadcast task update to server for web sync
                try {
                    val serverTask = ServerTask(
                        id = updatedTask.serverId ?: updatedTask.id.toString(),
                        title = updatedTask.title,
                        body = updatedTask.description,
                        done = updatedTask.isCompleted,
                        updatedAt = java.time.Instant.ofEpochMilli(updatedTask.updatedAt).toString()
                    )
                    KtorServer.updateTaskWithBroadcast(serverTask)
                    Log.d("NoteViewModel", "Task update broadcasted to server: ${updatedTask.title}")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to broadcast task update to server", e)
                }
                
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
    
    fun updateTaskCheckboxItems(taskId: Long, checkboxItems: List<CheckboxItem>) = viewModelScope.launch {
        try {
            val currentTasks = _allTasks.value
            val currentTask = currentTasks.find { it.id == taskId }
            if (currentTask != null) {
                val updatedTask = currentTask.copy(
                    checkboxItems = checkboxItems,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                
                // Broadcast task update to server for web sync
                try {
                    val serverTask = ServerTask(
                        id = updatedTask.serverId ?: updatedTask.id.toString(),
                        title = updatedTask.title,
                        body = updatedTask.description,
                        done = updatedTask.isCompleted,
                        updatedAt = java.time.Instant.ofEpochMilli(updatedTask.updatedAt).toString()
                    )
                    KtorServer.updateTaskWithBroadcast(serverTask)
                    Log.d("NoteViewModel", "Task checkbox update broadcasted to server: ${updatedTask.title}")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Failed to broadcast task checkbox update to server", e)
                }
                
                _allTasks.update { tasks ->
                    tasks.map { task ->
                        if (task.id == taskId) updatedTask else task
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error updating task checkbox items", e)
        }
    }
    
    // Helper function to check if a timestamp is today
    private fun isToday(timestamp: Long): Boolean {
        val today = java.util.Calendar.getInstance()
        val taskDate = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(java.util.Calendar.YEAR) == taskDate.get(java.util.Calendar.YEAR) &&
               today.get(java.util.Calendar.DAY_OF_YEAR) == taskDate.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    // Helper function to get current timestamp in ISO format for server compatibility
    private fun getCurrentTimestamp(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return dateFormat.format(java.util.Date())
    }

    // ======================= REMINDER FUNCTIONALITY =======================
    
    private fun loadRemindersOnce() = viewModelScope.launch {
        try {
            repository.getAllReminders().collect { reminders ->
                _allReminders.value = reminders
                Log.d("NoteViewModel", "Loaded ${reminders.size} reminders")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error loading reminders", e)
        }
    }
    
    fun createReminder(title: String, description: String = "", reminderTime: Long) = viewModelScope.launch {
        try {
            val newReminder = Reminder(
                title = title,
                description = description,
                reminderTime = reminderTime,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
            repository.insertReminder(newReminder)
            Log.d("NoteViewModel", "Created reminder: $title")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error creating reminder", e)
        }
    }
    
    fun completeReminder(reminderId: Long) = viewModelScope.launch {
        try {
            repository.markReminderCompleted(reminderId)
            Log.d("NoteViewModel", "Completed reminder: $reminderId")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error completing reminder", e)
        }
    }
    
    fun deleteReminder(reminderId: Long) = viewModelScope.launch {
        try {
            repository.deleteReminderById(reminderId)
            Log.d("NoteViewModel", "Deleted reminder: $reminderId")
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error deleting reminder", e)
        }
    }

    // Export/Import functionality
    suspend fun exportAllData() = repository.exportAllData()
    
    suspend fun importData(exportData: com.example.app.data.ExportData, replaceExisting: Boolean) {
        repository.importData(exportData, replaceExisting)
    }
    
    // ======================= MULTILINGUAL SUPPORT =======================
    
    fun setPreferredLanguage(languageCode: String) {
        _preferredLanguage.value = languageCode
        Log.d("NoteViewModel", "Preferred language set to: $languageCode")
    }
    
    fun getSupportedLanguages(): Map<String, String> = supportedLanguages
    
    // Detect language from text using OpenAI
    private suspend fun detectLanguage(text: String): String {
        try {
            val messages = listOf(
                Message(role = "system", content = """
                    Detect the language of the following text and respond with only the ISO 639-1 language code.
                    
                    Pay special attention to:
                    - Serbian (srpski): Use 'sr' - look for Cyrillic script or Latin with Serbian words
                    - Slovenian (slovenščina): Use 'sl' - similar to Croatian but distinct
                    - Croatian: Use 'hr'
                    - English: Use 'en'
                    - Spanish: Use 'es'
                    - French: Use 'fr'
                    - German: Use 'de'
                    - Italian: Use 'it'
                    - Portuguese: Use 'pt'
                    
                    If uncertain or mixed languages, default to 'en'.
                    Response format: Just the 2-letter language code, nothing else.
                """.trimIndent()),
                Message(role = "user", content = text)
            )
            
            val request = com.example.app.network.GPTRequest(
    model = "gpt-3.5-turbo",
    messages = listOf(
        com.example.app.network.Message(role = "system", content = """
            You are a precise extractor that converts user text into a JSON object only.
            OUTPUT MUST BE A SINGLE VALID JSON OBJECT AND NOTHING ELSE.

            JSON SCHEMA:
            {
              "summary": "<short one-line summary — 1 sentence, 20-40 words max, plain text>",
              "tasks": ["<task 1>", "<task 2>", ...]
            }

            RULES:
            - Extract only real user action items; ignore assistant-side instructions such as "I'll save this".
            - Tasks should be short imperative phrases (e.g., "Buy milk", "Call Alice").
            - Deduplicate tasks, do not invent tasks.
            - If no tasks, return "tasks": [] and a meaningful "summary".
            - Keep language identical to the input language.
            - Do not wrap result in markdown, code fences, or commentary.

            EXAMPLES:
            Input: "I need to buy bread and milk and also call mom."
            Output: {"summary": "","tasks":["Buy bread","Buy milk","Call mom"]}

            Input: "Today I met John and we discussed the project timeline."
            Output: {"summary":"Met John and discussed the project timeline.","tasks":[]}
        """.trimIndent()),
        com.example.app.network.Message(role = "user", content = text)
    )
)

            
            val response = RetrofitInstance.api.summarizeText(request)
            val detectedCode = response.body()?.choices?.firstOrNull()?.message?.content?.trim()?.lowercase()
            
            // Validate the detected language code
            val validCode = if (supportedLanguages.containsKey(detectedCode)) {
                Log.d("NoteViewModel", "Valid language code detected: $detectedCode")
                detectedCode!!
            } else {
                Log.w("NoteViewModel", "Invalid language code '$detectedCode', defaulting to English")
                "en" // Default to English if invalid
            }
            
            _detectedLanguage.value = validCode
            Log.d("NoteViewModel", "Final language detection result: $validCode for text: '${text.take(50)}'")
            return validCode
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Language detection failed for: '${text.take(50)}'", e)
            _detectedLanguage.value = "en"
            return "en"
        }
    }
    
    // Enhanced audio transcription with language support
    private suspend fun transcribeAudioWithLanguage(audioFile: File, languageCode: String? = null): String {
        try {
            val requestFile = audioFile.asRequestBody("audio/wav".toMediaType())
            val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val model = "whisper-1".toRequestBody("text/plain".toMediaType())
            
            // Use preferred language or auto-detect
            val language = when {
                languageCode != null && languageCode != "auto" -> {
                    Log.d("NoteViewModel", "Using specified language for Whisper: $languageCode")
                    languageCode.toRequestBody("text/plain".toMediaType())
                }
                _preferredLanguage.value != "auto" -> {
                    Log.d("NoteViewModel", "Using preferred language for Whisper: ${_preferredLanguage.value}")
                    _preferredLanguage.value.toRequestBody("text/plain".toMediaType())
                }
                else -> {
                    Log.d("NoteViewModel", "Using auto-detection for Whisper")
                    null // Let Whisper auto-detect
                }
            }
            
            val response = RetrofitInstance.api.transcribeAudio(body, model, language)
            val transcription = response.body()?.text ?: ""
            
            Log.d("NoteViewModel", "Whisper transcription result: '$transcription'")
            
            if (transcription.isNotEmpty()) {
                // Detect language from transcription for better context
                val detectedLang = detectLanguage(transcription)
                _detectedLanguage.value = detectedLang
                Log.d("NoteViewModel", "Final detected language: $detectedLang for text: '${transcription.take(50)}'")
            }
            
            return transcription
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error transcribing audio with language support", e)
            return ""
        }
    }
    
    // Enhanced TTS with language support
    private suspend fun speakTextInLanguage(text: String, languageCode: String? = null) {
        try {
            val targetLanguage = languageCode ?: _detectedLanguage.value
            
            // Use OpenAI TTS if available, otherwise fall back to Android TTS
            if (openAITTS != null) {
                Log.d("NoteViewModel", "Using OpenAI TTS for language: $targetLanguage")
                
                // Select appropriate voice for language
                val voice = when (targetLanguage) {
                    "es" -> "nova"   // Good for Spanish
                    "fr" -> "alloy"  // Good for French  
                    "de" -> "echo"   // Good for German
                    "it" -> "fable"  // Good for Italian
                    "pt" -> "onyx"   // Good for Portuguese
                    "ja" -> "shimmer" // Good for Japanese
                    "ko" -> "alloy"  // Good for Korean
                    "zh" -> "nova"   // Good for Chinese
                    "sr" -> "onyx"   // Good for Serbian
                    "sl" -> "echo"   // Good for Slovenian
                    else -> "alloy"  // Default voice for English and others
                }

                openAITTS?.speak(
                    text = text,
                    voice = voice,
                    onReady = {
                        Log.d("NoteViewModel", "OpenAI TTS started speaking in $targetLanguage")
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
            Log.e("NoteViewModel", "Error generating multilingual speech", e)
            // Fallback to Android TTS
            useAndroidTTS(text)
        }
    }
    
    private fun playAudioFile(audioFile: File) {
        try {
            val mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error playing audio file", e)
        }
    }
    
    // Enhanced voice processing with multilingual support
    fun processVoiceCommandMultilingual(audioFile: File) = viewModelScope.launch {
        _isProcessing.value = true
        
        try {
            // Transcribe with language detection
            val transcription = transcribeAudioWithLanguage(audioFile)
            
            if (transcription.isNotEmpty()) {
                // Update voice text
                _voiceText.value = transcription
                
                // Process the command with language context
                val detectedLang = _detectedLanguage.value
                Log.d("NoteViewModel", "Processing voice command with detected language: $detectedLang")
                
                val languageContext = if (detectedLang != "en") {
                    val languageName = supportedLanguages[detectedLang] ?: detectedLang
                    Log.d("NoteViewModel", "User is speaking in $languageName ($detectedLang)")
                    "User is speaking in $languageName. Please respond in the same language ($languageName)."
                } else {
                    Log.d("NoteViewModel", "User is speaking in English")
                    ""
                }
                
                // Get AI response with language context
                val messages = listOf(
                    Message(role = "system", content = """
    You are Logion AI — a fast, context-aware assistant for short voice commands.
    CONVERSATION INTELLIGENCE:
    - Use recent context to resolve pronouns and references.
    - When user asks to create a task or confirms creation, extract the task text and reply with a single-line confirmation.
    - If the requested action is ambiguous, ask one direct clarifying question.
    RESPONSE RULES:
    - Keep responses concise (one sentence, direct).
    - When extracting a task, output it as a short imperative phrase.
    - Do not perform multiple follow-up questions; ask one focused question if needed.
    EXAMPLE:
    User: "I need to fix the car" -> AI: "Should I create a task for that?" 
    User: "Create a task for that" -> AI: "Got it — creating task: Fix the car."
""".trimIndent()),

                    Message(role = "user", content = transcription)
                )
                
                val request = GPTRequest(messages = messages)
                val response = RetrofitInstance.api.summarizeText(request)
                val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                    ?: "I understand."
                
                // Add to voice session history
                val userMessage = ChatMessage(content = transcription, isUser = true)
                val aiMessage = ChatMessage(content = aiResponse, isUser = false)
                _voiceSessionHistory.update { it + userMessage + aiMessage }
                
                // Process intent detection
                processMultilingualIntent(transcription, aiResponse)
                
                // Speak response in detected language
                speakTextInLanguage(aiResponse, detectedLang)
                
            }
            
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error processing multilingual voice command", e)
            speakText("Sorry, I had trouble understanding that.")
        } finally {
            _isProcessing.value = false
        }
    }
    
    private suspend fun processMultilingualIntent(text: String, aiResponse: String) {
        // Enhanced intent detection that works across languages
        val listItems = detectListItems(text, aiResponse)
        if (listItems.isNotEmpty()) {
            createCheckboxNoteFromList(text, aiResponse, listItems)
            return
        }
        
        // Detect task/reminder/note creation in multiple languages
        val taskKeywords = listOf("task", "tarea", "tâche", "aufgabe", "attività", "tarefa", "задача", "タスク", "작업", "任务")
        val reminderKeywords = listOf("remind", "recordar", "rappeler", "erinnern", "ricordare", "lembrar", "напомнить", "思い出させる", "상기시키다", "提醒")
        val noteKeywords = listOf("note", "nota", "note", "notiz", "nota", "nota", "заметка", "ノート", "메모", "笔记")
        
        val lowerText = text.lowercase()
        val lowerAI = aiResponse.lowercase()
        
        when {
            taskKeywords.any { lowerText.contains(it) || lowerAI.contains(it) } -> {
                val taskContent = extractTaskFromMessage(text)
                createTaskFromVoiceContext(taskContent, text, aiResponse)
            }
            reminderKeywords.any { lowerText.contains(it) || lowerAI.contains(it) } -> {
                createVoiceReminder(text)
            }
            noteKeywords.any { lowerText.contains(it) || lowerAI.contains(it) } -> {
                saveVoiceSessionAsNote()
            }
        }
    }
}
