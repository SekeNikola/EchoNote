package com.example.app.viewmodel;

import androidx.lifecycle.*;
import com.example.app.data.Note;
import com.example.app.data.NoteRepository;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import com.example.app.audio.AudioRecorder;
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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u00012\u00020\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'J\u0016\u0010(\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001c0\u001a2\u0006\u0010&\u001a\u00020\'J\u0006\u0010)\u001a\u00020*J\u000e\u0010+\u001a\u00020%2\u0006\u0010,\u001a\u00020\'J\b\u0010-\u001a\u00020*H\u0014J\u0010\u0010.\u001a\u00020*2\u0006\u0010/\u001a\u00020\u000fH\u0016J\u000e\u00100\u001a\u00020*2\u0006\u0010&\u001a\u00020\'J\u001f\u00101\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\n\b\u0002\u00102\u001a\u0004\u0018\u00010\'\u00a2\u0006\u0002\u00103J\u000e\u00104\u001a\u00020%2\u0006\u00105\u001a\u00020\nJ\u000e\u00106\u001a\u00020*2\u0006\u00107\u001a\u000208J\u0006\u00109\u001a\u00020%J\u0016\u0010:\u001a\u00020\n2\u0006\u0010;\u001a\u00020\nH\u0082@\u00a2\u0006\u0002\u0010<J\u0006\u0010=\u001a\u00020%J\u000e\u0010>\u001a\u00020%2\u0006\u0010?\u001a\u00020\u001cJ\u0016\u0010@\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\u0006\u0010A\u001a\u00020\nJ\u000e\u0010B\u001a\u00020*2\u0006\u00105\u001a\u00020\nJ\u0016\u0010C\u001a\u00020\n2\u0006\u0010D\u001a\u00020\nH\u0082@\u00a2\u0006\u0002\u0010<R\u001f\u0010\b\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\n0\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u001f\u0010\u000e\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\u000f0\u000f0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u0014\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\u00150\u00150\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\rR\u001f\u0010\u0016\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\u00150\u00150\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\rR\u001f\u0010\u0017\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\n0\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\rR\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u001b0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u001f\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\n0\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\rR\u0010\u0010!\u001a\u0004\u0018\u00010\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006E"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "repository", "Lcom/example/app/data/NoteRepository;", "app", "Landroid/app/Application;", "(Lcom/example/app/data/NoteRepository;Landroid/app/Application;)V", "aiResponse", "Landroidx/lifecycle/MutableLiveData;", "", "kotlin.jvm.PlatformType", "getAiResponse", "()Landroidx/lifecycle/MutableLiveData;", "amplitude", "", "getAmplitude", "audioRecorder", "Lcom/example/app/audio/AudioRecorder;", "currentAudioPath", "isRecording", "", "isVoiceOverlayVisible", "liveTranscript", "getLiveTranscript", "notes", "Landroidx/lifecycle/LiveData;", "", "Lcom/example/app/data/Note;", "getNotes", "()Landroidx/lifecycle/LiveData;", "searchQuery", "getSearchQuery", "tts", "Landroid/speech/tts/TextToSpeech;", "ttsReady", "archiveNote", "Lkotlinx/coroutines/Job;", "id", "", "getNoteById", "hideVoiceOverlay", "", "markNoteForSync", "noteId", "onCleared", "onInit", "status", "readAloud", "setReminder", "timeMillis", "(JLjava/lang/Long;)Lkotlinx/coroutines/Job;", "showVoiceOverlay", "query", "startRecording", "context", "Landroid/content/Context;", "stopRecordingAndTranscribe", "summarizeTranscript", "transcript", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncNotesIfOnline", "toggleFavorite", "note", "updateNoteTitle", "title", "updateSearchQuery", "uploadAndTranscribe", "path", "app_debug"})
public final class NoteViewModel extends androidx.lifecycle.AndroidViewModel implements android.speech.tts.TextToSpeech.OnInitListener {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Note>> notes = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isRecording = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> liveTranscript = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isVoiceOverlayVisible = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> aiResponse = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Integer> amplitude = null;
    @org.jetbrains.annotations.Nullable()
    private com.example.app.audio.AudioRecorder audioRecorder;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentAudioPath;
    @org.jetbrains.annotations.Nullable()
    private android.speech.tts.TextToSpeech tts;
    private boolean ttsReady = false;
    
    public NoteViewModel(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteRepository repository, @org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        super(null);
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
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getLiveTranscript() {
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
    
    public final void updateSearchQuery(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
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
    public final kotlinx.coroutines.Job syncNotesIfOnline() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job markNoteForSync(long noteId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job setReminder(long id, @org.jetbrains.annotations.Nullable()
    java.lang.Long timeMillis) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job archiveNote(long id) {
        return null;
    }
    
    public final void startRecording(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job stopRecordingAndTranscribe() {
        return null;
    }
    
    private final java.lang.Object uploadAndTranscribe(java.lang.String path, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object summarizeTranscript(java.lang.String transcript, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job showVoiceOverlay(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    public final void hideVoiceOverlay() {
    }
}