package com.example.caminata20.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** HU-03: recibe las acciones Completar / Posponer de la notificación. */
class PauseActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actions = PauseActions(context.applicationContext)
                when (intent.action) {
                    ACTION_COMPLETE -> actions.complete()
                    ACTION_SNOOZE -> actions.snooze()
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.example.caminata20.action.PAUSE_COMPLETE"
        const val ACTION_SNOOZE = "com.example.caminata20.action.PAUSE_SNOOZE"
    }
}
