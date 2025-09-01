package com.example.app.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\t\u001a\u0004\u0018\u00010\bJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/example/app/audio/CompressedAudioRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "mediaRecorder", "Landroid/media/MediaRecorder;", "outputFile", "", "getOutputFile", "startRecording", "", "file", "Ljava/io/File;", "stopRecording", "", "app_debug"})
public final class CompressedAudioRecorder {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaRecorder mediaRecorder;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String outputFile;
    
    public CompressedAudioRecorder(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean startRecording(@org.jetbrains.annotations.NotNull()
    java.io.File file) {
        return false;
    }
    
    public final void stopRecording() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getOutputFile() {
        return null;
    }
}