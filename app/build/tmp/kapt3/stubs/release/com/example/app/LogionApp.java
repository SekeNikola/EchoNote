package com.example.app;

import androidx.compose.runtime.*;
import android.app.Application;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import com.example.app.data.AppDatabase;
import com.example.app.data.NoteRepository;
import com.example.app.viewmodel.NoteViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.app.util.ApiKeyProvider;
import com.example.app.network.RetrofitInstance;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0016R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/example/app/LogionApp;", "Landroid/app/Application;", "()V", "database", "Lcom/example/app/data/AppDatabase;", "getDatabase", "()Lcom/example/app/data/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "onCreate", "", "app_release"})
public final class LogionApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    
    public LogionApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.AppDatabase getDatabase() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
}