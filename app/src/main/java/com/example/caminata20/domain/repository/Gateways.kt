package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.ReminderTime

/** Temporizador de intervalos de pausas (HU-01, HU-02, HU-06, HU-14). */
interface PauseScheduler {
    fun restart()
    fun scheduleIn(minutes: Int)
    fun cancel()
    fun applySchedule(config: ReminderScheduleConfig, meetingUntil: Long? = null)
    fun scheduleHydration(config: ConfigHidratacion)
    fun cancelHydration()
}

/** Recordatorio diario a hora fija (pantalla Recordatorio). */
interface ReminderScheduler {
    fun schedule(time: ReminderTime)
    fun cancel()
}

/** Efectos de notificación que disparan los casos de uso. */
interface PauseNotificationGateway {
    fun dismissPause()
    fun showStreakWarning(consecutive: Int)
}
