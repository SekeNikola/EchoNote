package com.example.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0013\u001a\u00020\u0014J\u0006\u0010\u0015\u001a\u00020\u0014J\u000e\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u001a\u001a\u00020\u0014J\u0006\u0010\u001b\u001a\u00020\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010\u00a8\u0006\u001c"}, d2 = {"Lcom/example/app/util/VoiceAssistantManager;", "", "()V", "KEY_SHOW_ORB", "", "KEY_TRIGGER_VOICE", "PREFS_NAME", "_shouldShowOrb", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_shouldTriggerVoice", "sharedPreferences", "Landroid/content/SharedPreferences;", "shouldShowOrb", "Lkotlinx/coroutines/flow/StateFlow;", "getShouldShowOrb", "()Lkotlinx/coroutines/flow/StateFlow;", "shouldTriggerVoice", "getShouldTriggerVoice", "clearOrbTrigger", "", "clearTrigger", "initialize", "context", "Landroid/content/Context;", "setPendingTrigger", "showVoiceOrb", "triggerVoiceAssistant", "app_debug"})
public final class VoiceAssistantManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "voice_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TRIGGER_VOICE = "trigger_voice";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SHOW_ORB = "show_voice_orb";
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldTriggerVoice = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldTriggerVoice = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldShowOrb = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldShowOrb = null;
    @org.jetbrains.annotations.Nullable()
    private static android.content.SharedPreferences sharedPreferences;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.util.VoiceAssistantManager INSTANCE = null;
    
    private VoiceAssistantManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShouldTriggerVoice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShouldShowOrb() {
        return null;
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void triggerVoiceAssistant() {
    }
    
    public final void showVoiceOrb() {
    }
    
    public final void clearTrigger() {
    }
    
    public final void clearOrbTrigger() {
    }
    
    public final void setPendingTrigger(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}