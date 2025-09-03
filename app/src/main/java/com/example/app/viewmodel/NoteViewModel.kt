


package com.example.app.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.app.data.Note
import com.example.app.data.NoteRepository
import kotlinx.coroutines.launch
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
import android.speech.tts.TextToSpeech
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
import android.content.Intent
import android.speech.RecognitionListener
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
            messages.add(Message(role = "system", content = "You are a helpful AI assistant. You can answer general questions, provide information from the internet, and you also have access to the user's notes. If the user's question is about their notes, use the provided notes for context. Otherwise, answer as a general assistant with access to online information. User's notes:\n$notesContext"))
            
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
            Summarize the following text and extract any tasks as a checklist. Respond in JSON: {\"summary\": \"...\", \"tasks\": [ ... ]}\n\nText:\n$transcript
        """.trimIndent()
        
        Log.d("NoteViewModel", "Generated prompt: '$prompt'")
        
        val request = com.example.app.network.GPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                com.example.app.network.Message(role = "system", content = "You are a helpful assistant that summarizes notes and extracts tasks as a checklist."),
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
    
    // Helper function to clean brackets from summaries
    private fun cleanBracketsFromText(text: String): String {
        var cleanText = text.trim()
        if (cleanText.startsWith("[") && cleanText.endsWith("]")) {
            cleanText = cleanText.substring(1, cleanText.length - 1).trim()
        }
        // Also remove brackets if they appear with other characters
        cleanText = cleanText.replace(Regex("^\\[\\s*"), "").replace(Regex("\\s*\\]$"), "")
        return cleanText
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

    // Assistant chat history
    private val _assistantChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val assistantChatHistory: StateFlow<List<Pair<String, String>>> = _assistantChatHistory

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

    // Clear assistant chat history
    fun clearAssistantChat() {
        _assistantChatHistory.value = emptyList()
    }

    // Save chat conversation as a note - same logic as stopAndSaveNote()
    fun saveChatAsNote() = viewModelScope.launch {
        try {
            val chatHistory = _assistantChatHistory.value
            Log.d("NoteViewModel", "=== SAVE CHAT START ===")
            Log.d("NoteViewModel", "Chat history size: ${chatHistory.size}")
            
            if (chatHistory.isNotEmpty()) {
                // Convert chat history to transcript format
                val transcript = chatHistory.joinToString("\n\n") { (user, ai) ->
                    "User: $user${if (ai.isNotBlank()) "\n\nAssistant: $ai" else ""}"
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
                
                // Generate title using the same method as recording
                Log.d("NoteViewModel", "Calling generateTitle with summary: '$finalSummary'")
                val generatedTitle = generateTitle(finalSummary)
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
                Log.d("NoteViewModel", "=== SAVE CHAT SUCCESS ===")
                
            } else {
                Log.d("NoteViewModel", "Chat history is empty - nothing to save")
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "=== SAVE CHAT ERROR ===", e)
            Log.e("NoteViewModel", "Error details: ${e.message}")
            Log.e("NoteViewModel", "Error stack: ${e.stackTraceToString()}")
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
            // First, try to extract any text using OCR
            val extractedText = performOCR(bitmap, context)
            Log.d("NoteViewModel", "OCR extracted text: $extractedText")
            
            // Convert bitmap to base64 for OpenAI Vision API
            val base64Image = bitmapToBase64(bitmap)
            
            // Get visual description from OpenAI
            val visualDescription = analyzeImageWithOpenAI(base64Image, extractedText)
            Log.d("NoteViewModel", "Visual description: $visualDescription")
            
            // Check if the visual description indicates an error
            if (visualDescription.isNotBlank() && 
                !visualDescription.contains("API key not configured") &&
                !visualDescription.contains("error", ignoreCase = true) &&
                !visualDescription.contains("failed", ignoreCase = true) &&
                !visualDescription.contains("timeout", ignoreCase = true)) {
                
                processExtractedContent(visualDescription, "Image Analysis")
                Log.d("NoteViewModel", "=== PROCESS IMAGE VISUAL ANALYSIS SUCCESS ===")
            } else {
                // Fallback to OCR-only processing if visual analysis fails
                Log.w("NoteViewModel", "Visual analysis failed, falling back to OCR-only: $visualDescription")
                
                if (extractedText.isNotBlank()) {
                    val fallbackContent = """
                        Text extracted from image: $extractedText
                        
                        Note: Visual analysis was not available. ${visualDescription}
                        The above text was extracted using OCR (Optical Character Recognition).
                    """.trimIndent()
                    
                    processExtractedContent(fallbackContent, "Image Text (OCR)")
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
            // First, try to extract any text using OCR
            val extractedText = performOCR(imageUri, context)
            Log.d("NoteViewModel", "OCR extracted text: $extractedText")
            
            // Convert URI to bitmap then to base64 for OpenAI Vision API
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                throw Exception("Could not decode image from URI")
            }
            
            val base64Image = bitmapToBase64(bitmap)
            
            // Get visual description from OpenAI
            val visualDescription = analyzeImageWithOpenAI(base64Image, extractedText)
            Log.d("NoteViewModel", "Visual description: $visualDescription")
            
            // Check if the visual description indicates an error
            if (visualDescription.isNotBlank() && 
                !visualDescription.contains("API key not configured") &&
                !visualDescription.contains("error", ignoreCase = true) &&
                !visualDescription.contains("failed", ignoreCase = true) &&
                !visualDescription.contains("timeout", ignoreCase = true)) {
                
                processExtractedContent(visualDescription, "Image Analysis")
                Log.d("NoteViewModel", "=== PROCESS IMAGE URI VISUAL ANALYSIS SUCCESS ===")
            } else {
                // Fallback to OCR-only processing if visual analysis fails
                Log.w("NoteViewModel", "Visual analysis failed, falling back to OCR-only: $visualDescription")
                
                if (extractedText.isNotBlank()) {
                    val fallbackContent = """
                        Text extracted from image: $extractedText
                        
                        Note: Visual analysis was not available. ${visualDescription}
                        The above text was extracted using OCR (Optical Character Recognition).
                    """.trimIndent()
                    
                    processExtractedContent(fallbackContent, "Image Text (OCR)")
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
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { e: Exception ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
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
                            continuation.resume(visionText.text)
                        }
                        .addOnFailureListener { e: Exception ->
                            continuation.resumeWithException(e)
                        }
                } catch (e: Exception) {
                    // Fallback: try to load as bitmap first
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    if (inputStream != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        
                        val image = InputImage.fromBitmap(bitmap, 0)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText: com.google.mlkit.vision.text.Text ->
                                continuation.resume(visionText.text)
                            }
                            .addOnFailureListener { e: Exception ->
                                continuation.resumeWithException(e)
                            }
                    } else {
                        continuation.resumeWithException(Exception("Could not open image stream"))
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    // Helper function to convert bitmap to base64 for OpenAI Vision API
    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        // Resize bitmap if it's too large to avoid API limits and reduce processing time
        val maxSize = 1024 // Maximum width or height
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
        // Use higher quality for better analysis, but not max to keep file size reasonable
        resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
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
                    append("Please analyze this image and provide a detailed, human-like description of what you see. ")
                    append("Describe the scene, objects, people, activities, emotions, and any notable details in natural language. ")
                    append("Be descriptive and engaging, as if you're telling someone who can't see the image what's happening. ")
                    
                    if (extractedText.isNotBlank()) {
                        append("\n\nThe image also contains this text: \"$extractedText\". ")
                        append("Please incorporate this text into your description and explain its context within the image.")
                    }
                    
                    append("\n\nAfter your description, also extract any actionable tasks or important information that someone might want to remember or act upon from this image.")
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
                    put("max_tokens", 1500)  // Increased for more detailed descriptions
                    put("temperature", 0.7)
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
}
