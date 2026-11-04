package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.model.GoalPeriods
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.time.DayWindowCalculator
import javax.inject.Inject

/**
 * Calcula la racha de días consecutivos con la meta cumplida (HU-17).
 * Recorre como máximo 365 días hacia atrás desde hoy y delega las reglas
 * de cada día en [StreakDayEvaluator]. Cada día se evalúa con la meta que
 * tenía vigente según [goalPeriods] (Adenda C2).
 */
class CalculateStreakUseCase @Inject constructor(
    private val dayWindow: DayWindowCalculator,
    private val evaluator: StreakDayEvaluator,
) {
    operator fun invoke(
        records: List<PauseRecord>,
        currentGoal: Int,
        goalPeriods: List<GoalPeriod>,
        activeDays: Set<Int>,
        meetingDays: Set<Long>,
        now: Long,
    ): Int {
        val todayStart = dayWindow.startOfDay(now)
        var dayStart = todayStart
        var streak = 0

        repeat(MAX_DAYS) {
            val dayEnd = dayWindow.nextDayStart(dayStart)
            val completed = records.count {
                it.status == PauseStatus.COMPLETADA && it.timestamp in dayStart until dayEnd
            }
            val goal = GoalPeriods.goalFor(goalPeriods, dayStart, currentGoal).coerceAtLeast(1)
            val outcome = evaluator.evaluate(
                completed = completed,
                goal = goal,
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
