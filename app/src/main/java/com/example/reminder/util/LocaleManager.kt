package com.example.reminder.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Управление языком приложения (EN / RU).
 * Выбранный язык сохраняется в SharedPreferences и применяется через attachBaseContext.
 */
object LocaleManager {
    const val LANGUAGE_EN = "en"
    const val LANGUAGE_RU = "ru"

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, LANGUAGE_EN) ?: LANGUAGE_EN
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /** Переключает en ↔ ru и возвращает новый код языка. */
    fun toggleLanguage(context: Context): String {
        val nextLanguage = if (getLanguage(context) == LANGUAGE_RU) LANGUAGE_EN else LANGUAGE_RU
        setLanguage(context, nextLanguage)
        return nextLanguage
    }

    fun isRussian(context: Context): Boolean = getLanguage(context) == LANGUAGE_RU

    fun wrap(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    /**
     * Обновляет locale у Application-ресурсов (strings.xml, remote_config_defaults).
     * Нужно вызывать при смене языка — иначе Singleton-сервисы останутся на старом языке.
     */
    fun applyAppLocale(context: Context) {
        val locale = Locale(getLanguage(context))
        Locale.setDefault(locale)
        val resources = context.applicationContext.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
