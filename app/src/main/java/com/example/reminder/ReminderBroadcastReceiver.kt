package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


// Класс для отправки уведомлений
// Его нужно зарегестрировать в AndroidManifest
// <receiver android:name=".ReminderBroadcastReceiver"/>
class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Проверяем, можно ли отправлять уведомление
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Если нельзя - не отправляем уведомление и заканчиваем выполнение метода
                return
            }
            // Получаем данные из интента
            val text = intent?.getStringExtra("text") ?: "Reminder Text"
            val id = intent?.getIntExtra("id", 0)
            // Подключаемся к менеджеру уведомлений, который создали в классе ReminderApplication
            val notificationManager = NotificationManagerCompat.from(context)
            val builder = NotificationCompat.Builder(context, ReminderApplication.CHANNEL_ID)
                // Устанавливаем иконку
                .setSmallIcon(R.drawable.ic_notification)
                // Устанавливаем заголовок
                .setContentTitle(context.resources.getString(R.string.new_reminder))
                // Устанавливаем текст из интента
                .setContentText(text)
                // Устанавливаем высокий приоритет
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Устанавливаем публичную видимость, чтобы уведомление было видно на экране блокировки
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Устанавливаем звук
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            // Отправляем уведомление. Требуется его id (который точно будет, поэтому *!!*) и экземпляр notification (builder.build())
            notificationManager.notify(id!!, builder.build())
        }
    }
}