package com.example.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import com.jakewharton.threetenabp.AndroidThreeTen

// Класс содержит 2 функциональности:
class ReminderApplication : Application() {
    // Инициализация констант для канала уведомлений
    companion object {
        const val CHANNEL_NAME = "Reminders"
        const val CHANNEL_DESCRIPTION = "Channel for reminders"
        const val CHANNEL_ID = "reminders"
    }

    override fun onCreate() {
        super.onCreate()
        // 1) Инициализация библиотеки для работы с датой и временем
        AndroidThreeTen.init(this)
        // 2) Создание канала уведомлений (создается только если версия больше 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                // Уровень важности канала
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                // Звук уведомления
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                )
            }
            // Получаем сервис уведомлений
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Создаем канал
            notificationManager.createNotificationChannel(channel)
        }
    }
}