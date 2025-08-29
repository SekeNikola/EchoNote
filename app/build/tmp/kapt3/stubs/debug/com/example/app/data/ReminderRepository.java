package com.example.app.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0012\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\r\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/example/app/data/ReminderRepository;", "", "reminderDao", "Lcom/example/app/data/ReminderDao;", "(Lcom/example/app/data/ReminderDao;)V", "getActiveReminders", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/app/data/Reminder;", "insert", "", "reminder", "(Lcom/example/app/data/Reminder;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markDone", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reschedule", "time", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ReminderRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.ReminderDao reminderDao = null;
    
    public ReminderRepository(@org.jetbrains.annotations.NotNull()
    com.example.app.data.ReminderDao reminderDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.app.data.Reminder>> getActiveReminders() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Reminder reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markDone(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object reschedule(long id, long time, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}