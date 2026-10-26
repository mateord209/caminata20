package com.example.caminata20.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** HU-03: contador de posposiciones consecutivas. */
class SnoozeRepository(private val context: Context) {
    private val key = intPreferencesKey("consecutive_snoozes")

    val consecutive: Flow<Int> = context.breakeeDataStore.data.map { it[key] ?: 0 }

    /** Incrementa y devuelve el nuevo total. */
    suspend fun increment(): Int {
        var n = 0
        context.breakeeDataStore.edit { n = (it[key] ?: 0) + 1; it[key] = n }
        return n
    }

    suspend fun reset() {
        context.breakeeDataStore.edit { it[key] = 0 }
    }

    companion object {
        const val SNOOZE_MINUTES = 5
        /** Más de este número de posposiciones seguidas muestra la advertencia de racha. */
        const val STREAK_WARNING_AFTER = 3
    }
}
