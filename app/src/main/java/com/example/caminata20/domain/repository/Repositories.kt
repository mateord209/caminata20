package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.AparienciaApp
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.model.ScheduleProfilesState
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

    val reminderSchedule: Flow<ReminderScheduleConfig>
    suspend fun currentSchedule(): ReminderScheduleConfig
    suspend fun saveReminderSchedule(config: ReminderScheduleConfig)

    val scheduleProfiles: Flow<ScheduleProfilesState>
    suspend fun saveScheduleProfiles(state: ScheduleProfilesState)
    suspend fun activateScheduleProfile(indice: Int)

    val apariencia: Flow<AparienciaApp>
    suspend fun saveApariencia(apariencia: AparienciaApp)

    val hidratacion: Flow<ConfigHidratacion>
    suspend fun saveHidratacion(config: ConfigHidratacion)
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

    suspend fun cachedReminderSchedule(): ReminderScheduleConfig?
    suspend fun cacheReminderSchedule(config: ReminderScheduleConfig)

    val meetingUntil: Flow<Long?>
    suspend fun setMeetingUntil(until: Long?)

    suspend fun cachedHidratacion(): ConfigHidratacion?
    suspend fun cacheHidratacion(config: ConfigHidratacion)

    val lastPhraseIndex: Flow<Int?>
    suspend fun setLastPhraseIndex(index: Int)
    suspend fun lastPhraseAt(): Long?
    suspend fun setLastPhraseAt(at: Long)

    /** HU-13: apariencia aplicada al instante y persistida en el dispositivo. */
    val apariencia: Flow<AparienciaApp>
    suspend fun saveApariencia(apariencia: AparienciaApp)
}
