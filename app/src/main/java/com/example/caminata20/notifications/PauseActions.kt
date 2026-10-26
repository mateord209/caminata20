package com.example.caminata20.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.caminata20.NotificationHelper
import com.example.caminata20.data.ExerciseCatalog
import com.example.caminata20.data.PauseHistoryRepository
import com.example.caminata20.data.PauseRecord
import com.example.caminata20.data.PauseStatus
import com.example.caminata20.data.SettingsRepository
import com.example.caminata20.data.SnoozeRepository
import com.example.caminata20.timer.PauseTimer
import com.example.caminata20.timer.StubPauseTimer
import kotlinx.coroutines.flow.first

/** HU-03: lógica compartida de Completar / Posponer (notificación y pantalla Alertas). */
class PauseActions(
    private val context: Context,
    private val timer: PauseTimer = StubPauseTimer,
) {
    private val settings = SettingsRepository(context)
    private val history = PauseHistoryRepository(context)
    private val snoozes = SnoozeRepository(context)

    private suspend fun currentRecord(status: PauseStatus): PauseRecord {
        val s = settings.current()
        val ex = ExerciseCatalog.byId(settings.lastExerciseId.first())
        return PauseRecord(System.currentTimeMillis(), s.type, status, ex?.name ?: "")
    }

    /** Registra la pausa con fecha y hora, reinicia el contador de posposiciones y el temporizador. */
    suspend fun complete() {
        history.add(currentRecord(PauseStatus.COMPLETADA))
        snoozes.reset()
        timer.restart(context)
        NotificationManagerCompat.from(context).cancel(PauseNotifier.NOTIFICATION_ID)
        NotificationManagerCompat.from(context).cancel(WARNING_ID)
    }

    /** Vuelve a avisar en 5 min. Devuelve true si se superaron 3 posposiciones seguidas. */
    suspend fun snooze(): Boolean {
        history.add(currentRecord(PauseStatus.POSPUESTA))
        val n = snoozes.increment()
        timer.scheduleIn(context, SnoozeRepository.SNOOZE_MINUTES)
        NotificationManagerCompat.from(context).cancel(PauseNotifier.NOTIFICATION_ID)
        val warn = n > SnoozeRepository.STREAK_WARNING_AFTER
        if (warn) showStreakWarning(n)
        return warn
    }

    private fun showStreakWarning(count: Int) {
        NotificationHelper.createChannel(context)
        val n = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Tu racha está en riesgo")
            .setStyle(NotificationCompat.BigTextStyle().bigText(streakWarningText(count)))
            .setContentText(streakWarningText(count))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        PauseNotifier.notify(context, n, WARNING_ID)
    }

    companion object {
        const val WARNING_ID = 1002
        fun streakWarningText(count: Int) =
            "Has pospuesto $count pausas seguidas. Si no completas la próxima, tu racha de pausas activas se romperá."
    }
}
