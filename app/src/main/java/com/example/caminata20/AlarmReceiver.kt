package com.example.caminata20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import com.example.caminata20.notifications.PauseNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Dispara la notificación de pausa y reprograma el recordatorio diario. */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var notifier: PauseNotifier
    @Inject lateinit var local: LocalStateRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val fromTimer = intent.getBooleanExtra(EXTRA_FROM_TIMER, false)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notifier.show()
                // Avisos del temporizador/posponer (HU-03) no reprograman el recordatorio diario
                if (!fromTimer) {
                    val time = local.cachedReminder() ?: legacyReminder(context)
                    time?.let { reminderScheduler.schedule(it) }
                }
            } finally {
                pending.finish()
            }
        }
    }

    // TODO(limpieza): eliminar cuando todos los usuarios hayan migrado (LegacyDataMigrator).
    private fun legacyReminder(context: Context): ReminderTime? {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val h = prefs.getInt("hour", -1)
        return if (h >= 0) ReminderTime(h, prefs.getInt("minute", 0)) else null
    }

    companion object {
        const val EXTRA_FROM_TIMER = "extra_from_timer"
    }
}
