package com.example.reminder.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.reminder.R
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

/**
 * Custom View — классический Android View с отрисовкой через Canvas.
 *
 * Демонстрирует навык «Custom views creation»:
 * не Compose, а наследник View + onDraw().
 * В списке встраивается через AndroidView в Compose.
 */
class ReminderDateTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var date: String = ""
    private var time: String = ""

    private val tickRunnable = Runnable {
        invalidate()
        scheduleTick()
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.blue)
        textSize = resources.displayMetrics.scaledDensity * 14f
    }

    private val countdownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = resources.displayMetrics.scaledDensity * 12f
    }

    /** Обновляет данные и просит систему перерисовать View (invalidate). */
    fun bind(date: String, time: String) {
        this.date = date
        this.time = time
        invalidate()
        scheduleTick()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scheduleTick()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    /** Перерисовка на границе минуты — «Через N мин» / «Уже пора» обновляются без скролла. */
    private fun scheduleTick() {
        removeCallbacks(tickRunnable)
        if (!isAttachedToWindow) return
        postDelayed(tickRunnable, millisUntilNextMinute())
    }

    private fun millisUntilNextMinute(): Long {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        return (60 - now.second) * 1_000L - now.nano / 1_000_000L + 1L
    }

    /** Рисуем две строки: дата/время и «осталось X мин/ч». */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText("$date  $time", paddingLeft.toFloat(), height * 0.45f, datePaint)
        canvas.drawText(buildCountdownLabel(), paddingLeft.toFloat(), height * 0.85f, countdownPaint)
    }

    /** Считает разницу между «сейчас» и временем напоминания. */
    private fun buildCountdownLabel(): String {
        if (date.isBlank() || time.isBlank()) {
            return context.getString(R.string.custom_view_no_date)
        }
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val target = LocalDateTime.parse("$date $time", formatter)
            val now = LocalDateTime.now(ZoneId.systemDefault())
            val minutes = ChronoUnit.MINUTES.between(now, target)
            when {
                minutes <= 0 -> context.getString(R.string.custom_view_due_now)
                minutes < 60 -> context.getString(R.string.custom_view_in_minutes, minutes)
                else -> context.getString(R.string.custom_view_in_hours, minutes / 60)
            }
        } catch (_: Exception) {
            context.getString(R.string.custom_view_no_date)
        }
    }
}
