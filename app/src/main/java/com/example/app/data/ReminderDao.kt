package com.example.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY reminderTime ASC")
    fun getActiveReminders(): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder)
    
    @Update
    suspend fun update(reminder: Reminder)
    
    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE reminders SET reminderTime = :time WHERE id = :id")
    suspend fun reschedule(id: Long, time: Long)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
