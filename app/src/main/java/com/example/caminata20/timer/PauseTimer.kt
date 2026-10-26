package com.example.caminata20.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.caminata20.AlarmReceiver

/**
 * Contrato del temporizador de intervalos de pausas.
 * TODO(HU-01/HU-02): la configuración del intervalo y horario laboral y el temporizador real los
 * implementa otro integrante. Este módulo solo expone lo que necesita HU-03 (reiniciar / posponer).
 */
interface PauseTimer {
    /** Reinicia el ciclo tras completar una pausa. */
    fun restart(context: Context)

    /** Programa un aviso puntual dentro de [minutes] minutos (Posponer). */
    fun scheduleIn(context: Context, minutes: Int)
}

/** Implementación mínima basada en AlarmManager. Reemplazar por la de HU-01/02. */
object StubPauseTimer : PauseTimer {
    // TODO(HU-01/HU-02): leer el intervalo configurado por el usuario.
    const val DEFAULT_INTERVAL_MIN = 50
    private const val REQUEST_CODE = 2001

    override fun restart(context: Context) = scheduleIn(context, DEFAULT_INTERVAL_MIN)

    override fun scheduleIn(context: Context, minutes: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(AlarmReceiver.EXTRA_FROM_TIMER, true)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val at = System.currentTimeMillis() + minutes * 60_000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
