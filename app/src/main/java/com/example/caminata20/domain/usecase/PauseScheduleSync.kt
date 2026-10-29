package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Sincroniza alarmas locales con preferencias y caché del dispositivo (HU-01, HU-06, HU-14). */
@Singleton
class PauseScheduleSync @Inject constructor(
    private val prefs: PreferencesRepository,
    private val local: LocalStateRepository,
    private val pauseScheduler: PauseScheduler,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend fun syncPauseTimers() {
        val meeting = local.meetingUntil.first()
        pauseScheduler.applySchedule(prefs.currentSchedule(), meeting)
        pauseScheduler.scheduleHydration(prefs.hidratacion.first())
    }

    suspend fun syncDailyReminder() {
        val time = local.cachedReminder() ?: prefs.reminder.first()
        if (time != null) reminderScheduler.schedule(time) else reminderScheduler.cancel()
    }

    suspend fun syncAll() {
        syncPauseTimers()
        syncDailyReminder()
    }
}
