package com.example.app.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.WorkManager;
import com.example.app.R;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bH\u0002J\b\u0010\f\u001a\u00020\rH\u0002J\b\u0010\u000e\u001a\u00020\rH\u0002J\u000e\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\bJ\u001e\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\bJ\u0016\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\b\u00a8\u0006\u0017"}, d2 = {"Lcom/example/app/worker/NotificationHelper;", "", "()V", "createNotificationChannel", "", "context", "Landroid/content/Context;", "channelId", "", "name", "importance", "", "getTimeUntil8AM", "", "getTimeUntil9AM", "scheduleDailySummaryNotification", "scheduleFocusNotification", "showErrorNotification", "errorMessage", "showSuccessNotification", "title", "message", "showSyncNotification", "app_debug"})
public final class NotificationHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.worker.NotificationHelper INSTANCE = null;
    
    private NotificationHelper() {
        super();
    }
    
    public final void showSuccessNotification(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void scheduleDailySummaryNotification(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void scheduleFocusNotification(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void showSyncNotification(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void showErrorNotification(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String errorMessage) {
    }
    
    private final void createNotificationChannel(android.content.Context context, java.lang.String channelId, java.lang.String name, int importance) {
    }
    
    private final long getTimeUntil8AM() {
        return 0L;
    }
    
    private final long getTimeUntil9AM() {
        return 0L;
    }
}