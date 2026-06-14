package com.example.reminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Таблица Room — как строка в SQLite на диске телефона.
 * Entity = схема БД; domain [Reminder] = модель для UI/логики.
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val date: String,
    val time: String,
    val productId: Int? = null,
)
