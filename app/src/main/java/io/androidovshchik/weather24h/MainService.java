package io.androidovshchik.weather24h;

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

import io.androidovshchik.weather24h.parser.SiteResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static io.androidovshchik.weather24h.ConstantsKt.HOUR;

public class MainService extends BaseService {

    private static final String API_KEY = "12345";

    private static final String WEATHER = "http://api.openweathermap.org/data/2.5/forecast?id=543460&appid=" + API_KEY;
    //543460

    private WindowManager windowManager;

    private OverlayLayout overlay;

    private Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create();

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlay = (OverlayLayout) View.inflate(getApplicationContext(), R.layout.overlay, null);
        overlay.findViewById(R.id.close).setOnClickListener(view -> {
            if (overlay.getWindowToken() != null) {
                windowManager.removeView(overlay);
            }
        });
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Timber.d(Intent.ACTION_SCREEN_OFF);
                    addOverlay();
                }
            }
        }, intentFilter);
        startForeground(1, "Фоновая работа приложения", R.drawable.ic_cloud_white_24dp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (preferences.has(ConstantsKt.PREFERENCE_WEATHER)) {
            try {
                SiteResponse siteResponse = gson.fromJson(preferences.getString(ConstantsKt.PREFERENCE_WEATHER),
                    SiteResponse.class);
                overlay.bindOverlay(siteResponse);
            } catch (Exception e) {
                Timber.e(e);
                preferences.remove(ConstantsKt.PREFERENCE_WEATHER);
            }
        }
        addOverlay();
        getDisposable().add(Observable.interval(0, 5, TimeUnit.MINUTES)
            .subscribeOn(Schedulers.io())
            .subscribe(value -> {
                if (System.currentTimeMillis() - preferences.getLong(ConstantsKt.PREFERENCE_LAST_DATA_TIME, 0) >= HOUR) {
                    syncWeather();
                }
            }));
        return super.onStartCommand(intent, flags, startId);
    }

    private void syncWeather() {
        Request request = new Request.Builder()
                .url(WEATHER)
                .build();
        MainApplication.client.newCall(request).enqueue(new Callback() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String json = response.body().string();
                    SiteResponse siteResponse = gson.fromJson(json, SiteResponse.class);
                    preferences.putString(ConstantsKt.PREFERENCE_WEATHER, json);
                    preferences.putLong(ConstantsKt.PREFERENCE_LAST_DATA_TIME, System.currentTimeMillis());
                    getDisposable().add(Observable.just(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(value -> overlay.bindOverlay(siteResponse)));
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
        params.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        params.format = PixelFormat.TRANSLUCENT;
        windowManager.addView(overlay, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay.getWindowToken() != null) {
            windowManager.removeView(overlay);
        }
    }
}