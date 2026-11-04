package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.StreakResult
import com.example.caminata20.domain.repository.MeetingDayRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.time.DayWindowCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combina perfil, historial, horario y días de reunión para exponer la racha
 * del Dashboard; el cálculo base vive en [CalculateStreakUseCase] (HU-17).
 */
class ObserveStreakUseCase @Inject constructor(
    private val profiles: ProfileRepository,
    private val history: PauseHistoryRepository,
    private val preferences: PreferencesRepository,
    private val meetingDays: MeetingDayRepository,
    private val calculateStreak: CalculateStreakUseCase,
    private val dayWindow: DayWindowCalculator,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<StreakResult> = combine(
        profiles.profile,
        history.records,
        preferences.reminderSchedule,
        meetingDays.days(),
    ) { profile, records, schedule, days ->
        buildResult(profile.dailyGoal, records, schedule, days, clock.now())
    }

    private fun buildResult(
        goal: Int,
        records: List<PauseRecord>,
        schedule: ReminderScheduleConfig,
        days: Set<Long>,
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
            current = calculateStreak(records, goal, schedule.activeDays, days, now),
            todayCompleted = todayCompleted,
            todayGoal = todayGoal,
            atRisk = isActive && !isMeeting && todayCompleted < todayGoal,
        )
    }
}
