package io.androidovshchik.weather24h

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.github.androidovshchik.BaseV7Activity
import com.github.androidovshchik.utils.AlarmUtil
import com.github.androidovshchik.utils.ServiceUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseV7Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enable.setOnCheckedChangeListener { _, isChecked ->
            preferences.putBoolean(PREFERENCE_ENABLE, isChecked)
            val isRunning = ServiceUtil.isRunning(applicationContext, MainService::class.java)
            if (isChecked) {
                if (!isRunning) {
                    AlarmUtil.next(applicationContext, SERVICE_INTERVAL, ServiceTrigger::class.java)
                    ServiceUtil.forceStartService(applicationContext, MainService::class.java, true)
                }
            } else if (isRunning) {
                ServiceUtil.stopService(applicationContext, MainService::class.java)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        enable.isChecked = preferences.getBoolean(PREFERENCE_ENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }
}
