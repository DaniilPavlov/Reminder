package com.example.reminder.presentation.reminder

import com.example.reminder.domain.model.Reminder
import com.example.reminder.domain.model.SduiBlock

/**
 * Снимок состояния экрана напоминаний (MVVM UiState).
 *
 * ViewModel хранит один объект UiState; UI подписывается и перерисовывается при изменениях.
 * Так проще, чем десяток отдельных LiveData/StateFlow.
 */
data class RemindersUiState(
    /** Список напоминаний из Room (через Repository). */
    val reminders: List<Reminder> = emptyList(),
    /** Текст в поле формы. */
    val text: String = "",
    val date: String = "",
    val time: String = "",
    /** ID товара из каталога, если напоминание привязано к покупке. */
    val selectedProductId: Int? = null,
    /** Баннер Server-Driven UI с сервера (Firebase Remote Config). */
    val sduiBanner: SduiBlock? = null,
    /** true пока идёт syncFromRemote(). */
    val isSyncing: Boolean = false,
    /** Одноразовое сообщение для Toast (показали — обнулили через consumeMessage). */
    val message: String? = null,
)
