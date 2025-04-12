package com.example.reminder

// Дата класс для напоминания для удобной работы со списком с такими же данными как в DatabaseHelper
data class Reminder(val id: Int, val text: String, val date: String, val time: String)
