package com.example.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundNotificationService : Service() {

    private fun createPersistentNotification(): Notification {
        val channelId = "persistent_notification_channel"

        // Создаём канал
        val channel = NotificationChannel(
            channelId,
            "Persistent Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "живет в бекграунде"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Строим уведомление
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Тест")
            .setContentText("Проверка работоспособности")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val notification = createPersistentNotification()
        startForeground(10000, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}