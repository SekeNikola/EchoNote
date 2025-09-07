package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.navigation.NavController;
import com.example.app.viewmodel.NoteViewModel;
import java.text.SimpleDateFormat;
import java.util.*;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\"\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0007\u00a8\u0006\u000b"}, d2 = {"TaskDetailScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Lcom/example/app/viewmodel/NoteViewModel;", "taskId", "", "formatFullDate", "", "timestamp", "app_debug"})
public final class TaskDetailScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void TaskDetailScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.NoteViewModel viewModel, long taskId) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String formatFullDate(long timestamp) {
        return null;
    }
}