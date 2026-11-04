package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.time.DayWindowCalculator
import javax.inject.Inject

/**
 * Calcula la racha de días consecutivos con la meta cumplida (HU-17).
 * Recorre como máximo 365 días hacia atrás desde hoy y delega las reglas
 * de cada día en [StreakDayEvaluator].
 */
class CalculateStreakUseCase @Inject constructor(
    private val dayWindow: DayWindowCalculator,
    private val evaluator: StreakDayEvaluator,
) {
    operator fun invoke(
        records: List<PauseRecord>,
        goal: Int,
        activeDays: Set<Int>,
        meetingDays: Set<Long>,
        now: Long,
    ): Int {
        val effectiveGoal = goal.coerceAtLeast(1)
        val todayStart = dayWindow.startOfDay(now)
        var dayStart = todayStart
        var streak = 0

        repeat(MAX_DAYS) {
            val dayEnd = dayWindow.nextDayStart(dayStart)
            val completed = records.count {
                it.status == PauseStatus.COMPLETADA && it.timestamp in dayStart until dayEnd
            }
            val outcome = evaluator.evaluate(
                completed = completed,
                goal = effectiveGoal,
                isActive = dayWindow.dayOfWeek(dayStart) in activeDays,
                isMeeting = dayStart in meetingDays,
                isToday = dayStart == todayStart,
            )
            when (outcome) {
                DayOutcome.COUNTED -> streak++
                DayOutcome.NEUTRAL -> Unit
                DayOutcome.BROKEN -> return streak
            }
            dayStart = dayWindow.previousDayStart(dayStart)
        }
        return streak
    }

    private companion object {
        const val MAX_DAYS = 365
    }
}
