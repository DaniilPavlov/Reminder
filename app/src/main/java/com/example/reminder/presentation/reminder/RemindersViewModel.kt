package com.example.reminder.presentation.reminder

import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.reminder.analytics.AnalyticsTracker
import com.example.reminder.data.repository.ReminderRepository
import com.example.reminder.data.repository.SduiRepository
import com.example.reminder.domain.model.Product
import com.example.reminder.domain.model.Reminder
import com.example.reminder.util.AlarmScheduler
import com.example.reminder.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel экрана напоминаний — слой MVVM.
 *
 * Связывает UI с [ReminderRepository], планирует будильники через [AlarmScheduler],
 * логирует события в Firebase Analytics.
 *
 * RxJava: сеть/БД на IO-потоке, обновление UI на mainThread через observeOn.
 * CompositeDisposable отписывает все подписки в onCleared() — защита от утечек памяти.
 */
@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val sduiRepository: SduiRepository,
    private val alarmScheduler: AlarmScheduler,
    private val analyticsTracker: AnalyticsTracker,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    /** AlarmManager нельзя инжектить через Hilt — передаётся из Activity. */
    lateinit var alarmManager: AlarmManager

    init {
        observeReminders()
    }

    /** Пользователь меняет текст в форме. */
    fun onTextChanged(value: String) {
        _uiState.update { it.copy(text = value) }
    }

    fun onDateChanged(value: String) {
        _uiState.update { it.copy(date = value) }
    }

    fun onTimeChanged(value: String) {
        _uiState.update { it.copy(time = value) }
    }

    /** После выбора товара в каталоге (MVP) — подставляем текст в форму. */
    fun applyProductSelection(product: Product) {
        _uiState.update {
            it.copy(
                text = appContext.getString(
                    com.example.reminder.R.string.buy_product_format,
                    product.title,
                ),
                selectedProductId = product.id,
            )
        }
    }

    /**
     * Создаёт напоминание: валидация → Room → AlarmManager → Analytics.
     */
    fun addReminder(context: Context) {
        val state = _uiState.value
        if (state.date.isEmpty() && state.time.isEmpty()) {
            publishMessage(context.getString(com.example.reminder.R.string.toast_datetime_error))
            return
        }
        if (state.text.isEmpty()) {
            publishMessage(context.getString(com.example.reminder.R.string.toast_text_error))
            return
        }

        var date = state.date
        var time = state.time
        if (date.isEmpty()) date = Utils.getCurrentDate()
        if (time.isEmpty()) time = "12:00"

        val reminder = Reminder(
            id = Utils.getID(),
            text = state.text,
            date = date,
            time = time,
            productId = state.selectedProductId,
        )

        disposables.add(
            reminderRepository.addReminder(reminder)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    alarmScheduler.scheduleExactAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        date = reminder.date,
                        time = reminder.time,
                        text = reminder.text,
                        id = reminder.id,
                    )
                    analyticsTracker.logReminderCreated(reminder.productId)
                    _uiState.update {
                        it.copy(
                            text = "",
                            date = "",
                            time = "",
                            selectedProductId = null,
                            message = context.getString(com.example.reminder.R.string.toast_reminder_created),
                        )
                    }
                }, {
                    publishMessage(it.localizedMessage ?: "Failed to save reminder")
                }),
        )
    }

    /** Удаляет из БД и отменяет запланированный будильник. */
    fun removeReminder(reminder: Reminder, context: Context) {
        disposables.add(
            reminderRepository.removeReminder(reminder)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    alarmScheduler.cancelAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        id = reminder.id,
                        text = reminder.text,
                    )
                    publishMessage(context.getString(com.example.reminder.R.string.toast_reminder_removed))
                }, {
                    publishMessage(it.localizedMessage ?: "Failed to remove reminder")
                }),
        )
    }

    /** Импорт с REST API в Room + постановка будильников для каждого напоминания. */
    fun syncFromRemote(context: Context) {
        _uiState.update { it.copy(isSyncing = true) }
        disposables.add(
            reminderRepository.syncFromRemote()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ importedReminders ->
                    importedReminders.forEach { reminder ->
                        alarmScheduler.scheduleExactAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            date = reminder.date,
                            time = reminder.time,
                            text = reminder.text,
                            id = reminder.id,
                        )
                    }
                    analyticsTracker.logRemoteSync()
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            message = appContext.getString(com.example.reminder.R.string.toast_sync_success),
                        )
                    }
                }, {
                    _uiState.update { state ->
                        state.copy(
                            isSyncing = false,
                            message = it.localizedMessage ?: appContext.getString(com.example.reminder.R.string.toast_sync_error),
                        )
                    }
                }),
        )
    }

    /** Сбрасывает message после показа Toast в UI. */
    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Подписка на Room: при изменении таблицы список обновляется автоматически. */
    private fun observeReminders() {
        disposables.add(
            reminderRepository.observeReminders()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ reminders ->
                    _uiState.update { it.copy(reminders = reminders) }
                }, {
                    publishMessage(it.localizedMessage ?: "Failed to load reminders")
                }),
        )
    }

    /** Перезагружает SDUI-баннер после смены языка (ViewModel переживает recreate). */
    fun reloadSduiBanner() {
        loadSduiBanner()
    }

    /** Загружает JSON баннера из Firebase Remote Config. */
    private fun loadSduiBanner() {
        disposables.add(
            sduiRepository.loadHomeBanner()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ banner ->
                    _uiState.update { it.copy(sduiBanner = banner) }
                }, {
                    // При ошибке repository вернёт defaultBanner()
                }),
        )
    }

    private fun publishMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
