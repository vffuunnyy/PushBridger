package dev.vffuunnyy.push_bridger

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import dev.vffuunnyy.push_bridger.services.NotificationAccessibilityService

class MainActivity : AppCompatActivity() {

    private var serverUrl: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isBatteryOptimizationIgnored()) {
            showIgnoreBatteryOptimizationAlertDialog()  // Request to ignore battery optimization
        }

        if (!isAccessibilitySettingsEnabled()) {
            openAccessibilitySettings()  // Redirect to accessibility settings
        }

        if (!areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission()
            }
        }

        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        serverUrl = findViewById(R.id.server_url)
        serverUrl?.setText(sharedPreferences?.getString("serverUrl", ""))

        findViewById<Button>(R.id.save_button).setOnClickListener {
            sharedPreferences?.edit()
                ?.putString("serverUrl", serverUrl?.text.toString())
                ?.apply()
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1234)
    }

    private fun isAccessibilitySettingsEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val componentName = ComponentName(
            packageName,
            NotificationAccessibilityService::class.java.name
        ).flattenToString()
        return flat?.split(":")?.any { it.contains(componentName) } == true
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
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
