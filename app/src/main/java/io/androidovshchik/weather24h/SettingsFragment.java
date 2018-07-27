package io.androidovshchik.weather24h;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.androidovshchik.data.Preferences;
import com.github.androidovshchik.utils.ServiceUtil;

import timber.log.Timber;

import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_CLOCK_TEMP_COLOR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_CURRENT_URL;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_FORECAST_URL;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_GRAPH_BACK_COLOR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_GRID_COLOR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_ICON_DATA_COLOR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_LINE_COLOR;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_SHOW_WINDOW;
import static io.androidovshchik.weather24h.ConstantsKt.PREFERENCE_TOP_BACK_COLOR;

@SuppressWarnings("deprecation")
public class SettingsFragment extends PreferenceFragment {

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        Context context = getActivity().getApplicationContext();
        preferences = new Preferences(context);
        if (preferences.getBoolean(PREFERENCE_SHOW_WINDOW) &&
            !ServiceUtil.INSTANCE.isRunning(context, MainService.class)) {
            ServiceUtil.INSTANCE.forceStartService(context, MainService.class, true);
        }
        findPreference(PREFERENCE_SHOW_WINDOW)
            .setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                Timber.d(preference.getKey() + ": " + newValue);
                if ((Boolean) newValue) {
                    ServiceUtil.INSTANCE.forceStartService(context, MainService.class, true);
                } else {
                    ServiceUtil.INSTANCE.stopService(context, MainService.class);
                }
                return true;
            });
        findPreference(PREFERENCE_CURRENT_URL)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_FORECAST_URL)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_TOP_BACK_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_ICON_DATA_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_CLOCK_TEMP_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_GRAPH_BACK_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_GRID_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
        findPreference(PREFERENCE_LINE_COLOR)
            .setOnPreferenceChangeListener(this::onPreferenceChange);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Timber.d(preference.getKey() + ": " + newValue);
        if (preferences.getBoolean(PREFERENCE_SHOW_WINDOW)) {
            ServiceUtil.INSTANCE.forceStartService(getActivity().getApplicationContext(), MainService.class,
                true);
        }
        return true;
    }
}