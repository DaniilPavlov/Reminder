package com.example.reminder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun List(viewModel: RemindersViewModel = viewModel()) {
    val context = LocalContext.current

    // Вызываем получение напоминаний из базы данных при первой отрисовке листа
    LaunchedEffect(key1 = true) {
        viewModel.getReminders(context)
    }

    // Держит в памяти только те элементы, которые сейчас отображены на экране
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(viewModel.reminders) { _, reminder ->
            ReminderItem(reminder, viewModel)
        }
    }
}