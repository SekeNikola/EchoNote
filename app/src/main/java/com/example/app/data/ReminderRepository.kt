package com.example.app.data

class ReminderRepository(private val reminderDao: ReminderDao) {
    fun getActiveReminders() = reminderDao.getActiveReminders()
    suspend fun insert(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun markDone(id: Long) = reminderDao.markDone(id)
    suspend fun reschedule(id: Long, time: Long) = reminderDao.reschedule(id, time)
}
