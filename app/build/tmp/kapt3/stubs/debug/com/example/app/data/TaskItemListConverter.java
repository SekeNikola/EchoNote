package com.example.app.data;

import androidx.room.TypeConverter;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0007J\u0016\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\u000b\u001a\u00020\u0006H\u0002J\u0016\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\r\u001a\u00020\u0006H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/example/app/data/TaskItemListConverter;", "", "()V", "json", "Lkotlinx/serialization/json/Json;", "fromCheckboxItemList", "", "items", "", "Lcom/example/app/data/CheckboxItem;", "parseCheckboxesFromText", "text", "toCheckboxItemList", "itemsString", "app_debug"})
public final class TaskItemListConverter {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    
    public TaskItemListConverter() {
        super();
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fromCheckboxItemList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.CheckboxItem> items) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.app.data.CheckboxItem> toCheckboxItemList(@org.jetbrains.annotations.NotNull()
    java.lang.String itemsString) {
        return null;
    }
    
    private final java.util.List<com.example.app.data.CheckboxItem> parseCheckboxesFromText(java.lang.String text) {
        return null;
    }
}