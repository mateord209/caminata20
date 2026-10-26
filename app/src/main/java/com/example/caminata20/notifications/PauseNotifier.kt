package com.example.caminata20.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.caminata20.MainActivity
import com.example.caminata20.NotificationHelper
import com.example.caminata20.data.SettingsRepository

/** Construye y muestra la notificación de pausa activa. */
object PauseNotifier {
    const val NOTIFICATION_ID = 1001

    suspend fun show(context: Context) {
        NotificationHelper.createChannel(context)
        val settings = SettingsRepository(context).current()
        val type = settings.type

        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // HU-08: la notificación muestra la categoría elegida
            .setContentTitle("${type.emoji} ${type.notificationTitle}")
            .setContentText("${type.description} (${settings.durationMin} min)")
            .setSubText(type.title)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        notify(context, builder.build())
    }

    fun notify(context: Context, notification: android.app.Notification, id: Int = NOTIFICATION_ID) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
