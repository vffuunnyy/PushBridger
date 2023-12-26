package dev.vffuunnyy.push_bridger.listeners

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.vffuunnyy.push_bridger.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NotificationListener : NotificationListenerService() {

    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val title = notification.extras.getString("android.title")
        val text = notification.extras.getString("android.text")

        val jsonObject = JSONObject().apply {
            put("package_name", packageName)
            put("title", title ?: "")
            put("text", text ?: "")
        }

        coroutineScope.launch {
            sendPostRequest(jsonObject.toString())
        }
    }

    private suspend fun sendPostRequest(postBody: String) {
        withContext(Dispatchers.IO) {
            val requestBody =
                postBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val serverUrl = getString(R.string.server_url)
            val request = Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}