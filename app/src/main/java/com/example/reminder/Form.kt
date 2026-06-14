package com.example.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminder.presentation.reminder.RemindersViewModel
import com.example.reminder.ui.theme.AppDimens
import com.example.reminder.util.Utils

/**
 * UI формы создания напоминания (Jetpack Compose).
 * Отображение только — бизнес-логика в [RemindersViewModel].
 * Отступы и размеры — через [AppDimens] для единообразия.
 */
private val formFieldShape = RoundedCornerShape(AppDimens.fieldCorner)

/** Карточка формы: текст, дата, время, «Создать», «Sync». */
@Composable
fun Form(viewModel: RemindersViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.dark_navy), RoundedCornerShape(AppDimens.cardCorner))
            .border(0.5.dp, colorResource(id = R.color.blue), RoundedCornerShape(AppDimens.cardCorner))
            .padding(AppDimens.cardInner),
        verticalArrangement = Arrangement.spacedBy(AppDimens.fieldSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.form_title),
            style = TextStyle(
                color = colorResource(id = R.color.white),
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Thin,
            ),
        )
        ReminderTextField(
            value = uiState.text,
            onValueChange = viewModel::onTextChanged,
        )
        DateTimeInputFields(
            date = uiState.date,
            time = uiState.time,
            onDateChanged = viewModel::onDateChanged,
            onTimeChanged = viewModel::onTimeChanged,
        )
        CreateButton {
            viewModel.addReminder(context)
        }
        OutlinedButton(
            onClick = { viewModel.syncFromRemote(context) },
            enabled = !uiState.isSyncing,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.buttonHeight),
            shape = formFieldShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(id = R.color.white),
            ),
            border = BorderStroke(1.dp, colorResource(id = R.color.blue)),
        ) {
            Text(
                text = if (uiState.isSyncing) {
                    stringResource(R.string.form_syncing)
                } else {
                    stringResource(R.string.form_sync)
                },
            )
        }
    }
}

/** Поле ввода текста напоминания. */
@Composable
fun ReminderTextField(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(id = R.string.form_text_hint),
                style = LocalTextStyle.current.copy(color = colorResource(id = R.color.blue)),
            )
        },
        colors = formTextFieldColors(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppDimens.fieldHeight),
        shape = formFieldShape,
    )
}

/** Блок полей даты и времени с одинаковой шириной. */
@Composable
fun DateTimeInputFields(
    date: String,
    time: String,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.fieldSpacing),
    ) {
        DateInputField(date = date, onDateChanged = onDateChanged)
        TimeInputField(time = time, onTimeChanged = onTimeChanged)
    }
}

/** Поле даты — по клику открывает DatePickerDialog (формат dd.MM.yyyy). */
@Composable
fun DateInputField(date: String, onDateChanged: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    PickerInputField(
        value = date,
        hint = stringResource(id = R.string.form_date_hint),
        onClick = {
            DatePickerDialog(
                context,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    onDateChanged(
                        "${Utils.addZero(selectedDay)}.${Utils.addZero(selectedMonth + 1)}.$selectedYear",
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            ).show()
        },
    )
}

/** Поле времени — по клику открывает TimePickerDialog (формат HH:mm). */
@Composable
fun TimeInputField(time: String, onTimeChanged: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    PickerInputField(
        value = time,
        hint = stringResource(id = R.string.form_time_hint),
        onClick = {
            TimePickerDialog(
                context,
                { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    onTimeChanged("${Utils.addZero(selectedHour)}:${Utils.addZero(selectedMinute)}")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            ).show()
        },
    )
}

/** Унифицированное поле-«кнопка» для выбора даты или времени. */
@Composable
private fun PickerInputField(
    value: String,
    hint: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.fieldHeight)
            .background(colorResource(id = R.color.navy), formFieldShape)
            .border(0.5.dp, colorResource(id = R.color.blue), formFieldShape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppDimens.cardInner),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = if (value.isNotEmpty()) value else hint,
            color = colorResource(id = R.color.blue),
        )
    }
}

/** Кнопка «Создать» с градиентом; скрывает клавиатуру после нажатия. */
@Composable
fun CreateButton(onClick: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimens.buttonHeight)
            .clip(RoundedCornerShape(AppDimens.cardCorner))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.button_gradient_purple),
                        colorResource(id = R.color.button_gradient_blue),
                    ),
                ),
            )
            .clickable {
                onClick()
                keyboardController?.hide()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.form_create),
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
        )
    }
}

/** Общие цвета TextField в форме. */
@Composable
private fun formTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    cursorColor = colorResource(id = R.color.blue),
    focusedTextColor = colorResource(id = R.color.blue),
    unfocusedTextColor = colorResource(id = R.color.blue),
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = colorResource(id = R.color.blue),
    unfocusedPlaceholderColor = colorResource(id = R.color.blue),
)
