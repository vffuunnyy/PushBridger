package dev.vffuunnyy.push_bridger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class Utils {
    companion object {
        private val client = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        suspend fun sendPostRequest(
            url: String,
            headers: Map<String, String>?,
            postBody: String
        ): Response {
            return withContext(Dispatchers.IO) {
                var lastException: Exception? = null
                for (x in 0..5) {
                    try {
                        val requestBody =
                            postBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                        val requestBuilder = Request.Builder()
                            .url(url)
                            .post(requestBody)

                        headers?.forEach { (key, value) ->
                            requestBuilder.addHeader(key, value)
                        }

                        val request = requestBuilder.build()
                        return@withContext client.newCall(request).execute()
                    } catch (e: Exception) {
                        lastException = e
                        e.printStackTrace()
                        delay(5000)
                    }
                }
                throw lastException
                    ?: Exception("Failed to send post request after multiple attempts")
            }
        }
    }
}