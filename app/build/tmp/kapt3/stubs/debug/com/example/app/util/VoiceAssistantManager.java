package com.example.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0015\u001a\u00020\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0016"}, d2 = {"Lcom/example/app/util/VoiceAssistantManager;", "", "()V", "KEY_TRIGGER_VOICE", "", "PREFS_NAME", "_shouldTriggerVoice", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "sharedPreferences", "Landroid/content/SharedPreferences;", "shouldTriggerVoice", "Lkotlinx/coroutines/flow/StateFlow;", "getShouldTriggerVoice", "()Lkotlinx/coroutines/flow/StateFlow;", "clearTrigger", "", "initialize", "context", "Landroid/content/Context;", "setPendingTrigger", "triggerVoiceAssistant", "app_debug"})
public final class VoiceAssistantManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "voice_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TRIGGER_VOICE = "trigger_voice";
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldTriggerVoice = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldTriggerVoice = null;
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
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void triggerVoiceAssistant() {
    }
    
    public final void clearTrigger() {
    }
    
    public final void setPendingTrigger(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}