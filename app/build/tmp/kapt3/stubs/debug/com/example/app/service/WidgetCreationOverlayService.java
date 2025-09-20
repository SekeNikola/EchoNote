package com.example.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.platform.ComposeView;
import androidx.compose.ui.text.font.FontWeight;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;
import com.example.app.data.AppDatabase;
import com.example.app.data.Note;
import com.example.app.data.NoteRepository;
import com.example.app.data.Task;
import com.example.app.data.TaskRepository;
import com.example.app.viewmodel.NoteViewModel;
import com.example.app.viewmodel.TaskViewModel;
import com.example.app.widget.SimpleWidgetProvider;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u0000 42\u00020\u00012\u00020\u00022\u00020\u0003:\u00014B\u0005\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u001b\u001a\u00020\u001cH\u0002J\"\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001f2\b\u0010!\u001a\u0004\u0018\u00010\u001fH\u0002J(\u0010\"\u001a\u00020\u001c2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010#\u001a\u00020\u001f2\u0006\u0010$\u001a\u00020\u001f2\u0006\u0010%\u001a\u00020&H\u0002J\b\u0010\'\u001a\u00020\u001cH\u0002J\u0014\u0010(\u001a\u0004\u0018\u00010)2\b\u0010*\u001a\u0004\u0018\u00010+H\u0016J\b\u0010,\u001a\u00020\u001cH\u0016J\b\u0010-\u001a\u00020\u001cH\u0016J\"\u0010.\u001a\u00020/2\b\u0010*\u001a\u0004\u0018\u00010+2\u0006\u00100\u001a\u00020/2\u0006\u00101\u001a\u00020/H\u0016J\u0010\u00102\u001a\u00020\u001c2\u0006\u00103\u001a\u00020\u001fH\u0002R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\u00020\b8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\u00020\u00128VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00065"}, d2 = {"Lcom/example/app/service/WidgetCreationOverlayService;", "Landroid/app/Service;", "Landroidx/lifecycle/LifecycleOwner;", "Landroidx/savedstate/SavedStateRegistryOwner;", "()V", "hideJob", "Lkotlinx/coroutines/Job;", "lifecycle", "Landroidx/lifecycle/Lifecycle;", "getLifecycle", "()Landroidx/lifecycle/Lifecycle;", "lifecycleRegistry", "Landroidx/lifecycle/LifecycleRegistry;", "noteRepository", "Lcom/example/app/data/NoteRepository;", "overlayView", "Landroidx/compose/ui/platform/ComposeView;", "savedStateRegistry", "Landroidx/savedstate/SavedStateRegistry;", "getSavedStateRegistry", "()Landroidx/savedstate/SavedStateRegistry;", "savedStateRegistryController", "Landroidx/savedstate/SavedStateRegistryController;", "taskRepository", "Lcom/example/app/data/TaskRepository;", "windowManager", "Landroid/view/WindowManager;", "cleanupOverlay", "", "createNote", "title", "", "content", "imageUri", "createTask", "description", "priority", "dueDate", "", "hideCreatorOverlay", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "showCreatorOverlay", "mode", "Companion", "app_debug"})
public final class WidgetCreationOverlayService extends android.app.Service implements androidx.lifecycle.LifecycleOwner, androidx.savedstate.SavedStateRegistryOwner {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "WidgetCreationOverlayService";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SHOW_TASK_CREATOR = "com.example.app.SHOW_TASK_CREATOR";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SHOW_NOTE_CREATOR = "com.example.app.SHOW_NOTE_CREATOR";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_HIDE_OVERLAY = "com.example.app.HIDE_CREATION_OVERLAY";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_MODE = "mode";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MODE_TASK = "task";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MODE_NOTE = "note";
    @org.jetbrains.annotations.Nullable()
    private android.view.WindowManager windowManager;
    @org.jetbrains.annotations.Nullable()
    private androidx.compose.ui.platform.ComposeView overlayView;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job hideJob;
    @org.jetbrains.annotations.Nullable()
    private com.example.app.data.TaskRepository taskRepository;
    @org.jetbrains.annotations.Nullable()
    private com.example.app.data.NoteRepository noteRepository;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LifecycleRegistry lifecycleRegistry = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.savedstate.SavedStateRegistryController savedStateRegistryController = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.service.WidgetCreationOverlayService.Companion Companion = null;
    
    public WidgetCreationOverlayService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.lifecycle.Lifecycle getLifecycle() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.savedstate.SavedStateRegistry getSavedStateRegistry() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void showCreatorOverlay(java.lang.String mode) {
    }
    
    private final void hideCreatorOverlay() {
    }
    
    private final void cleanupOverlay() {
    }
    
    private final void createTask(java.lang.String title, java.lang.String description, java.lang.String priority, long dueDate) {
    }
    
    private final void createNote(java.lang.String title, java.lang.String content, java.lang.String imageUri) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/example/app/service/WidgetCreationOverlayService$Companion;", "", "()V", "ACTION_HIDE_OVERLAY", "", "ACTION_SHOW_NOTE_CREATOR", "ACTION_SHOW_TASK_CREATOR", "EXTRA_MODE", "MODE_NOTE", "MODE_TASK", "TAG", "canDrawOverlays", "", "context", "Landroid/content/Context;", "hideOverlay", "", "showNoteCreator", "showTaskCreator", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void showTaskCreator(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        public final void showNoteCreator(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        public final void hideOverlay(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        public final boolean canDrawOverlays(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
    }
}