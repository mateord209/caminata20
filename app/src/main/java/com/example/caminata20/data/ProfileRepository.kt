package com.example.caminata20.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Profile(val name: String, val email: String, val dailyGoal: Int)

/** HU-12: perfil (nombre editable, correo de solo lectura desde Firebase, meta diaria 2..15). */
class ProfileRepository(private val context: Context) {
    private val nameKey = stringPreferencesKey("profile_name")
    private val goalKey = intPreferencesKey("daily_goal")
    private val auth get() = FirebaseAuth.getInstance()

    val profile: Flow<Profile> = context.breakeeDataStore.data.map { prefs ->
        val user = auth.currentUser
        Profile(
            name = prefs[nameKey] ?: user?.displayName?.takeIf { it.isNotBlank() } ?: "",
            email = user?.email ?: "",
            dailyGoal = (prefs[goalKey] ?: DEFAULT_GOAL).coerceIn(GOAL_RANGE),
        )
    }

    suspend fun save(name: String, goal: Int) {
        context.breakeeDataStore.edit {
            it[nameKey] = name.trim()
            it[goalKey] = goal.coerceIn(GOAL_RANGE)
        }
        // Sincroniza el nombre visible en Firebase Auth (no bloquea si falla)
        auth.currentUser?.updateProfile(userProfileChangeRequest { displayName = name.trim() })
    }

    suspend fun saveGoal(goal: Int) {
        context.breakeeDataStore.edit { it[goalKey] = goal.coerceIn(GOAL_RANGE) }
    }

    companion object {
        val GOAL_RANGE = 2..15
        const val DEFAULT_GOAL = 8
    }
}
