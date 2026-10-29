package com.example.caminata20.data.migration

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.caminata20.data.local.breakeeDataStore
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.AiRoutineRepository
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.WalkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migración única (por dispositivo y usuario) de los datos locales a Cloud Firestore:
 *  - SharedPreferences "walk_stats" (sesiones de caminata)           → users/{uid}/walks
 *  - SharedPreferences "ai_exercises_stats" (rutinas IA)             → users/{uid}/aiRoutines
 *  - SharedPreferences "reminder_prefs" (hora del recordatorio)      → settings/preferences + caché local
 *  - DataStore breakee_prefs (nombre, meta, tipo/duración de pausa, historial de pausas) → users/{uid}...
 * Tras migrar se borran las fuentes legadas para no duplicar si entra otro usuario.
 */
@Singleton
class LegacyDataMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: AuthRepository,
    private val local: LocalStateRepository,
    private val walks: WalkRepository,
    private val routines: AiRoutineRepository,
    private val prefs: PreferencesRepository,
    private val profile: ProfileRepository,
    private val history: PauseHistoryRepository,
) {
    private val mutex = Mutex()

    private object OldKeys {
        val NAME = stringPreferencesKey("profile_name")
        val GOAL = intPreferencesKey("daily_goal")
        val PAUSE_TYPE = stringPreferencesKey("pause_type")
        val PAUSE_DURATION = intPreferencesKey("pause_duration_min")
        val HISTORY = stringPreferencesKey("pause_history_json")
    }

    suspend fun migrateIfNeeded() = mutex.withLock {
        val uid = auth.currentUserOrNull()?.uid ?: return@withLock
        if (local.isMigrated(uid)) return@withLock
        val now = System.currentTimeMillis()

        val walkPrefs = context.getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        LegacyParsers.walks(walkPrefs.getString("sessions", null), now).forEach { walks.add(it) }

        val aiPrefs = context.getSharedPreferences("ai_exercises_stats", Context.MODE_PRIVATE)
        LegacyParsers.routines(aiPrefs.getString("routines", null), now).forEach { routines.add(it) }

        val reminderPrefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val hour = reminderPrefs.getInt("hour", -1)
        if (hour >= 0) prefs.saveReminder(ReminderTime(hour, reminderPrefs.getInt("minute", 0)))

        val old = context.breakeeDataStore.data.first()
        old[OldKeys.NAME]?.takeIf { it.isNotBlank() }?.let { profile.saveName(it) }
        old[OldKeys.GOAL]?.let { profile.saveGoal(it) }
        old[OldKeys.PAUSE_TYPE]?.let { prefs.savePauseSettings(PauseType.fromName(it), old[OldKeys.PAUSE_DURATION] ?: 2) }
        LegacyParsers.pauseHistory(old[OldKeys.HISTORY]).forEach { history.add(it) }

        // Limpieza de fuentes legadas
        walkPrefs.edit().clear().apply()
        aiPrefs.edit().clear().apply()
        reminderPrefs.edit().clear().apply()
        context.breakeeDataStore.edit { p ->
            listOf(OldKeys.NAME, OldKeys.PAUSE_TYPE, OldKeys.HISTORY).forEach { p.remove(it) }
            listOf(OldKeys.GOAL, OldKeys.PAUSE_DURATION).forEach { p.remove(it) }
        }
        local.setMigrated(uid)
    }
}
