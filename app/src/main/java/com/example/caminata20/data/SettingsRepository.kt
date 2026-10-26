package com.example.caminata20.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class PauseSettings(val type: PauseType = PauseType.ESTIRAMIENTO, val durationMin: Int = 2)

/** Preferencias de usuario de Breakee (DataStore). */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val PAUSE_TYPE = stringPreferencesKey("pause_type")
        val PAUSE_DURATION = intPreferencesKey("pause_duration_min")
        val LAST_EXERCISE = stringPreferencesKey("last_exercise_id")
    }

    val pauseSettings: Flow<PauseSettings> = context.breakeeDataStore.data.map {
        PauseSettings(PauseType.fromName(it[Keys.PAUSE_TYPE]), it[Keys.PAUSE_DURATION] ?: 2)
    }

    suspend fun current(): PauseSettings = pauseSettings.first()

    /** HU-16: último ejercicio sugerido/notificado (para rotar y para la pantalla guía). */
    val lastExerciseId: Flow<String?> = context.breakeeDataStore.data.map { it[Keys.LAST_EXERCISE] }

    suspend fun setLastExercise(id: String) {
        context.breakeeDataStore.edit { it[Keys.LAST_EXERCISE] = id }
    }

    suspend fun savePauseSettings(type: PauseType, durationMin: Int) {
        context.breakeeDataStore.edit {
            it[Keys.PAUSE_TYPE] = type.name
            it[Keys.PAUSE_DURATION] = durationMin
        }
    }
}
