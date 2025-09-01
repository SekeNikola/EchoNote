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
import kotlinx.coroutines.Dispatchers;
import okhttp3.MultipartBody;
import android.speech.SpeechRecognizer;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.os.Bundle;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0098\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0017\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u00012\u00020\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020\n2\u0006\u00102\u001a\u00020\u001cJ\u000e\u00103\u001a\u0002042\u0006\u00105\u001a\u000206J\b\u00107\u001a\u000200H\u0002J\b\u00108\u001a\u00020%H\u0002J\u000e\u00109\u001a\u0002042\u0006\u00105\u001a\u000206J\u0010\u0010:\u001a\u00020\n2\u0006\u0010;\u001a\u00020\nH\u0002J\u0016\u0010<\u001a\u00020\n2\u0006\u0010*\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010=J\u0016\u0010>\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010!0\u001f2\u0006\u00105\u001a\u000206J\n\u0010?\u001a\u0004\u0018\u00010\nH\u0002J\u0006\u0010@\u001a\u000200J\b\u0010A\u001a\u000200H\u0014J\u0010\u0010B\u001a\u0002002\u0006\u0010C\u001a\u00020\u0011H\u0016J\u000e\u0010D\u001a\u0002002\u0006\u00105\u001a\u000206J\u0016\u0010E\u001a\u0002002\u0006\u0010F\u001a\u00020\u0014H\u0082@\u00a2\u0006\u0002\u0010GJ\u000e\u0010H\u001a\u0002002\u0006\u0010I\u001a\u000206J\u000e\u0010J\u001a\u0002042\u0006\u0010K\u001a\u00020\nJ\u000e\u0010L\u001a\u0002002\u0006\u0010M\u001a\u00020NJ\u0006\u0010O\u001a\u000200J\u0006\u0010P\u001a\u000200J\u000e\u0010Q\u001a\u0002042\u0006\u0010R\u001a\u00020!J\u0016\u0010S\u001a\u0002042\u0006\u0010I\u001a\u0002062\u0006\u0010T\u001a\u00020\nJ\u0016\u0010U\u001a\u0002042\u0006\u0010I\u001a\u0002062\u0006\u0010V\u001a\u00020\nJ\u0016\u0010W\u001a\u0002042\u0006\u00105\u001a\u0002062\u0006\u0010X\u001a\u00020\nR\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u000b\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\n0\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001f\u0010\u0010\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\u00110\u00110\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\n0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u001f\u0010\u001b\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\u001c0\u001c0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u000fR\u001f\u0010\u001d\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\u001c0\u001c0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u000fR\u001d\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0 0\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0010\u0010$\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010&\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\n0\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u000fR\u0010\u0010(\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001f\u0010*\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\n0\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u000fR\u0010\u0010,\u001a\u0004\u0018\u00010-X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010.\u001a\u00020\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006Y"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "repository", "Lcom/example/app/data/NoteRepository;", "app", "Landroid/app/Application;", "(Lcom/example/app/data/NoteRepository;Landroid/app/Application;)V", "_fullTranscript", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "aiResponse", "Landroidx/lifecycle/MutableLiveData;", "kotlin.jvm.PlatformType", "getAiResponse", "()Landroidx/lifecycle/MutableLiveData;", "amplitude", "", "getAmplitude", "compressedAudioFile", "Ljava/io/File;", "compressedAudioRecorder", "Lcom/example/app/audio/CompressedAudioRecorder;", "fullTranscript", "Lkotlinx/coroutines/flow/StateFlow;", "getFullTranscript", "()Lkotlinx/coroutines/flow/StateFlow;", "isRecording", "", "isVoiceOverlayVisible", "notes", "Landroidx/lifecycle/LiveData;", "", "Lcom/example/app/data/Note;", "getNotes", "()Landroidx/lifecycle/LiveData;", "recognizerIntent", "Landroid/content/Intent;", "searchQuery", "getSearchQuery", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "summary", "getSummary", "tts", "Landroid/speech/tts/TextToSpeech;", "ttsReady", "appendTranscript", "", "newText", "isFinal", "archiveNote", "Lkotlinx/coroutines/Job;", "id", "", "compressAndSendAudioToOpenAI", "createRecognizerIntent", "deleteNote", "generateSummary", "text", "generateTitle", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getNoteById", "getRawAudioFilePath", "hideVoiceOverlay", "onCleared", "onInit", "status", "readAloud", "sendCompressedAudioToOpenAI", "file", "(Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setReminder", "noteId", "showVoiceOverlay", "query", "startSpeechRecognition", "context", "Landroid/content/Context;", "stopAndSaveNote", "stopSpeechRecognition", "toggleFavorite", "note", "updateChecklistState", "checklistState", "updateNoteSnippet", "snippet", "updateNoteTitle", "title", "app_debug"})
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
}