package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.ReminderTime

/**
 * Temporizador de intervalos de pausas.
 * TODO(HU-01/HU-02): el temporizador real (intervalo y horario laboral configurables) lo
 * implementa otro integrante; aquí solo está lo que necesita HU-03 (reiniciar / posponer).
 */
interface PauseScheduler {
    fun restart()
    fun scheduleIn(minutes: Int)
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
