package dev.vffuunnyy.push_bridger

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object NotificationUtils {

    private const val CHANNEL_ID = "foreground_service_channel"
    private const val CHANNEL_NAME = "Foreground Service Channel"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    fun createForegroundNotification(context: Context, text: String): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, notificationIntent, PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun getNotificationId(): Int {
        return NOTIFICATION_ID
    }
}