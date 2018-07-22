package io.androidovshchik.weather24h;

import android.app.Application;

import com.github.androidovshchik.data.Preferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class MainApplication extends Application {

    public static OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.d(new Preferences(getApplicationContext()).getAllSorted().toString());
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
            Timber.tag("OkHttp").d(message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build();
    }
}
