package com.example.reminder.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) — формат JSON с сервера.
 *
 * Отделён от domain-моделей: если API изменит поля, правим только DTO и mapper.
 */

/** JSON товара с FakeStore API. */
data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val category: String,
    val image: String,
)

/** JSON «поста» с JSONPlaceholder — используем как удалённое напоминание для demo sync. */
data class RemoteReminderDto(
    val id: Int,
    val title: String,
    val body: String,
)

/** JSON баннера из Firebase Remote Config (Server-Driven UI). */
data class SduiBlockDto(
    val type: String,
    val title: String,
    val subtitle: String,
    @SerializedName("backgroundColor") val backgroundColor: String,
    @SerializedName("textColor") val textColor: String,
    @SerializedName("actionLabel") val actionLabel: String,
)
