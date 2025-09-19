package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.TextFieldValue;
import androidx.compose.ui.text.style.TextDecoration;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;
import coil.request.ImageRequest;
import com.example.app.viewmodel.NoteViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000H\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aH\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u000326\u0010\u0004\u001a2\u0012\u0013\u0012\u00110\u0006\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\t\u0012\u0013\u0012\u00110\n\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\u000b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a&\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0007\u001a.\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u001a\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0003H\u0002\u00a8\u0006\u001a"}, d2 = {"NoteContentDisplay", "", "content", "", "onCheckboxChange", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "index", "", "newCheckedState", "NoteDetailScreen", "noteId", "viewModel", "Lcom/example/app/viewmodel/NoteViewModel;", "onBack", "Lkotlin/Function0;", "SimpleNoteEditor", "onContentChange", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "parseNoteContent", "Lcom/example/app/ui/NoteContentData;", "snippet", "app_debug"})
public final class NoteDetailScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void NoteDetailScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String noteId, @org.jetbrains.annotations.NotNull()
    com.example.app.viewmodel.NoteViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    private static final com.example.app.ui.NoteContentData parseNoteContent(java.lang.String snippet) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NoteContentDisplay(java.lang.String content, kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> onCheckboxChange) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SimpleNoteEditor(java.lang.String content, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onContentChange, androidx.compose.ui.Modifier modifier) {
    }
}