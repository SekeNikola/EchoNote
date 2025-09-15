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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BQ\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\nH\u00c6\u0003J\u000f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00c6\u0003JU\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u00052\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010 \u001a\u00020\nH\u00d6\u0001J\t\u0010!\u001a\u00020\"H\u00d6\u0001R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0013R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0013R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0013R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0013R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006#"}, d2 = {"Lcom/example/app/ui/RichTextState;", "", "text", "Landroidx/compose/ui/text/input/TextFieldValue;", "isBold", "", "isItalic", "isUnderline", "isStrikethrough", "fontSize", "", "checkboxes", "", "Lcom/example/app/ui/CheckboxItem;", "(Landroidx/compose/ui/text/input/TextFieldValue;ZZZZILjava/util/List;)V", "getCheckboxes", "()Ljava/util/List;", "getFontSize", "()I", "()Z", "getText", "()Landroidx/compose/ui/text/input/TextFieldValue;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "", "app_debug"})
public final class RichTextState {
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.ui.text.input.TextFieldValue text = null;
    private final boolean isBold = false;
    private final boolean isItalic = false;
    private final boolean isUnderline = false;
    private final boolean isStrikethrough = false;
    private final int fontSize = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.app.ui.CheckboxItem> checkboxes = null;
    
    public RichTextState(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.text.input.TextFieldValue text, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough, int fontSize, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.ui.CheckboxItem> checkboxes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.text.input.TextFieldValue getText() {
        return null;
    }
    
    public final boolean isBold() {
        return false;
    }
    
    public final boolean isItalic() {
        return false;
    }
    
    public final boolean isUnderline() {
        return false;
    }
    
    public final boolean isStrikethrough() {
        return false;
    }
    
    public final int getFontSize() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.ui.CheckboxItem> getCheckboxes() {
        return null;
    }
    
    public RichTextState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.text.input.TextFieldValue component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.ui.CheckboxItem> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.ui.RichTextState copy(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.text.input.TextFieldValue text, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough, int fontSize, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.ui.CheckboxItem> checkboxes) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}