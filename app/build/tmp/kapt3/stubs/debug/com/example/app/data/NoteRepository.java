package com.example.app.data;

import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\t\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0012\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rJ\u0016\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\r2\u0006\u0010\t\u001a\u00020\nJ\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\r2\u0006\u0010\u0012\u001a\u00020\u0013J\u0016\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001a\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0019R\u0014\u0010\u0002\u001a\u00020\u0003X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u001c"}, d2 = {"Lcom/example/app/data/NoteRepository;", "", "noteDao", "Lcom/example/app/data/NoteDao;", "(Lcom/example/app/data/NoteDao;)V", "getNoteDao$app_debug", "()Lcom/example/app/data/NoteDao;", "archiveNote", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllNotes", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/app/data/Note;", "getNoteById", "searchNotes", "query", "", "toggleFavorite", "note", "(Lcom/example/app/data/Note;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateChecklistState", "checklistState", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNoteTitle", "title", "app_debug"})
public final class NoteRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteDao noteDao = null;
    
    public NoteRepository(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteDao noteDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.data.NoteDao getNoteDao$app_debug() {
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
}