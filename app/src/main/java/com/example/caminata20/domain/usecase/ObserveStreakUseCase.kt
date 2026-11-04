package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.StreakResult
import com.example.caminata20.domain.repository.GoalHistoryRepository
import com.example.caminata20.domain.repository.MeetingDayRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.time.DayWindowCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combina perfil, historial, horario, días de reunión e historial de metas
 * para exponer la racha del Dashboard; el cálculo base vive en
 * [CalculateStreakUseCase] (HU-17). La meta vigente hoy sigue saliendo del perfil.
 */
class ObserveStreakUseCase @Inject constructor(
    private val profiles: ProfileRepository,
    private val history: PauseHistoryRepository,
    private val preferences: PreferencesRepository,
    private val meetingDays: MeetingDayRepository,
    private val goalHistory: GoalHistoryRepository,
    private val calculateStreak: CalculateStreakUseCase,
    private val dayWindow: DayWindowCalculator,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<StreakResult> = combine(
        profiles.profile,
        history.records,
        preferences.reminderSchedule,
        meetingDays.days(),
        goalHistory.periods(),
    ) { profile, records, schedule, days, periods ->
        buildResult(profile.dailyGoal, records, schedule, days, periods, clock.now())
    }

    private fun buildResult(
        goal: Int,
        records: List<PauseRecord>,
        schedule: ReminderScheduleConfig,
        days: Set<Long>,
        goalPeriods: List<GoalPeriod>,
        now: Long,
    ): StreakResult {
        val todayStart = dayWindow.startOfDay(now)
        val tomorrowStart = dayWindow.nextDayStart(now)
        val todayCompleted = records.count {
            it.status == PauseStatus.COMPLETADA && it.timestamp in todayStart until tomorrowStart
        }
        val isActive = dayWindow.dayOfWeek(now) in schedule.activeDays
        val isMeeting = todayStart in days
        val todayGoal = if (isActive) goal.coerceAtLeast(1) else 0
        return StreakResult(
            current = calculateStreak(records, goal, goalPeriods, schedule.activeDays, days, now),
            todayCompleted = todayCompleted,
            todayGoal = todayGoal,
            atRisk = isActive && !isMeeting && todayCompleted < todayGoal,
        )
    }
}
