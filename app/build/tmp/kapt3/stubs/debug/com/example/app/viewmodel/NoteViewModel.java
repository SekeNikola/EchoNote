package com.example.app.viewmodel;

import androidx.lifecycle.*;
import kotlinx.coroutines.flow.StateFlow;
import com.example.app.data.Note;
import com.example.app.data.NoteRepository;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import com.example.app.audio.AudioRecorder;
import com.example.app.audio.CompressedAudioRecorder;
import com.example.app.network.GPTRequest;
import com.example.app.network.Message;
import com.example.app.network.RetrofitInstance;
import com.example.app.worker.ReminderScheduler;
import android.speech.tts.TextToSpeech;
import java.io.File;
import java.util.Locale;
import com.example.app.util.NetworkUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.common.InputImage;
import kotlinx.coroutines.Dispatchers;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.jsoup.Jsoup;
import java.net.URL;
import java.io.IOException;
import okhttp3.MultipartBody;
import android.speech.SpeechRecognizer;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Base64;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ae\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b8\u0018\u00002\u00020\u00012\u00020\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u001e\u00103\u001a\u00020\f2\u0006\u00104\u001a\u00020\f2\u0006\u00105\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u00106J\u0016\u00107\u001a\u0002082\u0006\u00109\u001a\u00020\f2\u0006\u0010:\u001a\u00020!J\u000e\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020>J\u0010\u0010?\u001a\u00020\f2\u0006\u0010@\u001a\u00020AH\u0002J\u0010\u0010B\u001a\u00020\f2\u0006\u0010C\u001a\u00020\fH\u0002J\u0006\u0010D\u001a\u000208J\b\u0010E\u001a\u000208H\u0002J\b\u0010F\u001a\u00020)H\u0002J\u000e\u0010G\u001a\u00020<2\u0006\u0010=\u001a\u00020>J\u001e\u0010H\u001a\u00020\f2\u0006\u0010I\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0082@\u00a2\u0006\u0002\u0010MJ*\u0010N\u001a\u0016\u0012\u0004\u0012\u00020\f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\n\u0018\u00010\u000b2\u0006\u0010O\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010PJ\u0016\u0010Q\u001a\u00020\f2\u0006\u0010R\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010PJ\u0016\u0010S\u001a\u00020\f2\u0006\u0010T\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010PJ\u0010\u0010U\u001a\u00020\f2\u0006\u0010C\u001a\u00020\fH\u0002J\u0016\u0010V\u001a\u00020\f2\u0006\u0010.\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010PJ\u000e\u0010W\u001a\u00020<2\u0006\u0010O\u001a\u00020\fJ\u0016\u0010X\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010%0$2\u0006\u0010=\u001a\u00020>J\n\u0010Y\u001a\u0004\u0018\u00010\fH\u0002J\u0006\u0010Z\u001a\u000208J\b\u0010[\u001a\u000208H\u0014J\u0010\u0010\\\u001a\u0002082\u0006\u0010]\u001a\u00020\u0014H\u0016J\u001e\u0010^\u001a\u00020\f2\u0006\u0010@\u001a\u00020A2\u0006\u0010K\u001a\u00020LH\u0082@\u00a2\u0006\u0002\u0010_J\u001e\u0010^\u001a\u00020\f2\u0006\u0010`\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0082@\u00a2\u0006\u0002\u0010MJ\u001e\u0010a\u001a\u00020\u00142\u0006\u0010I\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0086@\u00a2\u0006\u0002\u0010MJ\u001e\u0010b\u001a\u0002082\u0006\u0010c\u001a\u00020\f2\u0006\u0010d\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u00106J\u001e\u0010e\u001a\u00020\u00142\u0006\u0010@\u001a\u00020A2\u0006\u0010K\u001a\u00020LH\u0086@\u00a2\u0006\u0002\u0010_J\u001e\u0010e\u001a\u00020\u00142\u0006\u0010`\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0086@\u00a2\u0006\u0002\u0010MJ\u0016\u0010f\u001a\u00020\u00142\u0006\u0010g\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010PJ\u001e\u0010h\u001a\u00020\u00142\u0006\u0010i\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0086@\u00a2\u0006\u0002\u0010MJ\u0016\u0010j\u001a\u00020\u00142\u0006\u0010R\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010PJ\u0016\u0010k\u001a\u00020\u00142\u0006\u0010T\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010PJ\u000e\u0010l\u001a\u0002082\u0006\u0010=\u001a\u00020>J\u0006\u0010m\u001a\u00020<J\u0006\u0010n\u001a\u00020<J\u0016\u0010o\u001a\u0002082\u0006\u0010p\u001a\u00020\u001bH\u0082@\u00a2\u0006\u0002\u0010qJ\u000e\u0010r\u001a\u0002082\u0006\u0010s\u001a\u00020>J\u000e\u0010t\u001a\u00020<2\u0006\u0010u\u001a\u00020\fJ\u000e\u0010v\u001a\u0002082\u0006\u0010K\u001a\u00020LJ\u0006\u0010w\u001a\u000208J\u0006\u0010x\u001a\u000208J\u000e\u0010y\u001a\u00020<2\u0006\u0010z\u001a\u00020%J\u001e\u0010{\u001a\u00020\f2\u0006\u0010i\u001a\u00020J2\u0006\u0010K\u001a\u00020LH\u0082@\u00a2\u0006\u0002\u0010MJ\u0016\u0010|\u001a\u00020<2\u0006\u0010s\u001a\u00020>2\u0006\u0010}\u001a\u00020\fJ\u0016\u0010~\u001a\u00020<2\u0006\u0010s\u001a\u00020>2\u0006\u0010\u007f\u001a\u00020\fJ\u0018\u0010\u0080\u0001\u001a\u00020<2\u0006\u0010=\u001a\u00020>2\u0007\u0010\u0081\u0001\u001a\u00020\fJ\u0017\u0010\u0082\u0001\u001a\u00020<2\u0006\u0010s\u001a\u00020>2\u0006\u0010O\u001a\u00020\fJ\u0017\u0010\u0083\u0001\u001a\u00020<2\u0006\u0010s\u001a\u00020>2\u0006\u0010O\u001a\u00020\fR&\u0010\b\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u000b0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u000e\u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\f0\f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u001f\u0010\u0013\u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\u00140\u00140\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012R)\u0010\u0016\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u000b0\n0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\f0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0019R\u001f\u0010 \u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010!0!0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0012R\u001f\u0010\"\u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010!0!0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0012R\u001d\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\n0$\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0010\u0010(\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010*\u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\f0\f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u0012R\u0010\u0010,\u001a\u0004\u0018\u00010-X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001f\u0010.\u001a\u0010\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\f0\f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\u0012R\u0010\u00100\u001a\u0004\u0018\u000101X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00102\u001a\u00020!X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0084\u0001"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "repository", "Lcom/example/app/data/NoteRepository;", "app", "Landroid/app/Application;", "(Lcom/example/app/data/NoteRepository;Landroid/app/Application;)V", "_assistantChatHistory", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lkotlin/Pair;", "", "_fullTranscript", "aiResponse", "Landroidx/lifecycle/MutableLiveData;", "kotlin.jvm.PlatformType", "getAiResponse", "()Landroidx/lifecycle/MutableLiveData;", "amplitude", "", "getAmplitude", "assistantChatHistory", "Lkotlinx/coroutines/flow/StateFlow;", "getAssistantChatHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "compressedAudioFile", "Ljava/io/File;", "compressedAudioRecorder", "Lcom/example/app/audio/CompressedAudioRecorder;", "fullTranscript", "getFullTranscript", "isRecording", "", "isVoiceOverlayVisible", "notes", "Landroidx/lifecycle/LiveData;", "Lcom/example/app/data/Note;", "getNotes", "()Landroidx/lifecycle/LiveData;", "recognizerIntent", "Landroid/content/Intent;", "searchQuery", "getSearchQuery", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "summary", "getSummary", "tts", "Landroid/speech/tts/TextToSpeech;", "ttsReady", "analyzeImageWithOpenAI", "base64Image", "extractedText", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "appendTranscript", "", "newText", "isFinal", "archiveNote", "Lkotlinx/coroutines/Job;", "id", "", "bitmapToBase64", "bitmap", "Landroid/graphics/Bitmap;", "cleanBracketsFromText", "text", "clearAssistantChat", "compressAndSendAudioToOpenAI", "createRecognizerIntent", "deleteNote", "extractDocumentText", "documentUri", "Landroid/net/Uri;", "context", "Landroid/content/Context;", "(Landroid/net/Uri;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractSummaryAndTasksWithOpenAI", "transcript", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractVideoSummary", "videoUrl", "fetchWebPageContent", "webUrl", "generateSummary", "generateTitle", "getAssistantResponse", "getNoteById", "getRawAudioFilePath", "hideVoiceOverlay", "onCleared", "onInit", "status", "performOCR", "(Landroid/graphics/Bitmap;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "imageUri", "processDocument", "processExtractedContent", "content", "contentType", "processImageWithOCR", "processTextNote", "textContent", "processUploadedAudio", "audioUri", "processVideoUrl", "processWebPageUrl", "readAloud", "saveChatAsNote", "saveTestChatNote", "sendCompressedAudioToOpenAI", "file", "(Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setReminder", "noteId", "showVoiceOverlay", "query", "startSpeechRecognition", "stopAndSaveNote", "stopSpeechRecognition", "toggleFavorite", "note", "transcribeUploadedAudio", "updateChecklistState", "checklistState", "updateNoteSnippet", "snippet", "updateNoteTitle", "title", "updateSummaryWithOpenAI", "updateTranscript", "app_debug"})
public final class NoteViewModel extends androidx.lifecycle.AndroidViewModel implements android.speech.tts.TextToSpeech.OnInitListener {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.audio.CompressedAudioRecorder compressedAudioRecorder = null;
    @org.jetbrains.annotations.Nullable()
    private java.io.File compressedAudioFile;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Note>> notes = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isRecording = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> summary = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isVoiceOverlayVisible = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> aiResponse = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Integer> amplitude = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _fullTranscript = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> fullTranscript = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> _assistantChatHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> assistantChatHistory = null;
    @org.jetbrains.annotations.Nullable()
    private android.speech.tts.TextToSpeech tts;
    private boolean ttsReady = false;
    @org.jetbrains.annotations.Nullable()
    private android.speech.SpeechRecognizer speechRecognizer;
    @org.jetbrains.annotations.Nullable()
    private android.content.Intent recognizerIntent;
    
    public NoteViewModel(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteRepository repository, @org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        super(null);
    }
    
    /**
     * Get an AI assistant response from OpenAI for the given transcript.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getAssistantResponse(@org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateSummaryWithOpenAI(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTranscript(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    /**
     * Use OpenAI GPT to extract summary and tasks from transcript.
     * The prompt asks for a JSON response: {"summary": "...", "tasks": [ ... ]}
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object extractSummaryAndTasksWithOpenAI(@org.jetbrains.annotations.NotNull()
    java.lang.String transcript, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Pair<java.lang.String, ? extends java.util.List<java.lang.String>>> $completion) {
        return null;
    }
    
    private final java.lang.String cleanBracketsFromText(java.lang.String text) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateNoteSnippet(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String snippet) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateChecklistState(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String checklistState) {
        return null;
    }
    
    public final void stopAndSaveNote() {
    }
    
    private final void compressAndSendAudioToOpenAI() {
    }
    
    private final java.lang.Object sendCompressedAudioToOpenAI(java.io.File file, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String getRawAudioFilePath() {
        return null;
    }
    
    public final void setReminder(long noteId) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteNote(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Note>> getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getSearchQuery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isRecording() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getSummary() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isVoiceOverlayVisible() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getAiResponse() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Integer> getAmplitude() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getFullTranscript() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> getAssistantChatHistory() {
        return null;
    }
    
    public final void appendTranscript(@org.jetbrains.annotations.NotNull()
    java.lang.String newText, boolean isFinal) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String summary, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job toggleFavorite(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Note note) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.example.app.data.Note> getNoteById(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateNoteTitle(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
        return null;
    }
    
    public final void readAloud(long id) {
    }
    
    @java.lang.Override()
    public void onInit(int status) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job archiveNote(long id) {
        return null;
    }
    
    public final void startSpeechRecognition(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final android.content.Intent createRecognizerIntent() {
        return null;
    }
    
    private final java.lang.String generateSummary(java.lang.String text) {
        return null;
    }
    
    public final void stopSpeechRecognition() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job showVoiceOverlay(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    public final void hideVoiceOverlay() {
    }
    
    public final void clearAssistantChat() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveChatAsNote() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processUploadedAudio(@org.jetbrains.annotations.NotNull()
    android.net.Uri audioUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object transcribeUploadedAudio(android.net.Uri audioUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processImageWithOCR(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processImageWithOCR(@org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCR(android.graphics.Bitmap bitmap, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCR(android.net.Uri imageUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String bitmapToBase64(android.graphics.Bitmap bitmap) {
        return null;
    }
    
    private final java.lang.Object analyzeImageWithOpenAI(java.lang.String base64Image, java.lang.String extractedText, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processTextNote(@org.jetbrains.annotations.NotNull()
    java.lang.String textContent, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processVideoUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String videoUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object extractVideoSummary(java.lang.String videoUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processWebPageUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String webUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchWebPageContent(java.lang.String webUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri documentUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object extractDocumentText(android.net.Uri documentUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object processExtractedContent(java.lang.String content, java.lang.String contentType, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveTestChatNote() {
        return null;
    }
}