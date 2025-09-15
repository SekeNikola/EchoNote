package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import com.example.app.data.Task;
import com.example.app.viewmodel.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0002\u001a*\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001aJ\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2$\u0010\u000b\u001a \u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\n\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010\f2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00a8\u0006\u000f"}, d2 = {"EnhancedNoteCreationScreen", "", "initialNoteText", "", "onNavigateBack", "Lkotlin/Function0;", "taskViewModel", "Lcom/example/app/viewmodel/TaskViewModel;", "TaskCreationDialog", "tasks", "", "onConfirm", "Lkotlin/Function3;", "", "onDismiss", "app_debug"})
public final class EnhancedNoteCreationScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void EnhancedNoteCreationScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String initialNoteText, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.TaskViewModel taskViewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TaskCreationDialog(java.util.List<java.lang.String> tasks, kotlin.jvm.functions.Function3<? super java.util.List<java.lang.String>, ? super java.lang.String, ? super java.lang.Long, kotlin.Unit> onConfirm, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}