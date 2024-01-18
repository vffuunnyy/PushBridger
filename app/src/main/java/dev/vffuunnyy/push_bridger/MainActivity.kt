package dev.vffuunnyy.push_bridger

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


var sharedPreferences: SharedPreferences? = null

class MainActivity : AppCompatActivity() {

    private var serverUrl: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isNotificationServiceEnabled()) {
            showEnableNotificationListenerAlertDialog()  // Redirect to notification settings
        }

        if (!isBatteryOptimizationIgnored()) {
            showIgnoreBatteryOptimizationAlertDialog()  // Request to ignore battery optimization
        }

        if (!isAccessibilitySettingsEnabled()) {
            openAccessibilitySettings()  // Redirect to accessibility settings
        }

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        serverUrl = findViewById(R.id.server_url)
        serverUrl?.setText(sharedPreferences?.getString("serverUrl", ""))

        findViewById<Button>(R.id.save_button).setOnClickListener {
            sharedPreferences?.edit()
                ?.putString("serverUrl", serverUrl?.text.toString())
                ?.apply()
        }
    }

    private fun isAccessibilitySettingsEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
        return accessibilityEnabled == 1
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.split(":")?.any { it.contains(packageName) } == true
    }

    private fun showEnableNotificationListenerAlertDialog() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    private fun showIgnoreBatteryOptimizationAlertDialog() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
