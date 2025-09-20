package com.example.app.data;

import android.content.Context;
import kotlinx.coroutines.flow.Flow;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0012\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0012\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u001c\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\r2\b\b\u0002\u0010\u0012\u001a\u00020\u0013J\u0012\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0012\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0012\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0012\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0016\u0010\u001a\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010\u001d\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 J\b\u0010!\u001a\u00020\bH\u0002J\u0016\u0010\"\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010#\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010$\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010&J\u001e\u0010\'\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010(\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010)J\u001e\u0010*\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010+\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010&J\u001e\u0010,\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010-\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010&R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/example/app/data/TaskRepository;", "", "taskDao", "Lcom/example/app/data/TaskDao;", "context", "Landroid/content/Context;", "(Lcom/example/app/data/TaskDao;Landroid/content/Context;)V", "deleteTaskById", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveTasks", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/app/data/Task;", "getAllTasks", "getCompletedTasks", "limit", "", "getOverdueTasks", "getTaskById", "getTasksDueLater", "getTasksDueToday", "getTasksDueTomorrow", "getTopPriorityTasks", "insertTask", "task", "(Lcom/example/app/data/Task;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markTaskCompleted", "isCompleted", "", "(JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshWidget", "updateTask", "updateTaskDescription", "description", "", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTaskDueDate", "dueDate", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTaskPriority", "priority", "updateTaskTitle", "title", "app_debug"})
public final class TaskRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.TaskDao taskDao = null;
    @org.jetbrains.annotations.Nullable()
    private final android.content.Context context = null;
    
    public TaskRepository(@org.jetbrains.annotations.NotNull()
    com.example.app.data.TaskDao taskDao, @org.jetbrains.annotations.Nullable()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getAllTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getActiveTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getCompletedTasks(int limit) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getTaskById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.app.data.Task> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteTaskById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markTaskCompleted(long id, boolean isCompleted, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void refreshWidget() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTaskDueDate(long id, long dueDate, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTaskTitle(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTaskDescription(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTaskPriority(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getTasksDueToday() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getTasksDueTomorrow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getTasksDueLater() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getOverdueTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getTopPriorityTasks() {
        return null;
    }
}