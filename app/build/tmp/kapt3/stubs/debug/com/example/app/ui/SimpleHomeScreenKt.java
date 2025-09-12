package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.material3.OutlinedTextFieldDefaults;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.unit.Dp;
import androidx.compose.ui.window.PopupProperties;
import androidx.compose.ui.focus.FocusRequester;
import androidx.navigation.NavController;
import android.net.Uri;
import com.example.app.data.Note;
import com.example.app.data.Task;
import com.example.app.viewmodel.NoteViewModel;
import java.text.SimpleDateFormat;
import java.util.*;
import android.app.TimePickerDialog;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import java.io.File;
import org.json.JSONObject;
import androidx.compose.animation.core.*;
import com.airbnb.lottie.compose.LottieCompositionSpec;
import com.airbnb.lottie.compose.LottieConstants;
import com.example.app.R;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000`\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0002\u001a:\u0010\u0000\u001a\u00020\u00012\u0018\u0010\u0002\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a<\u0010\t\u001a\u00020\u00012$\u0010\n\u001a \u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a \u0010\r\u001a\u00020\u00012\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0007\u001a \u0010\u0011\u001a\u00020\u00012\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0007\u001a\u001e\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a(\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u000e\b\u0002\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a6\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\u001d2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u0016\b\u0002\u0010\u001e\u001a\u0010\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u001fH\u0007\u001a\u000e\u0010 \u001a\u00020\u00042\u0006\u0010!\u001a\u00020\u0004\u001a\u000e\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\f\u001a\u000e\u0010$\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\f\u001a\u000e\u0010%\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\f\u001a\u0006\u0010&\u001a\u00020\u0004\u001a\u000e\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\f\u00a8\u0006*"}, d2 = {"AddNoteBottomSheet", "", "onCreateNote", "Lkotlin/Function2;", "", "onDismiss", "Lkotlin/Function0;", "key", "", "AddTaskBottomSheet", "onCreateTask", "Lkotlin/Function4;", "", "CompactLottieVoiceOrb", "onClick", "modifier", "Landroidx/compose/ui/Modifier;", "CompactVoiceOrb", "NoteCard", "note", "Lcom/example/app/data/Note;", "SimpleHomeScreen", "navController", "Landroidx/navigation/NavController;", "viewModel", "Lcom/example/app/viewmodel/NoteViewModel;", "onAddNote", "TaskCard", "task", "Lcom/example/app/data/Task;", "onCompleteToggle", "Lkotlin/Function1;", "extractSummaryFromSnippet", "snippet", "formatDate", "timestamp", "formatDateShort", "formatTime", "getGreeting", "isToday", "", "date", "app_debug"})
public final class SimpleHomeScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SimpleHomeScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.NoteViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAddNote) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TaskCard(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onCompleteToggle) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void NoteCard(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Note note, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getGreeting() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String extractSummaryFromSnippet(@org.jetbrains.annotations.NotNull()
    java.lang.String snippet) {
        return null;
    }
    
    public static final boolean isToday(long date) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String formatTime(long timestamp) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String formatDate(long timestamp) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String formatDateShort(long timestamp) {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AddTaskBottomSheet(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function4<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.Long, kotlin.Unit> onCreateTask, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AddNoteBottomSheet(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onCreateNote, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, int key) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CompactVoiceOrb(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CompactLottieVoiceOrb(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
}