package com.example.reminder.domain.model

/**
 * Товар из e-commerce API (FakeStore).
 * Domain-слой не знает про JSON-поля вроде "image" — маппинг в DataMappers.
 */
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
)
