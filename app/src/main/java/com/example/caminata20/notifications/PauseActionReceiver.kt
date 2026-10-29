package com.example.caminata20.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** HU-03: acciones Completar / Posponer de la notificación. */
@AndroidEntryPoint
class PauseActionReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: PauseActionsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
