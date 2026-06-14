package com.example.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

/**
 * DAO (Data Access Object) — интерфейс SQL-запросов к таблице reminders.
 *
 * Room генерирует реализацию на этапе компиляции (KSP).
 * RxJava-типы: Observable для чтения, Completable для записи/удаления.
 */
@Dao
interface ReminderDao {

    /** Реактивный поток: при любом изменении таблицы эмитит новый список. */
    @Query("SELECT * FROM reminders ORDER BY date ASC, time ASC")
    fun observeReminders(): Observable<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY date ASC, time ASC")
    fun getReminders(): Observable<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(reminder: ReminderEntity): Completable

    @Query("DELETE FROM reminders WHERE id = :id")
    fun deleteReminder(id: Int): Completable

    /** Пакетная вставка при import с REST API. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(reminders: List<ReminderEntity>): Completable
}
