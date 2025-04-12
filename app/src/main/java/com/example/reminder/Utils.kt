package com.example.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


class Utils {
    // Создаем companion object, чтобы потом вызывать функции в любой точки приложения
    companion object {
        // Генерация случайного id
        fun getID(): Int {
            return (Math.random() * 1000000).roundToInt()
        }

        // Добавляем 0 в строку, если число меньше 10
        fun addZero(count: Int): String {
            return if (count < 10) "0$count" else count.toString()
        }

        // Получаем текущую дату
        fun getCurrentDate(): String {
            val currentDate = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return formatter.format(currentDate)
        }

        // Определяем, устарело ли напоминание
        fun isReminderInPast(date: String, time: String): Boolean {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val reminderDateTime = LocalDateTime.parse("$date $time", formatter)
            val now = LocalDateTime.now(ZoneId.systemDefault())
            return reminderDateTime.isBefore(now)
        }

        // Получаем новый интент с id и текстом напоминания
        fun createPendingIntent(context: Context, id: Int, text: String): PendingIntent {
            // Создаем интент для ресивера уведомлений и передаем туда данные уведомления
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("text", text)
                putExtra("id", id)
            }
            // Возвращаем интент
            // Флаг *PendingIntent.FLAG_UPDATE_CURRENT* означает, что если такой интент уже существует,
            // то он будет обновлен данными из нового интента
            // Флаг *PendingIntent.FLAG_IMMUTABLE* означает, что интент будет неизменяемым (для андроид 12+)
            return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}