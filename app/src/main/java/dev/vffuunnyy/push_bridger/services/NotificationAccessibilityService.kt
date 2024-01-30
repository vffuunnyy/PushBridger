package dev.vffuunnyy.push_bridger.services

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
import dev.vffuunnyy.push_bridger.NotificationUtils
import dev.vffuunnyy.push_bridger.R
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

class NotificationAccessibilityService : AccessibilityService() {

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        NotificationUtils.createNotificationChannel(this)

        val foregroundNotification = NotificationUtils.createForegroundNotification(
            this,
            getString(R.string.waiting_for_notifications)
        )
        startForeground(NotificationUtils.getNotificationId(), foregroundNotification)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            if (event.eventType != TYPE_NOTIFICATION_STATE_CHANGED)
                return

            val packageName = event.packageName.toString()
            val notification = event.parcelableData as? Notification
            val extras = notification?.extras
            val title = extras?.getString(Notification.EXTRA_TITLE)
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)

            if (packageName == this.packageName)
                return

            val jsonObject = JSONObject().apply {
                put("package_name", packageName)
                put("title", title ?: "")
                put("text", text.toString())
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val sendingNotification = NotificationUtils.createForegroundNotification(
                this,
                getString(R.string.sending_notifications) + " " + packageName
            )
            notificationManager.notify(NotificationUtils.getNotificationId(), sendingNotification)

            coroutineScope.launch {
                sendPostRequest(jsonObject.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sendPostRequest(postBody: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val foregroundNotification = NotificationUtils.createForegroundNotification(
            this,
            getString(R.string.waiting_for_notifications)
        )

        withContext(Dispatchers.IO) {
            for (x in 0..5) {
                try {
                    val serverUrl =
                        getSharedPreferences("AppPrefs", MODE_PRIVATE)?.getString("serverUrl", null)
                            ?: return@withContext

                    val requestBody =
                        postBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request = Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute()

                    notificationManager.notify(
                        NotificationUtils.getNotificationId(),
                        foregroundNotification
                    )

                    return@withContext
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(5000)
                }
            }
        }
    }

    override fun onInterrupt() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}