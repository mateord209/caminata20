package com.example.caminata20.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** HU-20: bandera para mostrar el onboarding una sola vez. */
class OnboardingRepository(private val context: Context) {
    private val key = booleanPreferencesKey("onboarding_done")

    val isDone: Flow<Boolean> = context.breakeeDataStore.data.map { it[key] ?: false }

    suspend fun isDoneOnce(): Boolean = isDone.first()

    suspend fun markDone() {
        context.breakeeDataStore.edit { it[key] = true }
    }
}
