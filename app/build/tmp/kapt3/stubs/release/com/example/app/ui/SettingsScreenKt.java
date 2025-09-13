package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.navigation.NavController;
import com.example.app.viewmodel.NoteViewModel;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.compose.ui.text.input.VisualTransformation;
import com.example.app.utils.OpenAITTS;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0018\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002\u001a\u0018\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002\u001a\u0018\u0010\r\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002\u00a8\u0006\u000e"}, d2 = {"SettingsScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Lcom/example/app/viewmodel/NoteViewModel;", "playVoicePreview", "voice", "", "context", "Landroid/content/Context;", "saveApiKey", "apiKey", "saveVoicePreference", "app_release"})
public final class SettingsScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.NoteViewModel viewModel) {
    }
    
    private static final void playVoicePreview(java.lang.String voice, android.content.Context context) {
    }
    
    private static final void saveVoicePreference(java.lang.String voice, android.content.Context context) {
    }
    
    private static final void saveApiKey(java.lang.String apiKey, android.content.Context context) {
    }
}