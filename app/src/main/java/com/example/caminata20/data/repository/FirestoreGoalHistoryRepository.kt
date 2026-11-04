package com.example.caminata20.data.repository

import com.example.caminata20.data.remote.UserPaths
import com.example.caminata20.data.remote.observe
import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.repository.GoalHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste el historial de metas en `users/{uid}/settings/goalHistory`
 * con el campo `entries: [{from, goal}]` (HU-17, Adenda C2).
 * La escritura no espera al servidor: Firestore la aplica en su caché local.
 */
@Singleton
class FirestoreGoalHistoryRepository @Inject constructor(
    private val paths: UserPaths,
) : GoalHistoryRepository {
    override fun periods(): Flow<List<GoalPeriod>> = paths.perUser(emptyList()) { uid ->
        paths.goalHistory(uid).observe().map { snapshot -> readPeriods(snapshot?.data) }
    }

    override suspend fun recordChange(dayStart: Long, oldGoal: Int, newGoal: Int) {
        val uid = paths.uid() ?: return
        val entries = periods().first().toMutableList()
        // Primera meta conocida: rige desde el origen para no reevaluar días viejos con la nueva.
        if (entries.isEmpty()) entries += GoalPeriod(0L, oldGoal)
        entries.removeAll { it.fromDay == dayStart }
        entries += GoalPeriod(dayStart, newGoal)
        val ordered = entries.sortedBy { it.fromDay }
        paths.goalHistory(uid).set(
            mapOf("entries" to ordered.map { mapOf("from" to it.fromDay, "goal" to it.goal) }),
        )
    }

    private fun readPeriods(data: Map<String, Any?>?): List<GoalPeriod> =
        (data?.get("entries") as? List<*>).orEmpty()
            .mapNotNull { entry ->
                val map = entry as? Map<*, *> ?: return@mapNotNull null
                val from = (map["from"] as? Number)?.toLong() ?: return@mapNotNull null
                val goal = (map["goal"] as? Number)?.toInt() ?: return@mapNotNull null
                GoalPeriod(from, goal)
            }
            .sortedBy { it.fromDay }
}
