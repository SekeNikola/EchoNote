package com.example.app.ui;

import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.SpanStyle;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.TextFieldValue;
import androidx.compose.ui.text.style.TextDecoration;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000J\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u001aF\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a&\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00062\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a2\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001aX\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u00052\u001e\u0010\u0007\u001a\u001a\u0012\u0004\u0012\u00020\b\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u0017\u0012\u0004\u0012\u00020\u00010\u00162\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aR\u0010\u0019\u001a\u00020\u00012\b\b\u0002\u0010\u001a\u001a\u00020\b2\u001e\u0010\u0007\u001a\u001a\u0012\u0004\u0012\u00020\b\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u0017\u0012\u0004\u0012\u00020\u00010\u00162\u0014\b\u0002\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0007\u001a\u0016\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00030\u00172\u0006\u0010\u001e\u001a\u00020\bH\u0002\u00a8\u0006\u001f"}, d2 = {"CheckboxRow", "", "checkbox", "Lcom/example/app/ui/CheckboxItem;", "onCheckedChange", "Lkotlin/Function1;", "", "onTextChange", "", "onRemove", "Lkotlin/Function0;", "FormatButton", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "isSelected", "onClick", "FormattingToolbar", "state", "Lcom/example/app/ui/RichTextState;", "onStateChange", "onAddCheckbox", "RenderRichText", "Lkotlin/Function2;", "", "onTaskCreated", "RichTextEditor", "initialText", "modifier", "Landroidx/compose/ui/Modifier;", "parseCheckboxes", "text", "app_debug"})
public final class RichTextEditorKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void RichTextEditor(@org.jetbrains.annotations.NotNull()
    java.lang.String initialText, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.util.List<com.example.app.ui.CheckboxItem>, kotlin.Unit> onTextChange, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTaskCreated, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FormattingToolbar(com.example.app.ui.RichTextState state, kotlin.jvm.functions.Function1<? super com.example.app.ui.RichTextState, kotlin.Unit> onStateChange, kotlin.jvm.functions.Function0<kotlin.Unit> onAddCheckbox) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FormatButton(androidx.compose.ui.graphics.vector.ImageVector icon, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RenderRichText(com.example.app.ui.RichTextState state, kotlin.jvm.functions.Function1<? super com.example.app.ui.RichTextState, kotlin.Unit> onStateChange, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.util.List<com.example.app.ui.CheckboxItem>, kotlin.Unit> onTextChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTaskCreated) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CheckboxRow(com.example.app.ui.CheckboxItem checkbox, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onCheckedChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTextChange, kotlin.jvm.functions.Function0<kotlin.Unit> onRemove) {
    }
    
    private static final java.util.List<com.example.app.ui.CheckboxItem> parseCheckboxes(java.lang.String text) {
        return null;
    }
}