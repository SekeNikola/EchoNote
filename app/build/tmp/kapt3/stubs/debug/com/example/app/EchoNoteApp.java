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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016\u00a8\u0006\u0005"}, d2 = {"Lcom/example/app/EchoNoteApp;", "Landroid/app/Application;", "()V", "onCreate", "", "app_debug"})
public final class EchoNoteApp extends android.app.Application {
    
    public EchoNoteApp() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
}