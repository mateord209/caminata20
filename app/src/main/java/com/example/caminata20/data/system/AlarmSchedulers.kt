package com.example.caminata20.data.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.caminata20.AlarmReceiver
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private fun AlarmManager.setExactCompat(at: Long, pi: PendingIntent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
        setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    } else {
        setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }
}

/**
 * Implementación mínima del temporizador de pausas (HU-03).
 * TODO(HU-01/HU-02): reemplazar por el temporizador de intervalos real (intervalo configurable).
 */
@Singleton
class AlarmPauseScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : PauseScheduler {
    private val am get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun restart() = scheduleIn(DEFAULT_INTERVAL_MIN)

    override fun scheduleIn(minutes: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(AlarmReceiver.EXTRA_FROM_TIMER, true)
        val pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setExactCompat(System.currentTimeMillis() + minutes * 60_000L, pi)
    }

    companion object {
        const val DEFAULT_INTERVAL_MIN = 50
        private const val REQUEST_CODE = 2001
    }
}

/** Recordatorio diario a hora fija (antes en ReminderActivity / AlarmReceiver). */
@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {
    private val am get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pending() = PendingIntent.getBroadcast(
        context, 0, Intent(context, AlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun schedule(time: ReminderTime) = am.setExactCompat(nextTrigger(time, System.currentTimeMillis()), pending())

    override fun cancel() = am.cancel(pending())

    companion object {
        /** Próxima ocurrencia de [time] (hoy si aún no pasó, si no mañana). */
        fun nextTrigger(time: ReminderTime, now: Long): Long = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, time.hour); set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
    }
}
