package com.example.caminata20.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.example.caminata20.domain.model.TemaApp

/**
 * HU-13: sincroniza la preferencia de tema con el modo nocturno a nivel de aplicación.
 *
 * En Android 12+ (`API 31`) `UiModeManager.setApplicationNightMode` le dice al sistema
 * qué tema usa la app, de modo que el **splash del sistema** y el `windowBackground`
 * se resuelvan con los recursos `values-night` correctos antes del primer frame.
 * El modo queda persistido por el sistema hasta que la app lo cambie o se borren sus datos.
 *
 * En Android 11 o inferior no existe esa API: el splash seguirá el tema del sistema y
 * el cambio se queda solo en Compose (limitación conocida y documentada).
 */
object AppNightMode {
    private const val PREFS = "breakee_ui"
    private const val KEY_LAST_MODE = "app_night_mode"

    fun apply(context: Context, tema: TemaApp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val desired = when (tema) {
            TemaApp.CLARO -> UiModeManager.MODE_NIGHT_NO
            TemaApp.OSCURO -> UiModeManager.MODE_NIGHT_YES
            TemaApp.SISTEMA -> UiModeManager.MODE_NIGHT_AUTO
        }

        // Evita recrear la Activity en bucle: solo se aplica cuando el modo cambia.
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_LAST_MODE, Int.MIN_VALUE) == desired) return

        val manager = context.getSystemService(UiModeManager::class.java) ?: return
        prefs.edit().putInt(KEY_LAST_MODE, desired).apply()
        manager.setApplicationNightMode(desired)
    }
}
