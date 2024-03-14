package dev.vffuunnyy.push_bridger

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isBatteryOptimizationIgnored()) {
            showIgnoreBatteryOptimizationAlertDialog()  // Request to ignore battery optimization
        }

        // if (!areNotificationsEnabled()) {
        //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //         requestNotificationPermission()
        //     }
        // }

        if (!isAccessibilitySettingsEnabled()) {
            openAccessibilitySettings()
        }
    }

    override fun onResume() {
        super.onResume()
//        updateAccessibilityServiceStatus(isAccessibilitySettingsEnabled())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
//        registerReceiver(
//            updateStatusReceiver, IntentFilter("dev.vffuunnyy.push_bridger.UPDATE_STATUS"),
//            RECEIVER_NOT_EXPORTED
//        )
    }

    override fun onStop() {
        super.onStop()
//        unregisterReceiver(updateStatusReceiver)
    }

//    private val updateStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val isEnabled = intent.getBooleanExtra("service_status", false)
//            updateAccessibilityServiceStatus(isEnabled)
//        }
//    }


//    private fun updateAccessibilityServiceStatus(isEnabled: Boolean) {
//        val switchButton = findViewById<Button>(R.id.update_accessibility_service_status)
//
//        if (isEnabled) {
//            switchButton.text = getString(R.string.disable)
//            switchButton.backgroundTintList = getColorStateList(R.color.disable)
//        } else {
//            switchButton.text = getString(R.string.enable)
//            switchButton.backgroundTintList = getColorStateList(R.color.enable)
//        }
//
//        switchButton.setOnClickListener {
//            openAccessibilitySettings()
//        }
//    }

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
