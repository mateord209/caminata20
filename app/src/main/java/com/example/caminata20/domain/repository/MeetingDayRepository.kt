package com.example.caminata20.domain.repository

import kotlinx.coroutines.flow.Flow

/** Días (inicio del día en ms) en que se usó el Modo Reunión (HU-17). */
interface MeetingDayRepository {
    fun days(): Flow<Set<Long>>
    suspend fun mark(days: Set<Long>)
    suspend fun prune(before: Long)
}
