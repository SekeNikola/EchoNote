package com.example.app.ui;

import android.media.MediaPlayer;
import android.widget.Toast;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import com.example.app.viewmodel.NoteViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/example/app/ui/PlaybackState;", "", "(Ljava/lang/String;I)V", "Idle", "Playing", "Paused", "app_debug"})
public enum PlaybackState {
    /*public static final*/ Idle /* = new Idle() */,
    /*public static final*/ Playing /* = new Playing() */,
    /*public static final*/ Paused /* = new Paused() */;
    
    PlaybackState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.example.app.ui.PlaybackState> getEntries() {
        return null;
    }
}