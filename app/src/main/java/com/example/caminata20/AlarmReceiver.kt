package com.example.caminata20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Es hora de moverte!")
            .setContentText("Levantate y caminá un poco, tu cuerpo te lo va a agradecer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }

        // Reprogramar para mañana, a la misma hora
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("hour", -1)
        val minute = prefs.getInt("minute", -1)

        if (hour >= 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextIntent = Intent(context, AlarmReceiver::class.java)
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                nextPendingIntent
            )
        }
    }
}