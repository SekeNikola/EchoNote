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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a<\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u001e\u0010\u0004\u001a\u001a\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a0\u0010\n\u001a\u00020\u00012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u00062\u0018\u0010\r\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u0006\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a(\u0010\u000f\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u00102\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0006H\u0002\u001a\u0016\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u00062\u0006\u0010\u0012\u001a\u00020\u0003H\u0002\u00a8\u0006\u0013"}, d2 = {"RichTextEditor", "", "initialText", "", "onTextChange", "Lkotlin/Function2;", "", "Lcom/example/app/ui/CheckboxItem;", "modifier", "Landroidx/compose/ui/Modifier;", "RichTextEditorWithCheckboxes", "items", "Lcom/example/app/ui/EditorItem;", "onItemsChange", "Lkotlin/Function1;", "convertEditorItemsToOutput", "Lkotlin/Pair;", "parseTextToEditorItems", "text", "app_debug"})
public final class RichTextEditorKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void RichTextEditor(@org.jetbrains.annotations.NotNull()
    java.lang.String initialText, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.util.List<com.example.app.ui.CheckboxItem>, kotlin.Unit> onTextChange, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RichTextEditorWithCheckboxes(java.util.List<? extends com.example.app.ui.EditorItem> items, kotlin.jvm.functions.Function1<? super java.util.List<? extends com.example.app.ui.EditorItem>, kotlin.Unit> onItemsChange) {
    }
    
    private static final java.util.List<com.example.app.ui.EditorItem> parseTextToEditorItems(java.lang.String text) {
        return null;
    }
    
    private static final kotlin.Pair<java.lang.String, java.util.List<com.example.app.ui.CheckboxItem>> convertEditorItemsToOutput(java.util.List<? extends com.example.app.ui.EditorItem> items) {
        return null;
    }
}