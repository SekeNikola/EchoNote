package com.example.app.navigation;

import androidx.compose.runtime.Composable;
import androidx.navigation.NavHostController;
import com.example.app.ui.*;
import com.example.app.viewmodel.NoteViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0012\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018B\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\u0012\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&\'()*\u00a8\u0006+"}, d2 = {"Lcom/example/app/navigation/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "AiChat", "AiVoice", "Chats", "DocumentUpload", "Home", "ImageCapture", "ImagePreview", "NoteDetail", "Notes", "Recording", "TaskDetail", "Tasks", "TypeText", "UploadAudio", "UploadImage", "VideoUrl", "VoiceCommand", "WebPage", "Lcom/example/app/navigation/Screen$AiChat;", "Lcom/example/app/navigation/Screen$AiVoice;", "Lcom/example/app/navigation/Screen$Chats;", "Lcom/example/app/navigation/Screen$DocumentUpload;", "Lcom/example/app/navigation/Screen$Home;", "Lcom/example/app/navigation/Screen$ImageCapture;", "Lcom/example/app/navigation/Screen$ImagePreview;", "Lcom/example/app/navigation/Screen$NoteDetail;", "Lcom/example/app/navigation/Screen$Notes;", "Lcom/example/app/navigation/Screen$Recording;", "Lcom/example/app/navigation/Screen$TaskDetail;", "Lcom/example/app/navigation/Screen$Tasks;", "Lcom/example/app/navigation/Screen$TypeText;", "Lcom/example/app/navigation/Screen$UploadAudio;", "Lcom/example/app/navigation/Screen$UploadImage;", "Lcom/example/app/navigation/Screen$VideoUrl;", "Lcom/example/app/navigation/Screen$VoiceCommand;", "Lcom/example/app/navigation/Screen$WebPage;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$AiChat;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class AiChat extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.AiChat INSTANCE = null;
        
        private AiChat() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$AiVoice;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class AiVoice extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.AiVoice INSTANCE = null;
        
        private AiVoice() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$Chats;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class Chats extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.Chats INSTANCE = null;
        
        private Chats() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$DocumentUpload;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class DocumentUpload extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.DocumentUpload INSTANCE = null;
        
        private DocumentUpload() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$Home;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class Home extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.Home INSTANCE = null;
        
        private Home() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$ImageCapture;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class ImageCapture extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.ImageCapture INSTANCE = null;
        
        private ImageCapture() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$ImagePreview;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class ImagePreview extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.ImagePreview INSTANCE = null;
        
        private ImagePreview() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u0003J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\nH\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0012"}, d2 = {"Lcom/example/app/navigation/Screen$NoteDetail;", "Lcom/example/app/navigation/Screen;", "noteId", "", "(J)V", "getNoteId", "()J", "component1", "copy", "createRoute", "", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class NoteDetail extends com.example.app.navigation.Screen {
        private final long noteId = 0L;
        
        public NoteDetail(long noteId) {
        }
        
        public final long getNoteId() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(long noteId) {
            return null;
        }
        
        public final long component1() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.app.navigation.Screen.NoteDetail copy(long noteId) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$Notes;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class Notes extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.Notes INSTANCE = null;
        
        private Notes() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$Recording;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class Recording extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.Recording INSTANCE = null;
        
        private Recording() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u0003J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\nH\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0012"}, d2 = {"Lcom/example/app/navigation/Screen$TaskDetail;", "Lcom/example/app/navigation/Screen;", "taskId", "", "(J)V", "getTaskId", "()J", "component1", "copy", "createRoute", "", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class TaskDetail extends com.example.app.navigation.Screen {
        private final long taskId = 0L;
        
        public TaskDetail(long taskId) {
        }
        
        public final long getTaskId() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(long taskId) {
            return null;
        }
        
        public final long component1() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.app.navigation.Screen.TaskDetail copy(long taskId) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$Tasks;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class Tasks extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.Tasks INSTANCE = null;
        
        private Tasks() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$TypeText;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class TypeText extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.TypeText INSTANCE = null;
        
        private TypeText() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$UploadAudio;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class UploadAudio extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.UploadAudio INSTANCE = null;
        
        private UploadAudio() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$UploadImage;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class UploadImage extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.UploadImage INSTANCE = null;
        
        private UploadImage() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$VideoUrl;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class VideoUrl extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.VideoUrl INSTANCE = null;
        
        private VideoUrl() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$VoiceCommand;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class VoiceCommand extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.VoiceCommand INSTANCE = null;
        
        private VoiceCommand() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/app/navigation/Screen$WebPage;", "Lcom/example/app/navigation/Screen;", "()V", "app_debug"})
    public static final class WebPage extends com.example.app.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.example.app.navigation.Screen.WebPage INSTANCE = null;
        
        private WebPage() {
        }
    }
}