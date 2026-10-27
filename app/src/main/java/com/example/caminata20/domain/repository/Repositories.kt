package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.model.WalkSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    fun currentUserOrNull(): AuthUser?
    suspend fun signInWithEmail(email: String, password: String): Outcome<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Outcome<AuthUser>
    suspend fun register(name: String, email: String, password: String): Outcome<AuthUser>
    suspend fun sendPasswordReset(email: String): Outcome<Unit>
    suspend fun updateDisplayName(name: String)
    fun signOut()
}

interface ProfileRepository {
    val profile: Flow<Profile>
    suspend fun saveName(name: String)
    suspend fun saveGoal(goal: Int)
}

interface PreferencesRepository {
    val pauseSettings: Flow<PauseSettings>
    suspend fun current(): PauseSettings
    suspend fun savePauseSettings(type: PauseType, durationMin: Int)
    val reminder: Flow<ReminderTime?>
    suspend fun saveReminder(time: ReminderTime?)
}

interface PauseHistoryRepository {
    val records: Flow<List<PauseRecord>>
    val today: Flow<List<PauseRecord>>
    suspend fun add(record: PauseRecord)
}

interface WalkRepository {
    val sessions: Flow<List<WalkSession>>
    suspend fun add(session: WalkSession)
    suspend fun delete(ids: Collection<String>)
}

interface AiRoutineRepository {
    val routines: Flow<List<AiRoutine>>
    suspend fun add(routine: AiRoutine)
    suspend fun delete(ids: Collection<String>)
}

/** Generación de rutinas con IA (Groq directo o vía proxy). */
interface AiExerciseGenerator {
    suspend fun generate(): Outcome<List<AiExercise>>
}

/** Estado local del dispositivo (no se sincroniza). */
interface LocalStateRepository {
    val onboardingDone: Flow<Boolean>
    suspend fun setOnboardingDone()
    val consecutiveSnoozes: Flow<Int>
    suspend fun incrementSnoozes(): Int
    suspend fun resetSnoozes()
    val lastExerciseId: Flow<String?>
    suspend fun setLastExercise(id: String)
    suspend fun isMigrated(uid: String): Boolean
    suspend fun setMigrated(uid: String)
    /** Copia local de la hora del recordatorio para AlarmReceiver/arranque sin red. */
    suspend fun cachedReminder(): ReminderTime?
    suspend fun cacheReminder(time: ReminderTime?)
}
