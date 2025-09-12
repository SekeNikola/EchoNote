package com.example.app.server;

import io.ktor.server.application.*;
import io.ktor.server.engine.*;
import io.ktor.server.netty.*;
import io.ktor.server.routing.*;
import io.ktor.server.websocket.*;
import io.ktor.websocket.*;
import io.ktor.server.request.*;
import io.ktor.server.response.*;
import io.ktor.serialization.kotlinx.json.*;
import io.ktor.server.plugins.contentnegotiation.*;
import io.ktor.server.plugins.cors.routing.*;
import io.ktor.http.*;
import kotlinx.serialization.Serializable;
import kotlinx.coroutines.channels.ClosedReceiveChannelException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import android.util.Log;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010#\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u001b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\tJ\u0016\u0010\u000f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u000e\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u000bJ\u0016\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0018J\u0006\u0010\u0019\u001a\u00020\rJ\u0016\u0010\u001a\u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001d\u001a\u00020\r2\u0006\u0010\u001e\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001f\u001a\u00020\r2\u0006\u0010 \u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010!\u001a\u00020\r2\u0006\u0010\"\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\b\u0010#\u001a\u00020\bH\u0002J\u000e\u0010$\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010%J\u0016\u0010&\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\'\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0014R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u000b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/example/app/server/KtorServer;", "", "()V", "connections", "", "Lio/ktor/websocket/DefaultWebSocketSession;", "notes", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Lcom/example/app/server/ServerNote;", "tasks", "Lcom/example/app/server/ServerTask;", "addNote", "", "note", "addNoteWithBroadcast", "(Lcom/example/app/server/ServerNote;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addTask", "task", "addTaskWithBroadcast", "(Lcom/example/app/server/ServerTask;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "broadcastSync", "type", "data", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearData", "deleteNoteWithBroadcast", "noteId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteNoteWithBroadcastByTitle", "noteTitle", "deleteTaskWithBroadcast", "taskId", "deleteTaskWithBroadcastByTitle", "taskTitle", "getCurrentTimestamp", "start", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNoteWithBroadcast", "updateTaskWithBroadcast", "app_debug"})
public final class KtorServer {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.example.app.server.ServerTask> tasks = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.example.app.server.ServerNote> notes = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<io.ktor.websocket.DefaultWebSocketSession> connections = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.server.KtorServer INSTANCE = null;
    
    private KtorServer() {
        super();
    }
    
    private final java.lang.String getCurrentTimestamp() {
        return null;
    }
    
    public final void addTask(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerTask task) {
    }
    
    public final void addNote(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerNote note) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateNoteWithBroadcast(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerNote note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTaskWithBroadcast(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerTask task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addNoteWithBroadcast(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerNote note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addTaskWithBroadcast(@org.jetbrains.annotations.NotNull()
    com.example.app.server.ServerTask task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteNoteWithBroadcast(@org.jetbrains.annotations.NotNull()
    java.lang.String noteId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteNoteWithBroadcastByTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String noteTitle, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteTaskWithBroadcast(@org.jetbrains.annotations.NotNull()
    java.lang.String taskId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteTaskWithBroadcastByTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String taskTitle, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void clearData() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object start(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object broadcastSync(java.lang.String type, java.lang.String data, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}