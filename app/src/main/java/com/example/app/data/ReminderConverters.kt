package com.example.app.data

import androidx.room.TypeConverter

class ReminderConverters {
    @TypeConverter
    fun fromLong(value: Long?): String = value?.toString() ?: ""
    @TypeConverter
    fun toLong(value: String): Long? = value.toLongOrNull()
}
