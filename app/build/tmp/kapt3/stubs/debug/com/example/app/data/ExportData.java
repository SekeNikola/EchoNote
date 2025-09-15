package com.example.app.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Data class for exporting/importing all app data
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\b\u0018\u0000 !2\u00020\u0001:\u0001!BI\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J\u000f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\t0\u0006H\u00c6\u0003J\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006H\u00c6\u0003JM\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00062\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\u0006\u0010\u001f\u001a\u00020\u0003J\t\u0010 \u001a\u00020\u0003H\u00d6\u0001R\u001c\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001c\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u001c\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010\u00a8\u0006\""}, d2 = {"Lcom/example/app/data/ExportData;", "", "version", "", "exportDate", "notes", "", "Lcom/example/app/data/Note;", "tasks", "Lcom/example/app/data/Task;", "chatMessages", "Lcom/example/app/data/ChatMessage;", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", "getChatMessages", "()Ljava/util/List;", "getExportDate", "()Ljava/lang/String;", "getNotes", "getTasks", "getVersion", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toJson", "toString", "Companion", "app_debug"})
public final class ExportData {
    @com.google.gson.annotations.SerializedName(value = "version")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String version = null;
    @com.google.gson.annotations.SerializedName(value = "exportDate")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String exportDate = null;
    @com.google.gson.annotations.SerializedName(value = "notes")
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.app.data.Note> notes = null;
    @com.google.gson.annotations.SerializedName(value = "tasks")
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.app.data.Task> tasks = null;
    @com.google.gson.annotations.SerializedName(value = "chatMessages")
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.app.data.ChatMessage> chatMessages = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.data.ExportData.Companion Companion = null;
    
    public ExportData(@org.jetbrains.annotations.NotNull()
    java.lang.String version, @org.jetbrains.annotations.NotNull()
    java.lang.String exportDate, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.Note> notes, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.Task> tasks, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.ChatMessage> chatMessages) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getVersion() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getExportDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.Note> getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.Task> getTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.ChatMessage> getChatMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String toJson() {
        return null;
    }
    
    public ExportData() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.Note> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.Task> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.ChatMessage> component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.ExportData copy(@org.jetbrains.annotations.NotNull()
    java.lang.String version, @org.jetbrains.annotations.NotNull()
    java.lang.String exportDate, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.Note> notes, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.Task> tasks, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.ChatMessage> chatMessages) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\b\u0010\u0007\u001a\u00020\u0006H\u0002\u00a8\u0006\b"}, d2 = {"Lcom/example/app/data/ExportData$Companion;", "", "()V", "fromJson", "Lcom/example/app/data/ExportData;", "json", "", "getCurrentTimestamp", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        private final java.lang.String getCurrentTimestamp() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.example.app.data.ExportData fromJson(@org.jetbrains.annotations.NotNull()
        java.lang.String json) {
            return null;
        }
    }
}