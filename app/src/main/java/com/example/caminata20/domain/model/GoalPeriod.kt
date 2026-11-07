package com.example.caminata20.domain.model

/**
 * Tramo de vigencia de la meta diaria (HU-17, Adenda C2).
 * [fromDay] es el inicio del día (ms) desde el cual rige [goal].
 */
data class GoalPeriod(val fromDay: Long, val goal: Int)

/** Resuelve la meta que estaba vigente en un día a partir de su historial. */
object GoalPeriods {
    /**
     * Devuelve la meta del último periodo que empezó en o antes de [dayStart].
     * Sin periodos aplicables se usa [fallback] (la meta actual).
     */
    fun goalFor(periods: List<GoalPeriod>, dayStart: Long, fallback: Int): Int =
        periods.filter { it.fromDay <= dayStart }.maxByOrNull { it.fromDay }?.goal ?: fallback
}
