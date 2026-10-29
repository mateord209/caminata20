package com.example.caminata20.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.LocalStateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalStateRepository {
    private val store get() = context.breakeeDataStore

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
        val SNOOZES = intPreferencesKey("consecutive_snoozes")
        val LAST_EXERCISE = stringPreferencesKey("last_exercise_id")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        fun migrated(uid: String) = booleanPreferencesKey("migrated_$uid")
    }

    override val onboardingDone: Flow<Boolean> = store.data.map { it[Keys.ONBOARDING] ?: false }
    override suspend fun setOnboardingDone() { store.edit { it[Keys.ONBOARDING] = true } }

    override val consecutiveSnoozes: Flow<Int> = store.data.map { it[Keys.SNOOZES] ?: 0 }
    override suspend fun incrementSnoozes(): Int {
        var n = 0
        store.edit { n = (it[Keys.SNOOZES] ?: 0) + 1; it[Keys.SNOOZES] = n }
        return n
    }
    override suspend fun resetSnoozes() { store.edit { it[Keys.SNOOZES] = 0 } }

    override val lastExerciseId: Flow<String?> = store.data.map { it[Keys.LAST_EXERCISE] }
    override suspend fun setLastExercise(id: String) { store.edit { it[Keys.LAST_EXERCISE] = id } }

    override suspend fun isMigrated(uid: String) = store.data.first()[Keys.migrated(uid)] ?: false
    override suspend fun setMigrated(uid: String) { store.edit { it[Keys.migrated(uid)] = true } }

    override suspend fun cachedReminder(): ReminderTime? {
        val p = store.data.first()
        val h = p[Keys.REMINDER_HOUR] ?: return null
        return ReminderTime(h, p[Keys.REMINDER_MINUTE] ?: 0)
    }

    override suspend fun cacheReminder(time: ReminderTime?) {
        store.edit {
            if (time == null) { it.remove(Keys.REMINDER_HOUR); it.remove(Keys.REMINDER_MINUTE) }
            else { it[Keys.REMINDER_HOUR] = time.hour; it[Keys.REMINDER_MINUTE] = time.minute }
        }
    }
}
