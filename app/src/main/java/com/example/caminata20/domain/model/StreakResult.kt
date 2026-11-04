package com.example.caminata20.domain.model

/** Resumen de la racha que consumen las pantallas (HU-17). */
data class StreakResult(
    val current: Int = 0,
    val todayCompleted: Int = 0,
    val todayGoal: Int = 0,
    val atRisk: Boolean = false,
)
