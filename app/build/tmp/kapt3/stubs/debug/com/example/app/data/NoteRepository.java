package com.example.app.data;

import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u001e\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u000e\u0010\u0018\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u0016\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010#\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u000e\u0010$\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u0012\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020 0(0\'J\u0012\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0(0\'J\u0012\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0(0\'J\u0012\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020.0(0\'J\u0012\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020 0(0\'J\u0012\u00100\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0(0\'J\u0014\u00101\u001a\b\u0012\u0004\u0012\u00020*0(H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u001a\u00102\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0(0\'2\u0006\u0010\u001b\u001a\u00020\u001cJ\u0012\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0(0\'J\u0016\u00104\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010.0\'2\u0006\u0010\u0015\u001a\u00020\u0016J \u00105\u001a\u00020\u00142\u0006\u00106\u001a\u00020%2\b\b\u0002\u00107\u001a\u000208H\u0086@\u00a2\u0006\u0002\u00109J\u0016\u0010:\u001a\u00020\u00142\u0006\u0010;\u001a\u00020,H\u0086@\u00a2\u0006\u0002\u0010<J\u0016\u0010=\u001a\u00020\u00142\u0006\u0010>\u001a\u00020.H\u0086@\u00a2\u0006\u0002\u0010?J\u0016\u0010@\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010A\u001a\u00020\u00162\u0006\u0010B\u001a\u00020*H\u0086@\u00a2\u0006\u0002\u0010CJ\u0016\u0010D\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u001a\u0010E\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020.0(0\'2\u0006\u0010F\u001a\u00020\u001cJ\u0016\u0010G\u001a\u00020\u00142\u0006\u0010>\u001a\u00020.H\u0086@\u00a2\u0006\u0002\u0010?J\u001e\u0010H\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010I\u001a\u000208H\u0086@\u00a2\u0006\u0002\u0010JJ\u001e\u0010K\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010L\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010MJ\u001e\u0010N\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010O\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010MJ\u001e\u0010P\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010Q\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010MJ\u0016\u0010R\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010S\u001a\u00020\u00142\u0006\u0010B\u001a\u00020*H\u0086@\u00a2\u0006\u0002\u0010CJ\u001e\u0010T\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010U\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010MR\u0014\u0010\u0006\u001a\u00020\u0007X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0014\u0010\u0002\u001a\u00020\u0003X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0014\u0010\b\u001a\u00020\tX\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0014\u0010\u0004\u001a\u00020\u0005X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006V"}, d2 = {"Lcom/example/app/data/NoteRepository;", "", "noteDao", "Lcom/example/app/data/NoteDao;", "taskDao", "Lcom/example/app/data/TaskDao;", "chatMessageDao", "Lcom/example/app/data/ChatMessageDao;", "reminderDao", "Lcom/example/app/data/ReminderDao;", "(Lcom/example/app/data/NoteDao;Lcom/example/app/data/TaskDao;Lcom/example/app/data/ChatMessageDao;Lcom/example/app/data/ReminderDao;)V", "getChatMessageDao$app_debug", "()Lcom/example/app/data/ChatMessageDao;", "getNoteDao$app_debug", "()Lcom/example/app/data/NoteDao;", "getReminderDao$app_debug", "()Lcom/example/app/data/ReminderDao;", "getTaskDao$app_debug", "()Lcom/example/app/data/TaskDao;", "archiveNote", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearChatHistory", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteChatSession", "sessionId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReminder", "reminder", "Lcom/example/app/data/Reminder;", "(Lcom/example/app/data/Reminder;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteReminderById", "deleteTask", "exportAllData", "Lcom/example/app/data/ExportData;", "getActiveReminders", "Lkotlinx/coroutines/flow/Flow;", "", "getActiveTasks", "Lcom/example/app/data/Task;", "getAllChatMessages", "Lcom/example/app/data/ChatMessage;", "getAllNotes", "Lcom/example/app/data/Note;", "getAllReminders", "getAllTasks", "getAllTasksOnce", "getChatMessagesBySession", "getCurrentConversation", "getNoteById", "importData", "exportData", "replaceExisting", "", "(Lcom/example/app/data/ExportData;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertChatMessage", "message", "(Lcom/example/app/data/ChatMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertNote", "note", "(Lcom/example/app/data/Note;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertReminder", "insertTask", "task", "(Lcom/example/app/data/Task;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markReminderCompleted", "searchNotes", "query", "toggleFavorite", "toggleTaskComplete", "isCompleted", "(JZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateChecklistState", "checklistState", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNoteSnippet", "snippet", "updateNoteTitle", "title", "updateReminder", "updateTask", "updateTranscript", "transcript", "app_debug"})
public final class NoteRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteDao noteDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.TaskDao taskDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.ChatMessageDao chatMessageDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.ReminderDao reminderDao = null;
    
    public NoteRepository(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteDao noteDao, @org.jetbrains.annotations.NotNull()
    com.example.app.data.TaskDao taskDao, @org.jetbrains.annotations.NotNull()
    com.example.app.data.ChatMessageDao chatMessageDao, @org.jetbrains.annotations.NotNull()
    com.example.app.data.ReminderDao reminderDao) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.ReminderDao getReminderDao$app_debug() {
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
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Reminder>> getAllReminders() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Reminder>> getActiveReminders() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertReminder(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Reminder reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateReminder(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Reminder reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteReminder(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Reminder reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteReminderById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markReminderCompleted(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object exportAllData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.app.data.ExportData> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object importData(@org.jetbrains.annotations.NotNull()
    com.example.app.data.ExportData exportData, boolean replaceExisting, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}