package com.example.app.ui;

import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextDecoration;
import com.example.app.data.Task;
import com.example.app.viewmodel.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010$\n\u0002\u0010 \n\u0002\b\u0002\u001a4\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a\u0016\u0010\t\u001a\u00020\u00012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a6\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\u00062\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001a\"\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0003\u001a(\u0010\u0018\u001a\u0014\u0012\u0004\u0012\u00020\u0013\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u001a0\u00192\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00060\u001aH\u0002\u00a8\u0006\u001c"}, d2 = {"AllTasksScreen", "", "onNavigateBack", "Lkotlin/Function0;", "onTaskClick", "Lkotlin/Function1;", "Lcom/example/app/data/Task;", "taskViewModel", "Lcom/example/app/viewmodel/TaskViewModel;", "EmptyTasksState", "onCreateTask", "TaskCard", "task", "onClick", "onToggleComplete", "modifier", "Landroidx/compose/ui/Modifier;", "TaskSectionHeader", "title", "", "taskCount", "", "isOverdue", "", "groupTasksByDate", "", "", "tasks", "app_debug"})
public final class AllTasksScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class, androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AllTasksScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.app.data.Task, kotlin.Unit> onTaskClick, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.TaskViewModel taskViewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TaskSectionHeader(java.lang.String title, int taskCount, boolean isOverdue) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TaskCard(com.example.app.data.Task task, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleComplete, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EmptyTasksState(kotlin.jvm.functions.Function0<kotlin.Unit> onCreateTask) {
    }
    
    private static final java.util.Map<java.lang.String, java.util.List<com.example.app.data.Task>> groupTasksByDate(java.util.List<com.example.app.data.Task> tasks) {
        return null;
    }
}