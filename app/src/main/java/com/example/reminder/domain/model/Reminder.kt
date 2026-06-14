package com.example.reminder.domain.model

/**
 * Доменная модель напоминания (чистый Kotlin, без Android/Room аннотаций).
 *
 * Используется во ViewModel, UI и Repository после маппинга из Entity/DTO.
 */
data class Reminder(
    val id: Int,
    val text: String,
    /** Формат dd.MM.yyyy */
    val date: String,
    /** Формат HH:mm */
    val time: String,
    /** Связь с товаром из e-commerce каталога, если напоминание о покупке. */
    val productId: Int? = null,
)
