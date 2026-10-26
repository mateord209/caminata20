package com.example.caminata20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.caminata20.notifications.PauseNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // HU-08: la notificación se arma con la categoría guardada (DataStore → goAsync)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PauseNotifier.show(context)
            } finally {
                pending.finish()
            }
        }

        // Avisos del temporizador/posponer (HU-03) no reprograman el recordatorio diario
        if (intent.getBooleanExtra(EXTRA_FROM_TIMER, false)) return

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

    companion object {
        const val EXTRA_FROM_TIMER = "extra_from_timer"
    }
}
