package com.example.app.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.app.R;
import android.util.Log;
import android.appwidget.AppWidgetManager;
import com.example.app.data.AppDatabase;
import com.example.app.data.Note;
import com.example.app.data.Task;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00072\u00020\u0001:\u0002\u0007\bB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/example/app/widget/LogionListRemoteViewsService;", "Landroid/widget/RemoteViewsService;", "()V", "onGetViewFactory", "Landroid/widget/RemoteViewsService$RemoteViewsFactory;", "intent", "Landroid/content/Intent;", "Companion", "LogionListFactory", "app_debug"})
public final class LogionListRemoteViewsService extends android.widget.RemoteViewsService {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_MODE = "mode";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_APPWIDGET_ID = "appWidgetId";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MODE_TASKS = "tasks";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MODE_NOTES = "notes";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ITEM_TYPE = "item_type";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ITEM_ID = "item_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_TOGGLE = "toggle";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_TASK = "task";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TYPE_NOTE = "note";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.widget.LogionListRemoteViewsService.Companion Companion = null;
    
    public LogionListRemoteViewsService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.widget.RemoteViewsService.RemoteViewsFactory onGetViewFactory(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/app/widget/LogionListRemoteViewsService$Companion;", "", "()V", "EXTRA_APPWIDGET_ID", "", "EXTRA_ITEM_ID", "EXTRA_ITEM_TYPE", "EXTRA_MODE", "EXTRA_TOGGLE", "MODE_NOTES", "MODE_TASKS", "TYPE_NOTE", "TYPE_TASK", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0005H\u0002J\u0010\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0005H\u0002J\b\u0010\u0019\u001a\u00020\u0005H\u0016J\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0017\u001a\u00020\u0005H\u0016J\n\u0010\u001c\u001a\u0004\u0018\u00010\u0016H\u0016J\u0012\u0010\u001d\u001a\u0004\u0018\u00010\u00162\u0006\u0010\u0017\u001a\u00020\u0005H\u0016J\b\u0010\u001e\u001a\u00020\u0005H\u0016J\b\u0010\u001f\u001a\u00020 H\u0016J\b\u0010!\u001a\u00020\"H\u0016J\b\u0010#\u001a\u00020\"H\u0016J\b\u0010$\u001a\u00020\"H\u0016J\u000e\u0010%\u001a\u00020\"2\u0006\u0010&\u001a\u00020\u0007R\u000e\u0010\t\u001a\u00020\u0007X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/example/app/widget/LogionListRemoteViewsService$LogionListFactory;", "Landroid/widget/RemoteViewsService$RemoteViewsFactory;", "context", "Landroid/content/Context;", "appWidgetId", "", "mode", "", "(Landroid/content/Context;ILjava/lang/String;)V", "TAG", "db", "Lcom/example/app/data/AppDatabase;", "getDb", "()Lcom/example/app/data/AppDatabase;", "db$delegate", "Lkotlin/Lazy;", "notes", "", "Lcom/example/app/data/Note;", "tasks", "Lcom/example/app/data/Task;", "buildNoteView", "Landroid/widget/RemoteViews;", "position", "buildTaskView", "getCount", "getItemId", "", "getLoadingView", "getViewAt", "getViewTypeCount", "hasStableIds", "", "onCreate", "", "onDataSetChanged", "onDestroy", "updateMode", "newMode", "app_debug"})
    public static final class LogionListFactory implements android.widget.RemoteViewsService.RemoteViewsFactory {
        @org.jetbrains.annotations.NotNull()
        private final android.content.Context context = null;
        private final int appWidgetId = 0;
        @org.jetbrains.annotations.NotNull()
        private java.lang.String mode;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String TAG = "LogionWidgetSvc";
        @org.jetbrains.annotations.NotNull()
        private final kotlin.Lazy db$delegate = null;
        @org.jetbrains.annotations.NotNull()
        private java.util.List<com.example.app.data.Task> tasks;
        @org.jetbrains.annotations.NotNull()
        private java.util.List<com.example.app.data.Note> notes;
        
        public LogionListFactory(@org.jetbrains.annotations.NotNull()
        android.content.Context context, int appWidgetId, @org.jetbrains.annotations.NotNull()
        java.lang.String mode) {
            super();
        }
        
        private final com.example.app.data.AppDatabase getDb() {
            return null;
        }
        
        @java.lang.Override()
        public void onCreate() {
        }
        
        @java.lang.Override()
        public void onDataSetChanged() {
        }
        
        @java.lang.Override()
        public void onDestroy() {
        }
        
        @java.lang.Override()
        public int getCount() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.Nullable()
        public android.widget.RemoteViews getViewAt(int position) {
            return null;
        }
        
        private final android.widget.RemoteViews buildTaskView(int position) {
            return null;
        }
        
        private final android.widget.RemoteViews buildNoteView(int position) {
            return null;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.Nullable()
        public android.widget.RemoteViews getLoadingView() {
            return null;
        }
        
        @java.lang.Override()
        public int getViewTypeCount() {
            return 0;
        }
        
        @java.lang.Override()
        public long getItemId(int position) {
            return 0L;
        }
        
        @java.lang.Override()
        public boolean hasStableIds() {
            return false;
        }
        
        public final void updateMode(@org.jetbrains.annotations.NotNull()
        java.lang.String newMode) {
        }
    }
}