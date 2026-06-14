package com.example.reminder.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.reminder.ReminderBroadcastReceiver
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Вспомогательные функции без привязки к UI.
 * object = singleton на уровне Kotlin (не нужен Dagger).
 */
object Utils {

    /** Случайный id для нового напоминания (в проде лучше UUID или auto-increment Room). */
    fun getID(): Int = (Math.random() * 1_000_000).roundToInt()

    /** "7" → "07" для формата даты/времени. */
    fun addZero(count: Int): String = if (count < 10) "0$count" else count.toString()

    /** Текущая дата в формате dd.MM.yyyy. */
    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    /** true, если дата+время напоминания уже в прошлом. */
    fun isReminderInPast(date: String, time: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val reminderDateTime = LocalDateTime.parse("$date $time", formatter)
        val now = LocalDateTime.now(ZoneId.systemDefault())
        return reminderDateTime.isBefore(now)
    }

    /**
     * PendingIntent — «отложенный» Intent для AlarmManager.
     * При срабатывании будильника система вызовет ReminderBroadcastReceiver.
     */
    fun createPendingIntent(context: Context, id: Int, text: String): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("text", text)
            putExtra("id", id)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
