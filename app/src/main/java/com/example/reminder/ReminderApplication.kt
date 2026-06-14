package com.example.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.provider.Settings
import com.example.reminder.util.LocaleManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

/**
 * Класс Application — запускается один раз при старте приложения (раньше Activity).
 *
 * Отвечает за глобальную инициализацию:
 * - Hilt (DI-контейнер);
 * - библиотеку даты/времени ThreeTenABP;
 * - канал уведомлений Android 8+;
 * - локализацию через [LocaleManager.wrap].
 *
 * Указан в AndroidManifest: android:name=".ReminderApplication"
 */
@HiltAndroidApp
class ReminderApplication : Application() {

    /** Константы канала уведомлений — используются в ReminderBroadcastReceiver. */
    companion object {
        const val CHANNEL_NAME = "Reminders"
        const val CHANNEL_DESCRIPTION = "Channel for reminders"
        const val CHANNEL_ID = "reminders"
    }

    /** Применяет сохранённый язык ко всему приложению при старте. */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        LocaleManager.applyAppLocale(this)
        AndroidThreeTen.init(this) // Java 8 Time API на старых Android
        createNotificationChannel()
    }

    /**
     * С Android 8 (Oreo) все уведомления должны идти через NotificationChannel.
     * Без канала система не покажет push.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = CHANNEL_DESCRIPTION
            setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build(),
            )
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
