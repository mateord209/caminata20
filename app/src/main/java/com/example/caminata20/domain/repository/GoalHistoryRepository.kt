package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.GoalPeriod
import kotlinx.coroutines.flow.Flow

/**
 * Historial de cambios de la meta diaria (HU-17, Adenda C2).
 * Permite evaluar cada día cerrado con la meta que tenía en ese momento.
 */
interface GoalHistoryRepository {
    fun periods(): Flow<List<GoalPeriod>>

    /** Registra que la meta cambió de [oldGoal] a [newGoal] a partir de [dayStart]. */
    suspend fun recordChange(dayStart: Long, oldGoal: Int, newGoal: Int)
}
