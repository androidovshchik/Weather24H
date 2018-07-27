package io.androidovshchik.weather24h

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.github.androidovshchik.BasePActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.BufferedReader

@SuppressLint("ExportedPreferenceActivity")
class MainActivity : BasePActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsFragment()
        fragmentManager.beginTransaction()
            .replace(android.R.id.content, settings)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logcat -> {
                Toast.makeText(applicationContext, "Подождите...",
                    Toast.LENGTH_SHORT).show()
                disposable.add(Observable.fromCallable {
                    var appLogs = ""
                    try {
                        val process = Runtime.getRuntime().exec("logcat -d")
                        appLogs = process.inputStream.bufferedReader().use(BufferedReader::readText)
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    appLogs
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { appLogs: String ->
                        sendEmail("Логи приложения", appLogs)
                    })
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun sendEmail(title: String, logs: String) {
        if (logs.trim().isEmpty()) {
            Toast.makeText(applicationContext, "Не удалось получить логи",
                Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vladkalyuzhnyu@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, logs)
        try {
            startActivity(Intent.createChooser(intent, "Отправить письмо..."))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            Toast.makeText(applicationContext, "Не удалось отправить письмо",
                Toast.LENGTH_SHORT).show()
        }
    }
}
