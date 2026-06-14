package com.example.reminder.domain.model

/**
 * Блок UI, приходящий с сервера (Server-Driven UI).
 *
 * Описывает, как нарисовать промо-баннер: тип, тексты, цвета, текст кнопки.
 */
data class SduiBlock(
    val type: String,
    val title: String,
    val subtitle: String,
    val backgroundColor: String,
    val textColor: String,
    val actionLabel: String,
)
