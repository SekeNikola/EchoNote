package com.example.app.worker;

import android.content.Context;
import androidx.work.Data;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J&\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\bJ&\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\b\u00a8\u0006\u0011"}, d2 = {"Lcom/example/app/worker/ReminderScheduler;", "", "()V", "cancelTaskReminder", "", "context", "Landroid/content/Context;", "taskId", "", "scheduleRecurringNotifications", "scheduleReminder", "noteId", "noteTitle", "", "delayMillis", "scheduleTaskReminder", "taskTitle", "app_debug"})
public final class ReminderScheduler {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.worker.ReminderScheduler INSTANCE = null;
    
    private ReminderScheduler() {
        super();
    }
    
    public final void scheduleReminder(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String noteTitle, long delayMillis) {
    }
    
    public final void scheduleTaskReminder(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long taskId, @org.jetbrains.annotations.NotNull()
    java.lang.String taskTitle, long delayMillis) {
    }
    
    public final void cancelTaskReminder(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long taskId) {
    }
    
    public final void scheduleRecurringNotifications(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}