package com.example.caminata20.domain.usecase

import javax.inject.Inject

/** Resultado de evaluar un día dentro del cálculo de racha (HU-17). */
enum class DayOutcome { COUNTED, NEUTRAL, BROKEN }

/**
 * Clasifica un día según la meta, el horario, el Modo Reunión y si es hoy.
 * El orden de las reglas es parte del contrato: un día completo siempre suma,
 * incluso si fue un día de reunión.
 */
class StreakDayEvaluator @Inject constructor() {
    fun evaluate(
        completed: Int,
        goal: Int,
        isActive: Boolean,
        isMeeting: Boolean,
        isToday: Boolean,
    ): DayOutcome = when {
        !isActive -> DayOutcome.NEUTRAL
        completed >= goal -> DayOutcome.COUNTED
        isToday -> DayOutcome.NEUTRAL
        isMeeting -> DayOutcome.NEUTRAL
        else -> DayOutcome.BROKEN
    }
}
