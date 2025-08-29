package com.example.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isDone = 0 ORDER BY time ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("UPDATE reminders SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)

    @Query("UPDATE reminders SET time = :time WHERE id = :id")
    suspend fun reschedule(id: Long, time: Long)
}
