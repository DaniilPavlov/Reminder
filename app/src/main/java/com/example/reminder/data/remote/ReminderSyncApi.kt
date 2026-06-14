package com.example.reminder.data.remote

import com.example.reminder.data.remote.dto.RemoteReminderDto
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

/**
 * Retrofit-интерфейс для demo-синхронизации напоминаний.
 *
 * JSONPlaceholder /posts — учебный REST API без авторизации.
 * В реальном проекте здесь были бы ваши CRUD-эндпоинты напоминаний.
 */
interface ReminderSyncApi {

    @GET("posts")
    fun getRemoteReminders(): Observable<List<RemoteReminderDto>>
}
