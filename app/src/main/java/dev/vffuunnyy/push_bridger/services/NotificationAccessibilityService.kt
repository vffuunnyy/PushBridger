package dev.vffuunnyy.push_bridger.services

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
import dev.vffuunnyy.push_bridger.NotificationUtils
import dev.vffuunnyy.push_bridger.R
import dev.vffuunnyy.push_bridger.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class NotificationAccessibilityService : AccessibilityService() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()

        val intent = Intent("ACTION_UPDATE_STATUS")
        intent.putExtra("service_status", true)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        coroutineScope.cancel()

        val intent = Intent("ACTION_UPDATE_STATUS")
        intent.putExtra("service_status", false)
        sendBroadcast(intent)

        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            if (event.eventType != TYPE_NOTIFICATION_STATE_CHANGED)
                return

            val packageName = event.packageName.toString()
            val notification = event.parcelableData as? Notification
            val extras = notification?.extras
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)

            if (packageName == this.packageName)
                return

            val regexString = getString(R.string.sms_regex)

            if (!text.toString().matches(Regex(regexString))) {
                return
            }

            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

            val jsonObject = JSONObject().apply {
                put("dial_code", sharedPreferences?.getString("dialCode", ""))
                put("mobile_number", sharedPreferences?.getString("phoneNumber", ""))
                put("text", text.toString())
                put("type", 1)
            }

            coroutineScope.launch {
                try {
                    val resp = Utils.sendPostRequest(
                        getString(R.string.server_sms_url),
                        mapOf("Authorization" to "Bearer ${sharedPreferences?.getString("token", "")}"),
                        jsonObject.toString()
                    )
                    Log.d("NotificationAccessibilityService", "SMS sent: ${resp.body.toString()}")
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        onDestroy()
    }
}