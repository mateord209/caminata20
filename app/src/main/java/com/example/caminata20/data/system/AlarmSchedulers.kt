package com.example.caminata20.data.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.caminata20.AlarmReceiver
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.ReminderScheduleConfig
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

/** Temporizador de pausas por intervalo (HU-01, HU-02, HU-06, HU-14). */
@Singleton
class AlarmPauseScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : PauseScheduler {
    private val am get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun restart() = applySchedule(ReminderScheduleConfig())

    override fun scheduleIn(minutes: Int) = scheduleAt(System.currentTimeMillis() + minutes * 60_000L, REQUEST_PAUSE)

    override fun cancel() {
        am.cancel(pausePending())
        am.cancel(hydrationPending())
    }

    override fun applySchedule(config: ReminderScheduleConfig, meetingUntil: Long?) {
        am.cancel(pausePending())
        val now = System.currentTimeMillis()
        if (meetingUntil != null && now < meetingUntil) {
            scheduleAt(meetingUntil, REQUEST_PAUSE)
            return
        }
        if (!config.isActiveToday(now)) return
        scheduleAt(now + config.intervalMin * 60_000L, REQUEST_PAUSE)
    }

    override fun scheduleHydration(config: ConfigHidratacion) {
        am.cancel(hydrationPending())
        if (!config.activo) return
        scheduleAt(System.currentTimeMillis() + config.intervaloMin * 60_000L, REQUEST_HYDRATION)
    }

    override fun cancelHydration() = am.cancel(hydrationPending())

    private fun scheduleAt(at: Long, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_FROM_TIMER, true)
            if (requestCode == REQUEST_HYDRATION) putExtra(AlarmReceiver.EXTRA_HYDRATION, true)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.setExactCompat(at, pi)
    }

    private fun pausePending() = pending(REQUEST_PAUSE, false)
    private fun hydrationPending() = pending(REQUEST_HYDRATION, true)

    private fun pending(requestCode: Int, hydration: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_FROM_TIMER, true)
            if (hydration) putExtra(AlarmReceiver.EXTRA_HYDRATION, true)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val DEFAULT_INTERVAL_MIN = ReminderScheduleConfig.DEFAULT_INTERVAL
        private const val REQUEST_PAUSE = 2001
        private const val REQUEST_HYDRATION = 2002
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
