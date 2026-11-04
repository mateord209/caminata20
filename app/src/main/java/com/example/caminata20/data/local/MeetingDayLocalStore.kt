package com.example.caminata20.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

internal object MeetingDayKeys {
    val DAYS = stringSetPreferencesKey("meeting_days")
}

/** Caché local de los días con Modo Reunión; evita depender de la red (HU-17). */
@Singleton
class MeetingDayLocalStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val days: Flow<Set<Long>> = context.breakeeDataStore.data.map { prefs ->
        prefs[MeetingDayKeys.DAYS].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    suspend fun add(days: Set<Long>) {
        if (days.isEmpty()) return
        context.breakeeDataStore.edit { prefs ->
            prefs[MeetingDayKeys.DAYS] = prefs[MeetingDayKeys.DAYS].orEmpty() +
                days.map(Long::toString)
        }
    }

    suspend fun prune(before: Long) {
        context.breakeeDataStore.edit { prefs ->
            val kept = prefs[MeetingDayKeys.DAYS].orEmpty()
                .mapNotNull(String::toLongOrNull)
                .filter { it >= before }
                .map(Long::toString)
                .toSet()
            prefs[MeetingDayKeys.DAYS] = kept
        }
    }

    suspend fun clear() {
        context.breakeeDataStore.edit { prefs -> prefs.remove(MeetingDayKeys.DAYS) }
    }
}
