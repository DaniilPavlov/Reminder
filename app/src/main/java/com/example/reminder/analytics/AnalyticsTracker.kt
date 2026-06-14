package com.example.reminder.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обёртка над Firebase Analytics — централизованные имена событий.
 *
 * ViewModel/Presenter вызывают методы здесь, а не Firebase напрямую —
 * проще менять аналитику и писать тесты.
 */
@Singleton
class AnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) {

    /** Пользователь создал напоминание (опционально привязано к товару). */
    fun logReminderCreated(productId: Int?) {
        firebaseAnalytics.logEvent(
            EVENT_REMINDER_CREATED,
            Bundle().apply {
                putBoolean(PARAM_HAS_PRODUCT, productId != null)
                productId?.let { putInt(PARAM_PRODUCT_ID, it) }
            },
        )
    }

    /** Пользователь выбрал товар в каталоге. */
    fun logProductSelected(productId: Int, category: String) {
        firebaseAnalytics.logEvent(
            EVENT_PRODUCT_SELECTED,
            Bundle().apply {
                putInt(PARAM_PRODUCT_ID, productId)
                putString(PARAM_CATEGORY, category)
            },
        )
    }

    /** Нажата кнопка import с REST API. */
    fun logRemoteSync() {
        firebaseAnalytics.logEvent(EVENT_REMOTE_SYNC, null)
    }

    companion object {
        private const val EVENT_REMINDER_CREATED = "reminder_created"
        private const val EVENT_PRODUCT_SELECTED = "product_selected"
        private const val EVENT_REMOTE_SYNC = "remote_sync"
        private const val PARAM_HAS_PRODUCT = "has_product"
        private const val PARAM_PRODUCT_ID = "product_id"
        private const val PARAM_CATEGORY = "category"
    }
}
