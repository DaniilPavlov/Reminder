package com.example.reminder.util

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обёртка над AlarmManager — планирование и отмена точных будильников.
 *
 * Вынесено из ViewModel: ViewModel не должен знать детали Android Alarm API.
 */
@Singleton
class AlarmScheduler @Inject constructor() {

    /**
     * Ставит exact alarm на дату/время напоминания.
     * RTC_WAKEUP — разбудит устройство, если оно спит.
     */
    fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        date: String,
        time: String,
        text: String,
        id: Int,
    ) {
        val dateTime = "$date $time"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse(dateTime)?.time ?: return
        val pendingIntent = Utils.createPendingIntent(context, id, text)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /** Отменяет будильник с тем же id (PendingIntent должен совпадать). */
    fun cancelAlarm(context: Context, alarmManager: AlarmManager, id: Int, text: String) {
        alarmManager.cancel(Utils.createPendingIntent(context, id, text))
    }
}
