package com.example.reminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Главный класс базы данных Room (Architecture Components).
 *
 * version = 2: при изменении схемы нужна миграция или fallbackToDestructiveMigration (как в AppModule).
 */
@Database(
    entities = [ReminderEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
