package com.example.app.audio;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001:\u0001 B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0006J\u0010\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J\u0006\u0010\u0019\u001a\u00020\u0014J\u0018\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\bH\u0002J\u0018\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u001f\u001a\u00020\bH\u0002R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/example/app/audio/AudioRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "amplitudeListener", "Lcom/example/app/audio/AudioRecorder$AmplitudeListener;", "audioFormat", "", "audioRecord", "Landroid/media/AudioRecord;", "channelConfig", "isRecording", "", "outputFile", "", "recordingThread", "Ljava/lang/Thread;", "sampleRate", "setOnAmplitudeListener", "", "listener", "startRecording", "file", "Ljava/io/File;", "stopRecording", "updateWavHeaderInPlace", "raf", "Ljava/io/RandomAccessFile;", "totalAudioLen", "writeAudioDataToFile", "bufferSize", "AmplitudeListener", "app_release"})
public final class AudioRecorder {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private com.example.app.audio.AudioRecorder.AmplitudeListener amplitudeListener;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioRecord audioRecord;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Thread recordingThread;
    @kotlin.jvm.Volatile()
    private volatile boolean isRecording = false;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String outputFile;
    private final int sampleRate = 16000;
    private final int channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT;
    
    public AudioRecorder(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void setOnAmplitudeListener(@org.jetbrains.annotations.Nullable()
    com.example.app.audio.AudioRecorder.AmplitudeListener listener) {
    }
    
    @androidx.annotation.RequiresPermission(value = "android.permission.RECORD_AUDIO")
    public final boolean startRecording(@org.jetbrains.annotations.NotNull()
    java.io.File file) {
        return false;
    }
    
    public final void stopRecording() {
    }
    
    private final void writeAudioDataToFile(java.io.File file, int bufferSize) {
    }
    
    private final void updateWavHeaderInPlace(java.io.RandomAccessFile raf, int totalAudioLen) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lcom/example/app/audio/AudioRecorder$AmplitudeListener;", "", "onAmplitude", "", "amplitude", "", "app_release"})
    public static abstract interface AmplitudeListener {
        
        public abstract void onAmplitude(int amplitude);
    }
}