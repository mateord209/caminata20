package com.example.caminata20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.caminata20.domain.usecase.PauseScheduleSync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reprograma alarmas tras reiniciar el dispositivo (HU-01, HU-02). */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduleSync: PauseScheduleSync

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduleSync.syncAll()
            } finally {
                pending.finish()
            }
        }
    }
}
