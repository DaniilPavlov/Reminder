package com.example.reminder.data.repository

import com.example.reminder.data.local.ReminderDao
import com.example.reminder.data.mapper.DataMappers.toDomain
import com.example.reminder.data.mapper.DataMappers.toEntity
import com.example.reminder.data.mapper.DataMappers.toLocalReminder
import com.example.reminder.data.remote.ReminderSyncApi
import com.example.reminder.domain.model.Reminder
import com.example.reminder.util.Utils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository напоминаний — единая точка доступа к данным (local + remote).
 *
 * Паттерн Repository скрывает от ViewModel, откуда данные: Room или Retrofit.
 * RxJava operators: map, filterNot, flatMapCompletable, take.
 */
@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderSyncApi: ReminderSyncApi,
) {

    /**
     * Поток списка напоминаний из Room.
     * Фильтруем прошедшие, сортируем по дате/времени.
     */
    fun observeReminders(): Observable<List<Reminder>> {
        return reminderDao.observeReminders()
            .subscribeOn(Schedulers.io())
            .map { entities -> entities.map { it.toDomain() } }
            .map { reminders -> reminders.filterNot { Utils.isReminderInPast(it.date, it.time) } }
            .map { reminders -> sortReminders(reminders) }
    }

    fun addReminder(reminder: Reminder): Completable {
        return reminderDao.insertReminder(reminder.toEntity())
            .subscribeOn(Schedulers.io())
    }

    fun removeReminder(reminder: Reminder): Completable {
        return reminderDao.deleteReminder(reminder.id)
            .subscribeOn(Schedulers.io())
    }

    /**
     * Demo sync: GET /posts → берём 5 записей → сохраняем в Room.
     * Время: сейчас, +1 мин, +2 мин, +3 мин, +4 мин.
     * @return список импортированных напоминаний (для постановки AlarmManager).
     */
    fun syncFromRemote(): Single<List<Reminder>> {
        return reminderSyncApi.getRemoteReminders()
            .subscribeOn(Schedulers.io())
            .firstOrError()
            .map { remoteItems ->
                remoteItems
                    .take(5)
                    .mapIndexed { index, item -> item.toLocalReminder(minutesFromNow = index) }
            }
            .flatMap { reminders ->
                if (reminders.isEmpty()) {
                    Single.just(emptyList())
                } else {
                    reminderDao.insertAll(reminders.map { it.toEntity() })
                        .toSingleDefault(reminders)
                }
            }
    }

    private fun sortReminders(reminders: List<Reminder>): List<Reminder> {
        return reminders.sortedWith(
            compareBy { reminder ->
                org.threeten.bp.LocalDateTime.parse(
                    "${reminder.date} ${reminder.time}",
                    org.threeten.bp.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
                )
            },
        )
    }
}
