package com.example.app.server;

import com.example.app.data.AppDatabase;
import com.example.app.data.Task;
import com.example.app.data.Note;
import kotlinx.coroutines.*;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0005\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0012\u0010\t\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0004J\u001c\u0010\u0012\u001a\u00020\b2\b\u0010\u0013\u001a\u0004\u0018\u00010\u00062\b\u0010\u0014\u001a\u0004\u0018\u00010\u0006H\u0002J\u0016\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010\u0019\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001b\u001a\u00020\u000b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0086@\u00a2\u0006\u0002\u0010\u001eJ\u000e\u0010\u001f\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u001aR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/example/app/server/DataSyncManager;", "", "()V", "database", "Lcom/example/app/data/AppDatabase;", "formatTimestampToDate", "", "timestamp", "", "formatTimestampToTime", "handleNoteDeletion", "", "noteTitle", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleTaskDeletion", "taskTitle", "initialize", "db", "parseDueDateAndTime", "dateString", "timeString", "syncNoteToDatabase", "serverNote", "Lcom/example/app/server/ServerNote;", "(Lcom/example/app/server/ServerNote;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncNotesFromDatabase", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncTaskToDatabase", "serverTask", "Lcom/example/app/server/ServerTask;", "(Lcom/example/app/server/ServerTask;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncTasksFromDatabase", "app_debug"})
public final class DataSyncManager {
    @org.jetbrains.annotations.Nullable()
    private static com.example.app.data.AppDatabase database;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.server.DataSyncManager INSTANCE = null;
    
    private DataSyncManager() {
        super();
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    com.example.app.data.AppDatabase db) {
    }
    
    private final long parseDueDateAndTime(java.lang.String dateString, java.lang.String timeString) {
        return 0L;
    }
    
    private final java.lang.String formatTimestampToDate(long timestamp) {
        return null;
    }
    
    private final java.lang.String formatTimestampToTime(long timestamp) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncTasksFromDatabase(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncNotesFromDatabase(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncTaskToDatabase(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerTask serverTask, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncNoteToDatabase(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerNote serverNote, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object handleTaskDeletion(@org.jetbrains.annotations.NotNull()
    java.lang.String taskTitle, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object handleNoteDeletion(@org.jetbrains.annotations.NotNull()
    java.lang.String noteTitle, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}