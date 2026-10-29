package com.example.caminata20.data.repository

import com.example.caminata20.data.remote.FirestoreMappers
import com.example.caminata20.data.remote.UserPaths
import com.example.caminata20.data.remote.observe
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.repository.AiRoutineRepository
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.WalkRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Las escrituras NO esperan al servidor (no hay await): Firestore las aplica primero en la caché
 * local persistente y las sincroniza al volver la conexión. Así la app funciona offline.
 */

@Singleton
class FirestoreProfileRepository @Inject constructor(
    private val paths: UserPaths,
    private val auth: FirebaseAuth,
    private val authRepo: AuthRepository,
) : ProfileRepository {
    override val profile: Flow<Profile> = paths.perUser(Profile()) { uid ->
        paths.userDoc(uid).observe().map { snap ->
            val u = auth.currentUser
            FirestoreMappers.profile(snap?.data, u?.displayName, u?.email)
        }
    }

    override suspend fun saveName(name: String) {
        val uid = paths.uid() ?: return
        paths.userDoc(uid).set(mapOf("name" to name.trim(), "email" to auth.currentUser?.email, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
        authRepo.updateDisplayName(name.trim())
    }

    override suspend fun saveGoal(goal: Int) {
        val uid = paths.uid() ?: return
        paths.userDoc(uid).set(mapOf("dailyGoal" to goal.coerceIn(Profile.GOAL_RANGE), "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
    }
}

@Singleton
class FirestorePreferencesRepository @Inject constructor(
    private val paths: UserPaths,
    private val local: LocalStateRepository,
) : PreferencesRepository {
    private val doc: Flow<Map<String, Any?>?> = paths.perUser(null) { uid -> paths.preferences(uid).observe().map { it?.data } }

    override val pauseSettings: Flow<PauseSettings> = doc.map { FirestoreMappers.pauseSettings(it) }
    override suspend fun current(): PauseSettings = pauseSettings.first()

    override suspend fun savePauseSettings(type: PauseType, durationMin: Int) {
        val uid = paths.uid() ?: return
        paths.preferences(uid).set(mapOf("pauseType" to type.name, "pauseDurationMin" to durationMin), SetOptions.merge())
    }

    override val reminder: Flow<ReminderTime?> = doc.map { FirestoreMappers.reminder(it) }

    override suspend fun saveReminder(time: ReminderTime?) {
        local.cacheReminder(time)
        val uid = paths.uid() ?: return
        paths.preferences(uid).set(
            mapOf("reminderHour" to (time?.hour ?: -1), "reminderMinute" to (time?.minute ?: 0)), SetOptions.merge(),
        )
    }
}

@Singleton
class FirestorePauseHistoryRepository @Inject constructor(
    private val paths: UserPaths,
) : PauseHistoryRepository {
    override val records: Flow<List<PauseRecord>> = paths.perUser(emptyList()) { uid ->
        paths.pauseHistory(uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(500).observe()
            .map { s -> s?.documents.orEmpty().map { FirestoreMappers.pauseRecord(it.id, it.data.orEmpty()) } }
    }

    override val today: Flow<List<PauseRecord>> = paths.perUser(emptyList()) { uid ->
        paths.pauseHistory(uid).whereGreaterThanOrEqualTo("timestamp", startOfToday()).observe()
            .map { s -> s?.documents.orEmpty().map { FirestoreMappers.pauseRecord(it.id, it.data.orEmpty()) } }
    }

    override suspend fun add(record: PauseRecord) {
        val uid = paths.uid() ?: return
        paths.pauseHistory(uid).add(FirestoreMappers.pauseRecordToMap(record))
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Singleton
class FirestoreWalkRepository @Inject constructor(
    private val paths: UserPaths,
    private val db: FirebaseFirestore,
) : WalkRepository {
    override val sessions: Flow<List<WalkSession>> = paths.perUser(emptyList()) { uid ->
        paths.walks(uid).orderBy("timestamp", Query.Direction.DESCENDING).observe()
            .map { s -> s?.documents.orEmpty().map { FirestoreMappers.walk(it.id, it.data.orEmpty()) } }
    }

    override suspend fun add(session: WalkSession) {
        val uid = paths.uid() ?: return
        paths.walks(uid).add(FirestoreMappers.walkToMap(session))
    }

    override suspend fun delete(ids: Collection<String>) {
        val uid = paths.uid() ?: return
        val batch = db.batch()
        ids.forEach { batch.delete(paths.walks(uid).document(it)) }
        batch.commit()
    }
}

@Singleton
class FirestoreAiRoutineRepository @Inject constructor(
    private val paths: UserPaths,
    private val db: FirebaseFirestore,
) : AiRoutineRepository {
    override val routines: Flow<List<AiRoutine>> = paths.perUser(emptyList()) { uid ->
        paths.aiRoutines(uid).orderBy("timestamp", Query.Direction.DESCENDING).observe()
            .map { s -> s?.documents.orEmpty().map { FirestoreMappers.routine(it.id, it.data.orEmpty()) } }
    }

    override suspend fun add(routine: AiRoutine) {
        val uid = paths.uid() ?: return
        paths.aiRoutines(uid).add(FirestoreMappers.routineToMap(routine))
    }

    override suspend fun delete(ids: Collection<String>) {
        val uid = paths.uid() ?: return
        val batch = db.batch()
        ids.forEach { batch.delete(paths.aiRoutines(uid).document(it)) }
        batch.commit()
    }
}
