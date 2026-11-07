package com.example.caminata20.data.repository

import com.example.caminata20.data.local.MeetingDayLocalStore
import com.example.caminata20.data.remote.UserPaths
import com.example.caminata20.data.remote.observe
import com.example.caminata20.domain.repository.MeetingDayRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste los días de Modo Reunión en `users/{uid}/settings/meetingDays`.
 * La caché local se usa como fuente inmediata y Firestore como sincronización.
 */
@Singleton
class FirestoreMeetingDayRepository @Inject constructor(
    private val paths: UserPaths,
    private val local: MeetingDayLocalStore,
) : MeetingDayRepository {
    private val remote: Flow<Set<Long>> = paths.perUser(emptySet()) { uid ->
        paths.meetingDays(uid).observe().map { snapshot -> readDays(snapshot?.data) }
    }

    override fun days(): Flow<Set<Long>> = combine(local.days, remote) { localDays, remoteDays ->
        localDays + remoteDays
    }

    override suspend fun mark(days: Set<Long>) {
        if (days.isEmpty()) return
        local.add(days)
        val uid = paths.uid() ?: return
        paths.meetingDays(uid).set(
            mapOf("days" to FieldValue.arrayUnion(*days.toTypedArray())),
            SetOptions.merge(),
        )
    }

    override suspend fun prune(before: Long) {
        local.prune(before)
        val uid = paths.uid() ?: return
        // Best-effort: la caché local ya quedó podada aunque falle la red.
        try {
            val snapshot = paths.meetingDays(uid).get().await()
            val kept = readDays(snapshot.data).filter { it >= before }
            paths.meetingDays(uid).set(mapOf("days" to kept), SetOptions.merge())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Sin conexión: se reintentará en la próxima poda.
        }
    }

    private fun readDays(data: Map<String, Any?>?): Set<Long> =
        (data?.get("days") as? List<*>).orEmpty()
            .mapNotNull { (it as? Number)?.toLong() }
            .toSet()
}
