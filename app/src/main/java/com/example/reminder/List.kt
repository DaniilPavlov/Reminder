package com.example.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.reminder.domain.model.Reminder
import com.example.reminder.presentation.reminder.RemindersViewModel
import com.example.reminder.ui.theme.AppDimens

/**
 * Вкладка «Reminders»: форма + список в одном скролле.
 *
 * Раньше Form и List были отдельными блоками в Column — нижние кнопки
 * обрезались, а список перекрывал форму. Теперь всё в LazyColumn.
 */

@Composable
fun RemindersTabContent(
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppDimens.screenHorizontal,
            end = AppDimens.screenHorizontal,
            bottom = AppDimens.sectionSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimens.fieldSpacing),
    ) {
        item(key = "form") {
            Form(viewModel)
        }
        items(uiState.reminders, key = { it.id }) { reminder ->
            ReminderItem(reminder = reminder) {
                reminderToDelete = reminder
            }
        }
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = {
                Text(stringResource(R.string.delete_confirm_message, reminder.text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeReminder(reminder, context)
                        reminderToDelete = null
                    },
                ) {
                    Text(stringResource(R.string.delete_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text(stringResource(R.string.delete_confirm_no))
                }
            },
        )
    }
}
