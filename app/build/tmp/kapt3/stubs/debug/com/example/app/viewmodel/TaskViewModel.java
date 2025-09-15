package com.example.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.work.Data;
import androidx.work.WorkManager;
import com.example.app.data.AppDatabase;
import com.example.app.data.Task;
import com.example.app.data.TaskRepository;
import com.example.app.worker.ReminderWorker;
import kotlinx.coroutines.flow.StateFlow;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0016\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u001b\u001a\u00020\u001cJ0\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020 2\u0006\u0010#\u001a\u00020$2\b\b\u0002\u0010%\u001a\u00020 JA\u0010&\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020 2\u0006\u0010#\u001a\u00020$2\b\b\u0002\u0010%\u001a\u00020 2\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010$\u00a2\u0006\u0002\u0010(J\u000e\u0010)\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$J\u0012\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000bJ\u0012\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000bJ\u0012\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000bJ\u0012\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000bJ\u0006\u0010/\u001a\u00020\u001cJ\u0016\u00100\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u00101\u001a\u00020$J\u000e\u00102\u001a\u00020\u001c2\u0006\u00103\u001a\u00020\u0007J\u0006\u0010\u0015\u001a\u00020\u001cJ\u000e\u00104\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$J\u000e\u00105\u001a\u00020\u001e2\u0006\u00103\u001a\u00020\u0007J\u0016\u00106\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$2\u0006\u0010!\u001a\u00020 J\u0016\u00107\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$2\u0006\u0010#\u001a\u00020$J\u0016\u00108\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$2\u0006\u0010\"\u001a\u00020 J\u0016\u00109\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020$2\u0006\u0010\u001f\u001a\u00020 R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u000e\u00a8\u0006:"}, d2 = {"Lcom/example/app/viewmodel/TaskViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_selectedTask", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/app/data/Task;", "_showCreateTaskDialog", "", "activeTasks", "Landroidx/lifecycle/LiveData;", "", "getActiveTasks", "()Landroidx/lifecycle/LiveData;", "allTasks", "getAllTasks", "selectedTask", "Lkotlinx/coroutines/flow/StateFlow;", "getSelectedTask", "()Lkotlinx/coroutines/flow/StateFlow;", "showCreateTaskDialog", "getShowCreateTaskDialog", "taskRepository", "Lcom/example/app/data/TaskRepository;", "topPriorityTasks", "getTopPriorityTasks", "clearSelectedTask", "", "createTask", "Lkotlinx/coroutines/Job;", "title", "", "description", "priority", "dueDate", "", "duration", "createTaskWithReminder", "reminderMinutes", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;Ljava/lang/Long;)Lkotlinx/coroutines/Job;", "deleteTask", "taskId", "getOverdueTasks", "getTasksDueLater", "getTasksDueToday", "getTasksDueTomorrow", "hideCreateTaskDialog", "scheduleVoiceReminder", "delayMinutes", "selectTask", "task", "toggleTaskComplete", "updateTask", "updateTaskDescription", "updateTaskDueDate", "updateTaskPriority", "updateTaskTitle", "app_debug"})
public final class TaskViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.TaskRepository taskRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> allTasks = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> activeTasks = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> topPriorityTasks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _showCreateTaskDialog = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> showCreateTaskDialog = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.app.data.Task> _selectedTask = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.app.data.Task> selectedTask = null;
    
    public TaskViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getAllTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getActiveTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getTopPriorityTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShowCreateTaskDialog() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.app.data.Task> getSelectedTask() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createTask(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, long dueDate, @org.jetbrains.annotations.NotNull()
    java.lang.String duration) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteTask(long taskId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job toggleTaskComplete(long taskId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTaskTitle(long taskId, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTaskDescription(long taskId, @org.jetbrains.annotations.NotNull()
    java.lang.String description) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTaskPriority(long taskId, @org.jetbrains.annotations.NotNull()
    java.lang.String priority) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTaskDueDate(long taskId, long dueDate) {
        return null;
    }
    
    public final void showCreateTaskDialog() {
    }
    
    public final void hideCreateTaskDialog() {
    }
    
    public final void selectTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task) {
    }
    
    public final void clearSelectedTask() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job scheduleVoiceReminder(@org.jetbrains.annotations.NotNull()
    java.lang.String title, long delayMinutes) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createTaskWithReminder(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, long dueDate, @org.jetbrains.annotations.NotNull()
    java.lang.String duration, @org.jetbrains.annotations.Nullable()
    java.lang.Long reminderMinutes) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getTasksDueToday() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getTasksDueTomorrow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getTasksDueLater() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Task>> getOverdueTasks() {
        return null;
    }
}