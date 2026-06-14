package com.example.reminder

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.reminder.domain.model.Reminder
import com.example.reminder.ui.custom.ReminderDateTimeView
import com.example.reminder.ui.theme.AppDimens

/**
 * Одна строка списка напоминаний.
 *
 * Слева — текст, справа — Custom View с датой/обратным отсчётом и кнопка удаления.
 * AndroidView встраивает классический View в Compose (демо Custom View).
 */

@Composable
fun ReminderItem(reminder: Reminder, onDeleteClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(bottom = AppDimens.fieldSpacing)
            .background(colorResource(id = R.color.dark), RoundedCornerShape(25.dp))
            .border(0.5.dp, colorResource(id = R.color.blue), RoundedCornerShape(25.dp))
            .padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable {
                    Toast.makeText(context, reminder.text, Toast.LENGTH_LONG).show()
                },
        ) {
            Text(
                text = reminder.text,
                style = TextStyle(color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            factory = { ctx -> ReminderDateTimeView(ctx) },
            update = { view -> view.bind(reminder.date, reminder.time) },
        )
        IconButton(onClick = onDeleteClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = stringResource(R.string.delete_content_description),
                tint = colorResource(id = R.color.blue),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
