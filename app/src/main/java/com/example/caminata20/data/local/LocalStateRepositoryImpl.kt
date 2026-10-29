package com.example.caminata20.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.ReminderScheduleConfig
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
        val INTERVAL_MIN = intPreferencesKey("interval_min")
        val ACTIVE_DAYS = intPreferencesKey("active_days_mask")
        val MEETING_UNTIL = longPreferencesKey("meeting_until")
        val HYDRATION_ACTIVE = booleanPreferencesKey("hydration_active")
        val HYDRATION_INTERVAL = intPreferencesKey("hydration_interval")
        val PHRASE_INDEX = intPreferencesKey("phrase_index")
        val PHRASE_AT = longPreferencesKey("phrase_at")
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

    override suspend fun cachedReminderSchedule(): ReminderScheduleConfig? {
        val p = store.data.first()
        val interval = p[Keys.INTERVAL_MIN] ?: return null
        return ReminderScheduleConfig(interval, maskToDays(p[Keys.ACTIVE_DAYS] ?: defaultMask()))
    }

    override suspend fun cacheReminderSchedule(config: ReminderScheduleConfig) {
        store.edit {
            it[Keys.INTERVAL_MIN] = config.intervalMin
            it[Keys.ACTIVE_DAYS] = daysToMask(config.activeDays)
        }
    }

    override val meetingUntil: Flow<Long?> = store.data.map {
        val v = it[Keys.MEETING_UNTIL] ?: return@map null
        if (v <= System.currentTimeMillis()) null else v
    }

    override suspend fun setMeetingUntil(until: Long?) {
        store.edit {
            if (until == null) it.remove(Keys.MEETING_UNTIL) else it[Keys.MEETING_UNTIL] = until
        }
    }

    override suspend fun cachedHidratacion(): ConfigHidratacion? {
        val p = store.data.first()
        if (!p.contains(Keys.HYDRATION_ACTIVE)) return null
        return ConfigHidratacion(p[Keys.HYDRATION_ACTIVE] ?: false, p[Keys.HYDRATION_INTERVAL] ?: 60)
    }

    override suspend fun cacheHidratacion(config: ConfigHidratacion) {
        store.edit {
            it[Keys.HYDRATION_ACTIVE] = config.activo
            it[Keys.HYDRATION_INTERVAL] = config.intervaloMin
        }
    }

    override val lastPhraseIndex: Flow<Int?> = store.data.map { it[Keys.PHRASE_INDEX] }
    override suspend fun setLastPhraseIndex(index: Int) { store.edit { it[Keys.PHRASE_INDEX] = index } }
    override suspend fun lastPhraseAt(): Long? = store.data.first()[Keys.PHRASE_AT]
    override suspend fun setLastPhraseAt(at: Long) { store.edit { it[Keys.PHRASE_AT] = at } }

    companion object {
        fun daysToMask(days: Set<Int>): Int = ReminderScheduleConfig.daysToMask(days)
        fun maskToDays(mask: Int): Set<Int> = ReminderScheduleConfig.maskToDays(mask)
        fun defaultMask(): Int = ReminderScheduleConfig.defaultDaysMask()
    }
}
