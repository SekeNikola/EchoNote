package com.example.app.server;

import com.example.app.data.AppDatabase;
import com.example.app.data.Task;
import com.example.app.data.Note;
import kotlinx.coroutines.*;
import android.util.Log;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004J\u0016\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u000e\u0010\f\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u000e\u0010\u0012\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\rR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/example/app/server/DataSyncManager;", "", "()V", "database", "Lcom/example/app/data/AppDatabase;", "initialize", "", "db", "syncNoteToDatabase", "serverNote", "Lcom/example/app/server/ServerNote;", "(Lcom/example/app/server/ServerNote;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncNotesFromDatabase", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncTaskToDatabase", "serverTask", "Lcom/example/app/server/ServerTask;", "(Lcom/example/app/server/ServerTask;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncTasksFromDatabase", "app_debug"})
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
}