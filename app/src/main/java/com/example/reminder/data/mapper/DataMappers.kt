package com.example.reminder.data.mapper

import com.example.reminder.data.local.ReminderEntity
import com.example.reminder.data.remote.dto.ProductDto
import com.example.reminder.data.remote.dto.RemoteReminderDto
import com.example.reminder.data.remote.dto.SduiBlockDto
import com.example.reminder.domain.model.Product
import com.example.reminder.domain.model.Reminder
import com.example.reminder.domain.model.SduiBlock
import com.example.reminder.util.Utils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

/**
 * Преобразование между слоями data ↔ domain.
 *
 * Extension-функции: entity.toDomain(), dto.toDomain() и т.д.
 * Repository не должен знать детали JSON или Room — только mapper.
 */
object DataMappers {

    fun ReminderEntity.toDomain(): Reminder = Reminder(
        id = id,
        text = text,
        date = date,
        time = time,
        productId = productId,
    )

    fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
        id = id,
        text = text,
        date = date,
        time = time,
        productId = productId,
    )

    fun ProductDto.toDomain(): Product = Product(
        id = id,
        title = title,
        price = price,
        category = category,
        imageUrl = image,
    )

    /**
     * Удалённый «пост» → локальное напоминание.
     * @param minutesFromNow 0 = сейчас, 1 = через минуту, 2 = через 2 минуты и т.д.
     */
    fun RemoteReminderDto.toLocalReminder(minutesFromNow: Int = 0): Reminder {
        val dateTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(minutesFromNow.toLong())
        return Reminder(
            id = Utils.getID(),
            text = title,
            date = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            productId = null,
        )
    }

    fun SduiBlockDto.toDomain(): SduiBlock = SduiBlock(
        type = type,
        title = title,
        subtitle = subtitle,
        backgroundColor = backgroundColor,
        textColor = textColor,
        actionLabel = actionLabel,
    )
}
