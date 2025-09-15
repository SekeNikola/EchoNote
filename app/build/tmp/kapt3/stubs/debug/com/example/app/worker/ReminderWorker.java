package com.example.app.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;
import com.example.app.MainActivity;
import com.example.app.R;
import com.example.app.receiver.NotificationActionReceiver;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u0000 !2\u00020\u0001:\u0001!B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J \u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\bH\u0002J\u000e\u0010\u0014\u001a\u00020\u0015H\u0096@\u00a2\u0006\u0002\u0010\u0016J\b\u0010\u0017\u001a\u00020\u000eH\u0002J\u0010\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\nH\u0002J\b\u0010\u001a\u001a\u00020\u000eH\u0002J\u0018\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\n2\u0006\u0010\u001d\u001a\u00020\fH\u0002J\u0010\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\nH\u0002J\u0018\u0010 \u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002\u00a8\u0006\""}, d2 = {"Lcom/example/app/worker/ReminderWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "createActionIntent", "Landroid/app/PendingIntent;", "action", "", "taskId", "", "createNotificationChannel", "", "channelId", "name", "importance", "", "createOpenAppIntent", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "showDailySummaryNotification", "showErrorNotification", "errorMessage", "showFocusNotification", "showNotification", "title", "noteId", "showSyncNotification", "message", "showTaskReminderNotification", "Companion", "app_debug"})
public final class ReminderWorker extends androidx.work.CoroutineWorker {
    public static final int DAILY_SUMMARY_ID = 1001;
    public static final int FOCUS_NOTIFICATION_ID = 1002;
    public static final int SYNC_NOTIFICATION_ID = 1003;
    public static final int ERROR_NOTIFICATION_ID = 1004;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.worker.ReminderWorker.Companion Companion = null;
    
    public ReminderWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    private final void showTaskReminderNotification(java.lang.String title, long taskId) {
    }
    
    private final void showDailySummaryNotification() {
    }
    
    private final void showFocusNotification() {
    }
    
    private final void showSyncNotification(java.lang.String message) {
    }
    
    private final void showErrorNotification(java.lang.String errorMessage) {
    }
    
    private final void showNotification(java.lang.String title, long noteId) {
    }
    
    private final void createNotificationChannel(java.lang.String channelId, java.lang.String name, int importance) {
    }
    
    private final android.app.PendingIntent createActionIntent(java.lang.String action, long taskId) {
        return null;
    }
    
    private final android.app.PendingIntent createOpenAppIntent() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/example/app/worker/ReminderWorker$Companion;", "", "()V", "DAILY_SUMMARY_ID", "", "ERROR_NOTIFICATION_ID", "FOCUS_NOTIFICATION_ID", "SYNC_NOTIFICATION_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}