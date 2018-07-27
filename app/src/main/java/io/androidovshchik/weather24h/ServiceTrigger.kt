package io.androidovshchik.weather24h

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.androidovshchik.data.Preferences
import com.github.androidovshchik.utils.AlarmUtil
import com.github.androidovshchik.utils.ServiceUtil
import timber.log.Timber

class ServiceTrigger : BroadcastReceiver() {

    private lateinit var preferences: Preferences

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("ServiceTrigger")
        preferences = Preferences(context)
        if (preferences.has(PREFERENCE_SHOW_WINDOW)) {
            AlarmUtil.next(context, SERVICE_INTERVAL, ServiceTrigger::class.java)
            ServiceUtil.forceStartService(context, MainService::class.java, true)
        } else {
            if (ServiceUtil.isRunning(context, MainService::class.java)) {
                ServiceUtil.stopService(context, MainService::class.java)
            }
        }
    }
}