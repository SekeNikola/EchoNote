package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.window.PopupProperties;
import androidx.navigation.NavController;
import com.example.app.data.ChatMessage;
import com.example.app.viewmodel.NoteViewModel;
import android.net.Uri;
import coil.request.ImageRequest;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0018\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007\u001a\u001e\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0007\u001a\b\u0010\u0010\u001a\u00020\u0001H\u0007\u001a\u001e\u0010\u0011\u001a\u00020\u00012\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010\u0013H\u0007\u00a8\u0006\u0014"}, d2 = {"AiChatScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Lcom/example/app/viewmodel/NoteViewModel;", "ChatMessageItem", "message", "Lcom/example/app/data/ChatMessage;", "isUser", "", "SuggestionChip", "text", "", "onClick", "Lkotlin/Function0;", "TypingIndicator", "WelcomeMessage", "onSuggestionClick", "Lkotlin/Function1;", "app_debug"})
public final class AiChatScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AiChatScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.NoteViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void WelcomeMessage(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSuggestionClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SuggestionChip(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChatMessageItem(@org.jetbrains.annotations.NotNull()
    com.example.app.data.ChatMessage message, boolean isUser) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TypingIndicator() {
    }
}