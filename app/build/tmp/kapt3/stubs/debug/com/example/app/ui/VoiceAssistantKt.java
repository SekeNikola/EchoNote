package com.example.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import kotlinx.coroutines.Dispatchers;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u001e\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u001a\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0004H\u0002\u001a\u0010\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\rH\u0002\u001a8\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u001e\u0010\u0016\u001a\u001a\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010\u0017H\u0002\u00a8\u0006\u0018"}, d2 = {"VoiceAssistantButton", "", "onVoiceCommand", "Lkotlin/Function1;", "Lcom/example/app/ui/VoiceCommand;", "modifier", "Landroidx/compose/ui/Modifier;", "VoiceButton", "state", "Lcom/example/app/ui/VoiceAssistantState;", "onClick", "Lkotlin/Function0;", "generateResponse", "", "command", "parseVoiceCommand", "text", "startListening", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "context", "Landroid/content/Context;", "onStateChange", "Lkotlin/Function3;", "app_debug"})
public final class VoiceAssistantKt {
    
    @androidx.compose.runtime.Composable()
    public static final void VoiceAssistantButton(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.app.ui.VoiceCommand, kotlin.Unit> onVoiceCommand, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void VoiceButton(com.example.app.ui.VoiceAssistantState state, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final void startListening(android.speech.SpeechRecognizer speechRecognizer, android.content.Context context, kotlin.jvm.functions.Function3<? super com.example.app.ui.VoiceAssistantState, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onStateChange) {
    }
    
    private static final com.example.app.ui.VoiceCommand parseVoiceCommand(java.lang.String text) {
        return null;
    }
    
    private static final java.lang.String generateResponse(com.example.app.ui.VoiceCommand command) {
        return null;
    }
}