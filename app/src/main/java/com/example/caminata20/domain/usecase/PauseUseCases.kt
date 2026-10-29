package com.example.caminata20.domain.usecase

import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PauseNotificationGateway
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** HU-03: lógica compartida de Completar / Posponer (notificación y pantalla Alertas). */
class PauseActionsUseCase @Inject constructor(
    private val prefs: PreferencesRepository,
    private val history: PauseHistoryRepository,
    private val local: LocalStateRepository,
    private val scheduler: PauseScheduler,
    private val notifications: PauseNotificationGateway,
    private val clock: Clock,
) {
    private suspend fun record(status: PauseStatus): PauseRecord {
        val s = prefs.current()
        val ex = ExerciseCatalog.byId(local.lastExerciseId.first())
        return PauseRecord(clock.now(), s.type, status, ex?.name ?: "")
    }

    /** Registra la pausa (fecha y hora), reinicia el contador de posposiciones y el temporizador. */
    suspend fun complete() {
        history.add(record(PauseStatus.COMPLETADA))
        local.resetSnoozes()
        scheduler.restart()
        notifications.dismissPause()
    }

    /** Vuelve a avisar en 5 min. Devuelve true si se superaron 3 posposiciones seguidas. */
    suspend fun snooze(): Boolean {
        history.add(record(PauseStatus.POSPUESTA))
        val n = local.incrementSnoozes()
        scheduler.scheduleIn(SNOOZE_MINUTES)
        notifications.dismissPause()
        val warn = n > STREAK_WARNING_AFTER
        if (warn) notifications.showStreakWarning(n)
        return warn
    }

    companion object {
        const val SNOOZE_MINUTES = 5
        const val STREAK_WARNING_AFTER = 3
        fun streakWarningText(count: Int) =
            "Has pospuesto $count pausas seguidas. Si no completas la próxima, tu racha de pausas activas se romperá."
    }
}

/** Reloj inyectable (facilita tests). */
fun interface Clock {
    fun now(): Long
}
