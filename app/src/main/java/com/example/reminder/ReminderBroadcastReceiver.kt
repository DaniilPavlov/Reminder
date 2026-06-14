package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * BroadcastReceiver — компонент, который «просыпается» по системному событию.
 *
 * Здесь: AlarmManager в нужное время шлёт Intent сюда, и мы показываем уведомление.
 * Зарегистрирован в AndroidManifest: <receiver android:name=".ReminderBroadcastReceiver"/>
 *
 * Цепочка: ViewModel → AlarmScheduler → PendingIntent → onReceive() → Notification.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {

    /**
     * Вызывается системой, когда срабатывает будильник.
     * @param context контекст приложения
     * @param intent содержит extras "text" и "id" напоминания
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Без разрешения на Android 13+ notify() упадёт или не покажет push
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val text = intent?.getStringExtra("text") ?: "Reminder Text"
            val id = intent?.getIntExtra("id", 1)

            val notificationManager = NotificationManagerCompat.from(context)
            val builder = NotificationCompat.Builder(context, ReminderApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.resources.getString(R.string.new_reminder))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            notificationManager.notify(id!!, builder.build())
        }
    }
}
