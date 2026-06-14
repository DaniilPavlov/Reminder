package com.example.reminder.presentation.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminder.R
import com.example.reminder.domain.model.SduiBlock
import com.example.reminder.ui.theme.AppDimens

/**
 * Server-Driven UI: промо-баннер из JSON (Firebase Remote Config).
 *
 * Сервер задаёт цвета — клиент рисует по [SduiBlock].
 * Тексты берутся из strings.xml, чтобы смена языка работала без перезагрузки Remote Config.
 */

@Composable
fun SduiBanner(
    block: SduiBlock,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AppDimens.sectionSpacing)
            .background(
                color = parseColor(block.backgroundColor),
                shape = RoundedCornerShape(AppDimens.cardCorner),
            )
            .padding(AppDimens.cardInner),
    ) {
        Text(
            text = stringResource(R.string.sdui_banner_title),
            color = parseColor(block.textColor),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.sdui_banner_subtitle),
            color = parseColor(block.textColor).copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 4.dp, bottom = AppDimens.fieldSpacing),
        )
        Button(
            onClick = onActionClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.fieldCorner),
            colors = ButtonDefaults.buttonColors(
                containerColor = parseColor(block.textColor),
                contentColor = parseColor(block.backgroundColor),
            ),
        ) {
            Text(text = stringResource(R.string.sdui_banner_action))
        }
    }
}

/** Парсит #RRGGBB из JSON; при ошибке — дефолтный тёмно-синий. */
private fun parseColor(value: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (_: IllegalArgumentException) {
        Color(0xFF1B2845)
    }
}
