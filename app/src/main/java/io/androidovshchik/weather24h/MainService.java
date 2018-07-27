package io.androidovshchik.weather24h;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import com.github.androidovshchik.BaseService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.androidovshchik.weather24h.current.Current;
import io.androidovshchik.weather24h.data.DbManager;
import io.androidovshchik.weather24h.forecast.Forecast;
import io.androidovshchik.weather24h.model.Data;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static io.androidovshchik.weather24h.ConstantsKt.HOUR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_CURRENT;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_CURRENT_URL;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_FORECAST_URL;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_LAST_CURRENT;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_LAST_FORECAST;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_FORECAST;

public class MainService extends BaseService {

    private WindowManager windowManager;

    private OverlayLayout overlay;

    private Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create();

    private DbManager dbManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("BinaryOperationInTimber")
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("> onReceive " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Timber.d(Intent.ACTION_SCREEN_OFF);
                Timber.d("> addOverlay 2");
                addOverlay();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("> onCreate");
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlay = (OverlayLayout) View.inflate(getApplicationContext(), R.layout.overlay, null);
        overlay.findViewById(R.id.close).setOnClickListener(view -> {
            if (overlay.getWindowToken() != null) {
                windowManager.removeView(overlay);
            }
        });
        dbManager = new DbManager(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);
        Timber.d("> startForeground");
        startForeground(1, "Фоновая работа приложения", R.drawable.ic_cloud_white_24dp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("> Observable.onSelectTable");
        getDisposable().add(dbManager.onSelectTable("SELECT * FROM data")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(cursor -> {
                try {
                    while (cursor.moveToNext()) {
                        Data item = new Data();
                        item.parseCursor(cursor);
                        overlay.getItems().add(item);
                    }
                } finally {
                    cursor.close();
                }
                if (preferences.has(PREFERENCE_CURRENT)) {
                    try {
                        Current current = gson.fromJson(preferences.getString(PREFERENCE_CURRENT),
                            Current.class);
                        overlay.bindTopPart(current);
                    } catch (Exception e) {
                        Timber.e(e);
                        preferences.remove(PREFERENCE_CURRENT);
                    }
                }
                if (preferences.has(PREFERENCE_FORECAST)) {
                    try {
                        Forecast forecast = gson.fromJson(preferences.getString(PREFERENCE_FORECAST),
                            Forecast.class);
                        overlay.bindStripBottomPart(forecast);
                    } catch (Exception e) {
                        Timber.e(e);
                        preferences.remove(PREFERENCE_FORECAST);
                    }
                }
                Timber.d("> addOverlay 1");
                addOverlay();
                Timber.d("> Observable.interval");
                getDisposable().add(Observable.interval(0, 5, TimeUnit.MINUTES)
                    .subscribeOn(Schedulers.io())
                    .subscribe(value -> {
                        long now = System.currentTimeMillis();
                        if (now - preferences.getLong(PREFERENCE_LAST_CURRENT, 0) >= HOUR) {
                            syncCurrent();
                        }
                        if (now - preferences.getLong(PREFERENCE_LAST_FORECAST, 0) >= HOUR) {
                            syncForecast();
                        }
                    }));
            }));
        return super.onStartCommand(intent, flags, startId);
    }

    private void syncCurrent() {
        Timber.d("> syncCurrent");
        Request request = new Request.Builder()
                .url(preferences.getString(PREFERENCE_CURRENT_URL))
                .build();
        MainApplication.client.newCall(request).enqueue(new Callback() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                Timber.d("> onResponse syncCurrent");
                try {
                    String json = response.body().string();
                    Current current = gson.fromJson(json, Current.class);
                    preferences.putString(PREFERENCE_CURRENT, json);
                    preferences.putLong(PREFERENCE_LAST_CURRENT, System.currentTimeMillis());
                    getDisposable().add(Observable.just(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(value -> overlay.bindTopPart(current)));
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Timber.e(e);
            }
        });
    }

    private void syncForecast() {
        Timber.d("> syncForecast");
        Request request = new Request.Builder()
            .url(preferences.getString(PREFERENCE_FORECAST_URL))
            .build();
        MainApplication.client.newCall(request).enqueue(new Callback() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                Timber.d("> onResponse syncForecast");
                try {
                    String json = response.body().string();
                    Forecast forecast = gson.fromJson(json, Forecast.class);
                    preferences.putString(PREFERENCE_FORECAST, json);
                    preferences.putLong(PREFERENCE_LAST_FORECAST, System.currentTimeMillis());
                    getDisposable().add(Observable.just(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(value -> overlay.bindStripBottomPart(forecast)));
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Timber.e(e);
            }
        });
    }

    private void addOverlay() {
        Timber.d("> addOverlay");
        if (overlay.getWindowToken() != null) {
            return;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        } else {
            params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        params.format = PixelFormat.TRANSLUCENT;
        windowManager.addView(overlay, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("> onDestroy");
        unregisterReceiver(receiver);
        if (overlay.getWindowToken() != null) {
            Timber.d("> removeView");
            windowManager.removeView(overlay);
        }
    }
}