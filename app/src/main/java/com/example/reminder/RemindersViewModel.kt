package com.example.reminder

import android.app.AlarmManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class RemindersViewModel : ViewModel() {
    lateinit var dbHelper: DatabaseHelper
    lateinit var alarmManager: AlarmManager

    var text by mutableStateOf("")
    var date by mutableStateOf("")
    var time by mutableStateOf("")

    // Список можно менять только в этом классе
    var reminders = mutableStateListOf<Reminder>()
        private set

    // Метод добавления уведомления
    fun addReminder(context: Context) {
        // Выводим ошибку, если что-либо не выбрано
        if (date.isEmpty() && time.isEmpty()) {
            return Toast.makeText(context, R.string.toast_datetime_error, Toast.LENGTH_LONG).show()
        } else if (text.isEmpty()) {
            return Toast.makeText(context, R.string.toast_text_error, Toast.LENGTH_LONG).show()
        }
        // При отсутствующих значениях задаем им дефолтные
        if (date.isEmpty()) date = Utils.getCurrentDate()
        if (time.isEmpty()) time = "12:00"
        // Добавляем reminder в лист и базу данных
        val reminder = Reminder(Utils.getID(), text, date, time)
        reminders.add(reminder)
        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_NAME, null, ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, reminder.id)
            put(DatabaseHelper.COLUMN_TEXT, reminder.text)
            put(DatabaseHelper.COLUMN_DATE, reminder.date)
            put(DatabaseHelper.COLUMN_TIME, reminder.time)
        })
        // Очищаем поля ввода
        text = ""
        date = ""
        time = ""
        // Оповещаем пользователя о создании напоминания
        scheduleNotification(context, reminder.date, reminder.time, reminder.text, reminder.id)
        sortReminders()
        Toast.makeText(context, R.string.toast_reminder_created, Toast.LENGTH_LONG).show()
    }

    // Метод удаления reminder из всех служб
    fun removeReminder(reminder: Reminder, context: Context) {
        reminders.remove(reminder)
        dbHelper.writableDatabase?.delete(
            DatabaseHelper.TABLE_NAME,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(reminder.id.toString())
        )
        alarmManager.cancel(Utils.createPendingIntent(context, reminder.id, reminder.text))
        Toast.makeText(context, R.string.toast_reminder_removed, Toast.LENGTH_LONG).show()
    }

    // Метод получения reminders из базы данных
    fun getReminders(context: Context) {
        reminders.clear()
        // Cоздаем курсор, с помощью которого можно взаимодействовать с записями таблицы
        // Нужно получить все записи таблицы, поэтому везде передаем null
        val cursor = dbHelper.readableDatabase?.query(
            DatabaseHelper.TABLE_NAME, null, null, null, null, null, null
        )
        if (cursor?.moveToFirst() == true) {
            do {
                val id = getIntValueFromColumn(cursor, DatabaseHelper.COLUMN_ID)
                val text = getStringValueFromColumn(cursor, DatabaseHelper.COLUMN_TEXT)
                val date = getStringValueFromColumn(cursor, DatabaseHelper.COLUMN_DATE)
                val time = getStringValueFromColumn(cursor, DatabaseHelper.COLUMN_TIME)

                // Формируем reminder из база данных. Если он в прошлом - удаляем его
                val reminder = Reminder(id, text, date, time)
                if (Utils.isReminderInPast(date, time)) {
                    removeReminder(reminder, context)
                } else {
                    reminders.add(reminder)
                }
            } while (cursor.moveToNext())
        }
        cursor?.close()
        sortReminders()
    }

    fun getIntValueFromColumn(cursor: Cursor, columnName: String): Int {
        val index = cursor.getColumnIndex(columnName)
        return if (index != -1) cursor.getInt(index) else 0
    }

    fun getStringValueFromColumn(cursor: Cursor, columnName: String): String {
        val index = cursor.getColumnIndex(columnName)
        return if (index != -1) cursor.getString(index) else ""
    }

    // Метод сортировки reminders по дате и времени
    fun sortReminders() {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        reminders.sortWith(compareBy { reminder ->
            LocalDateTime.parse("${reminder.date} ${reminder.time}", formatter)
        })
    }

    // Метод планирования уведомления
    fun scheduleNotification(
        context: Context,
        date: String,
        time: String,
        text: String,
        id: Int,
    ) {

        val dateTime = "$date $time"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        // Получаем время отправки
        val triggerTime = sdf.parse(dateTime)?.time ?: return
        // Создаем интент для отправки
        val pendingIntent = Utils.createPendingIntent(context, id, text)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            // Уведомление разбудит устройство, если оно будет в спящем режиме
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}