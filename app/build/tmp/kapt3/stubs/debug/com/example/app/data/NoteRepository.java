package com.example.app.data;

import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\f\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010\u0014\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u0016\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u0017\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u0016\u0010\u001a\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0012\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0\u001d0\u001cJ\u0012\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020 0\u001d0\u001cJ\u0012\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0\u001d0\u001cJ\u0012\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0\u001d0\u001cJ\u0014\u0010$\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001dH\u0086@\u00a2\u0006\u0002\u0010\u0015J\u001a\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020 0\u001d0\u001c2\u0006\u0010\u0017\u001a\u00020\u0018J\u0012\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020 0\u001d0\u001cJ\u0016\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\"0\u001c2\u0006\u0010\u0011\u001a\u00020\u0012J\u0016\u0010(\u001a\u00020\u00102\u0006\u0010)\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010*J\u0016\u0010+\u001a\u00020\u00102\u0006\u0010,\u001a\u00020\"H\u0086@\u00a2\u0006\u0002\u0010-J\u0016\u0010.\u001a\u00020\u00102\u0006\u0010/\u001a\u00020\u001eH\u0086@\u00a2\u0006\u0002\u00100J\u001a\u00101\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0\u001d0\u001c2\u0006\u00102\u001a\u00020\u0018J\u0016\u00103\u001a\u00020\u00102\u0006\u0010,\u001a\u00020\"H\u0086@\u00a2\u0006\u0002\u0010-J\u001e\u00104\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u00105\u001a\u000206H\u0086@\u00a2\u0006\u0002\u00107J\u001e\u00108\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u00109\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010:J\u001e\u0010;\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010<\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010:J\u001e\u0010=\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010>\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010:J\u0016\u0010?\u001a\u00020\u00102\u0006\u0010/\u001a\u00020\u001eH\u0086@\u00a2\u0006\u0002\u00100J\u001e\u0010@\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010A\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010:R\u0014\u0010\u0006\u001a\u00020\u0007X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0014\u0010\u0002\u001a\u00020\u0003X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0014\u0010\u0004\u001a\u00020\u0005X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006B"}, d2 = {"Lcom/example/app/data/NoteRepository;", "", "noteDao", "Lcom/example/app/data/NoteDao;", "taskDao", "Lcom/example/app/data/TaskDao;", "chatMessageDao", "Lcom/example/app/data/ChatMessageDao;", "(Lcom/example/app/data/NoteDao;Lcom/example/app/data/TaskDao;Lcom/example/app/data/ChatMessageDao;)V", "getChatMessageDao$app_debug", "()Lcom/example/app/data/ChatMessageDao;", "getNoteDao$app_debug", "()Lcom/example/app/data/NoteDao;", "getTaskDao$app_debug", "()Lcom/example/app/data/TaskDao;", "archiveNote", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearChatHistory", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteChatSession", "sessionId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteTask", "getActiveTasks", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/app/data/Task;", "getAllChatMessages", "Lcom/example/app/data/ChatMessage;", "getAllNotes", "Lcom/example/app/data/Note;", "getAllTasks", "getAllTasksOnce", "getChatMessagesBySession", "getCurrentConversation", "getNoteById", "insertChatMessage", "message", "(Lcom/example/app/data/ChatMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertNote", "note", "(Lcom/example/app/data/Note;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertTask", "task", "(Lcom/example/app/data/Task;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchNotes", "query", "toggleFavorite", "toggleTaskComplete", "isCompleted", "", "(JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateChecklistState", "checklistState", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNoteSnippet", "snippet", "updateNoteTitle", "title", "updateTask", "updateTranscript", "transcript", "app_debug"})
public final class NoteRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteDao noteDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.TaskDao taskDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.ChatMessageDao chatMessageDao = null;
    
    public NoteRepository(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteDao noteDao, @org.jetbrains.annotations.NotNull()
    com.example.app.data.TaskDao taskDao, @org.jetbrains.annotations.NotNull()
    com.example.app.data.ChatMessageDao chatMessageDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.NoteDao getNoteDao$app_debug() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.TaskDao getTaskDao$app_debug() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.ChatMessageDao getChatMessageDao$app_debug() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertNote(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Note note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTranscript(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String transcript, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateNoteSnippet(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String snippet, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateChecklistState(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String checklistState, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Note>> getAllNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Note>> searchNotes(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.app.data.Note> getNoteById(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object toggleFavorite(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Note note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateNoteTitle(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object archiveNote(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getAllTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllTasksOnce(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.app.data.Task>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Task>> getActiveTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateTask(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Task task, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteTask(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object toggleTaskComplete(long id, boolean isCompleted, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.ChatMessage>> getAllChatMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.ChatMessage>> getChatMessagesBySession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.ChatMessage>> getCurrentConversation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertChatMessage(@org.jetbrains.annotations.NotNull()
    com.example.app.data.ChatMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearChatHistory(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteChatSession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}