package com.example.caminata20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import com.example.caminata20.domain.usecase.PauseScheduleSync
import com.example.caminata20.notifications.PauseNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Dispara notificaciones de pausa/hidratación y reprograma alarmas (HU-02, HU-03, HU-14). */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var notifier: PauseNotifier
    @Inject lateinit var local: LocalStateRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var pauseScheduler: PauseScheduler
    @Inject lateinit var prefs: PreferencesRepository
    @Inject lateinit var scheduleSync: PauseScheduleSync

    override fun onReceive(context: Context, intent: Intent) {
        val fromTimer = intent.getBooleanExtra(EXTRA_FROM_TIMER, false)
        val hydration = intent.getBooleanExtra(EXTRA_HYDRATION, false)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (hydration) {
                    notifier.showHydration()
                    pauseScheduler.scheduleHydration(prefs.hidratacion.first())
                } else {
                    notifier.show()
                    if (fromTimer) {
                        val meeting = local.meetingUntil.first()
                        pauseScheduler.applySchedule(prefs.currentSchedule(), meeting)
                    } else {
                        val time = local.cachedReminder() ?: legacyReminder(context)
                        time?.let { reminderScheduler.schedule(it) }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun legacyReminder(context: Context): ReminderTime? {
        val prefsLegacy = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val h = prefsLegacy.getInt("hour", -1)
        return if (h >= 0) ReminderTime(h, prefsLegacy.getInt("minute", 0)) else null
    }

    companion object {
        const val EXTRA_FROM_TIMER = "extra_from_timer"
        const val EXTRA_HYDRATION = "extra_hydration"
    }
}
