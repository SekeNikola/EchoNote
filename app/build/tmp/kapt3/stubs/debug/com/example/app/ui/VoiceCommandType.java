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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/example/app/ui/VoiceCommandType;", "", "(Ljava/lang/String;I)V", "CREATE_TASK", "CREATE_NOTE", "SET_REMINDER", "SEARCH", "READ_TASKS", "UNKNOWN", "app_debug"})
public enum VoiceCommandType {
    /*public static final*/ CREATE_TASK /* = new CREATE_TASK() */,
    /*public static final*/ CREATE_NOTE /* = new CREATE_NOTE() */,
    /*public static final*/ SET_REMINDER /* = new SET_REMINDER() */,
    /*public static final*/ SEARCH /* = new SEARCH() */,
    /*public static final*/ READ_TASKS /* = new READ_TASKS() */,
    /*public static final*/ UNKNOWN /* = new UNKNOWN() */;
    
    VoiceCommandType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.example.app.ui.VoiceCommandType> getEntries() {
        return null;
    }
}