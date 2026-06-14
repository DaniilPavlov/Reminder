package com.example.reminder.di

import android.content.Context
import androidx.room.Room
import com.example.reminder.BuildConfig
import com.example.reminder.data.local.AppDatabase
import com.example.reminder.data.local.ReminderDao
import com.example.reminder.data.remote.ProductApi
import com.example.reminder.data.remote.ReminderSyncApi
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt-модуль зависимостей (Dagger).
 *
 * @Module + @Provides — Hilt знает, как создавать Retrofit, Room, Firebase и т.д.
 * @InstallIn(SingletonComponent) — объекты живут всё время работы приложения.
 *
 * @Named — два разных Retrofit с разными baseUrl (FakeStore и JSONPlaceholder).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /** HTTP-клиент с логированием запросов в debug-сборке. */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** Retrofit для e-commerce API (FakeStore). */
    @Provides
    @Singleton
    @Named("fakeStoreRetrofit")
    fun provideFakeStoreRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FAKE_STORE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    /** Retrofit для demo sync (JSONPlaceholder). */
    @Provides
    @Singleton
    @Named("jsonPlaceholderRetrofit")
    fun provideJsonPlaceholderRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.JSON_PLACEHOLDER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Provides
    @Singleton
    fun provideProductApi(@Named("fakeStoreRetrofit") retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReminderSyncApi(@Named("jsonPlaceholderRetrofit") retrofit: Retrofit): ReminderSyncApi {
        return retrofit.create(ReminderSyncApi::class.java)
    }

    /** Локальная SQLite-база через Room. */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "reminders.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}
