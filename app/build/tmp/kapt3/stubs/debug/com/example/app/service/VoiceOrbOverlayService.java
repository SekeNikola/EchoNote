package com.example.app.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import androidx.compose.animation.core.RepeatMode;
import androidx.compose.material.icons.Icons;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.platform.ComposeView;
import kotlinx.coroutines.Dispatchers;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\b\u0010\u000f\u001a\u00020\fH\u0002J\u0014\u0010\u0010\u001a\u0004\u0018\u00010\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0016J\b\u0010\u0014\u001a\u00020\fH\u0016J\"\u0010\u0015\u001a\u00020\u00162\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0016H\u0016J\b\u0010\u0019\u001a\u00020\fH\u0002J\b\u0010\u001a\u001a\u00020\fH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/example/app/service/VoiceOrbOverlayService;", "Landroid/app/Service;", "()V", "hideJob", "Lkotlinx/coroutines/Job;", "overlayView", "Landroidx/compose/ui/platform/ComposeView;", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "windowManager", "Landroid/view/WindowManager;", "handleSpeechResult", "", "spokenText", "", "hideOrbOverlay", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onDestroy", "onStartCommand", "", "flags", "startId", "showOrbOverlay", "startSpeechRecognition", "Companion", "app_debug"})
public final class VoiceOrbOverlayService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "VoiceOrbOverlayService";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SHOW_ORB = "com.example.app.SHOW_VOICE_ORB";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_HIDE_ORB = "com.example.app.HIDE_VOICE_ORB";
    @org.jetbrains.annotations.Nullable()
    private android.view.WindowManager windowManager;
    @org.jetbrains.annotations.Nullable()
    private androidx.compose.ui.platform.ComposeView overlayView;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job hideJob;
    @org.jetbrains.annotations.Nullable()
    private android.speech.SpeechRecognizer speechRecognizer;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.service.VoiceOrbOverlayService.Companion Companion = null;
    
    public VoiceOrbOverlayService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void showOrbOverlay() {
    }
    
    private final void hideOrbOverlay() {
    }
    
    private final void startSpeechRecognition() {
    }
    
    private final void handleSpeechResult(java.lang.String spokenText) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\r\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/example/app/service/VoiceOrbOverlayService$Companion;", "", "()V", "ACTION_HIDE_ORB", "", "ACTION_SHOW_ORB", "TAG", "canDrawOverlays", "", "context", "Landroid/content/Context;", "hideOrb", "", "showOrb", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void showOrb(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        public final void hideOrb(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        public final boolean canDrawOverlays(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
    }
}