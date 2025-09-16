package com.example.app.data

class ReminderRepository(private val reminderDao: ReminderDao) {
    fun getActiveReminders() = reminderDao.getActiveReminders()
    fun getAllReminders() = reminderDao.getAllReminders()
    suspend fun insert(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun update(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder)
    suspend fun markCompleted(id: Long) = reminderDao.markCompleted(id)
    suspend fun deleteById(id: Long) = reminderDao.deleteById(id)
    suspend fun reschedule(id: Long, time: Long) = reminderDao.reschedule(id, time)
}
