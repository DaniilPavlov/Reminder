package com.example.reminder.data.repository

import android.content.Context
import com.example.reminder.BuildConfig
import com.example.reminder.R
import com.example.reminder.data.mapper.DataMappers.toDomain
import com.example.reminder.data.remote.dto.SduiBlockDto
import com.example.reminder.domain.model.SduiBlock
import com.example.reminder.util.LocaleManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Server-Driven UI через Firebase Remote Config.
 *
 * Defaults берутся из remote_config_defaults.xml (values / values-ru).
 * При смене языка defaults перечитываются с актуальной locale.
 */
@Singleton
class SduiRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson,
) {
    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build(),
        )
    }

    fun loadHomeBanner(): Single<SduiBlock> {
        return Single.fromCallable {
            val localizedContext = LocaleManager.wrap(appContext)
            applyLocalizedDefaults()

            try {
                Tasks.await(remoteConfig.fetchAndActivate(), 10, TimeUnit.SECONDS)
                val json = remoteConfig.getString(HOME_BANNER_KEY)
                if (json.isNotBlank()) {
                    withLocalizedTexts(
                        gson.fromJson(json, SduiBlockDto::class.java).toDomain(),
                        localizedContext,
                    )
                } else {
                    buildLocalizedBanner(localizedContext)
                }
            } catch (_: Exception) {
                buildLocalizedBanner(localizedContext)
            }
        }
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                buildLocalizedBanner(LocaleManager.wrap(appContext))
            }
    }

    /** Тексты всегда из strings.xml — Remote Config задаёт только стиль (цвета). */
    private fun withLocalizedTexts(block: SduiBlock, context: Context): SduiBlock = block.copy(
        title = context.getString(R.string.sdui_banner_title),
        subtitle = context.getString(R.string.sdui_banner_subtitle),
        actionLabel = context.getString(R.string.sdui_banner_action),
    )

    /** Сбрасывает кэш Remote Config и подставляет defaults для текущего языка. */
    private fun applyLocalizedDefaults() {
        Tasks.await(remoteConfig.reset(), 5, TimeUnit.SECONDS)
        Tasks.await(remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults), 5, TimeUnit.SECONDS)
    }

    /** Fallback из strings.xml — всегда совпадает с выбранным языком. */
    private fun buildLocalizedBanner(context: Context): SduiBlock = SduiBlock(
        type = "banner",
        title = context.getString(R.string.sdui_banner_title),
        subtitle = context.getString(R.string.sdui_banner_subtitle),
        backgroundColor = "#1B2845",
        textColor = "#FFFFFF",
        actionLabel = context.getString(R.string.sdui_banner_action),
    )

    companion object {
        const val HOME_BANNER_KEY = "home_banner"
    }
}
