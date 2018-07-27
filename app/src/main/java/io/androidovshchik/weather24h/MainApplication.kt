package io.androidovshchik.weather24h

import android.app.Application
import com.github.androidovshchik.BaseApplication
import com.github.androidovshchik.Environment

import com.github.androidovshchik.data.Preferences

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

class MainApplication : BaseApplication() {

    override val environment: Environment = Environment.SANDBOX

    override val theme = R.style.LibraryTheme_Dialog

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d(Preferences(applicationContext).getAllSorted().toString())
        }
        val logging = HttpLoggingInterceptor { message -> Timber.tag("OkHttp").d(message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    companion object {

        var client: OkHttpClient? = null
    }
}
