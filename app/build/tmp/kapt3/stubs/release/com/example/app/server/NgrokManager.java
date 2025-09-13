package com.example.app.server;

import android.util.Log;
import kotlinx.coroutines.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\n\u0010\b\u001a\u0004\u0018\u00010\u0004H\u0002J\b\u0010\t\u001a\u0004\u0018\u00010\u0004J\b\u0010\n\u001a\u0004\u0018\u00010\u0004J\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\fJ\u0010\u0010\r\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u0010\u000eJ\u000e\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0006\u0010\u0011\u001a\u00020\u0012R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/example/app/server/NgrokManager;", "", "()V", "localUrl", "", "ngrokProcess", "Ljava/lang/Process;", "publicUrl", "getLocalIpAddress", "getLocalUrl", "getPublicUrl", "getServerUrls", "", "getTunnelUrl", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startTunnel", "", "stopTunnel", "", "app_release"})
public final class NgrokManager {
    @org.jetbrains.annotations.Nullable()
    private static java.lang.Process ngrokProcess;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String publicUrl;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String localUrl;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.server.NgrokManager INSTANCE = null;
    
    private NgrokManager() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object startTunnel(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    public final void stopTunnel() {
    }
    
    private final java.lang.String getLocalIpAddress() {
        return null;
    }
    
    private final java.lang.Object getTunnelUrl(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPublicUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLocalUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getServerUrls() {
        return null;
    }
}