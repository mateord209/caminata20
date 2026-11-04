package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.repository.MeetingDayRepository
import com.example.caminata20.domain.time.DayWindowCalculator
import javax.inject.Inject

/** Marca los días de Modo Reunión y poda los que tengan más de 400 días (HU-17). */
class MarkMeetingDayUseCase @Inject constructor(
    private val meetingDays: MeetingDayRepository,
    private val dayWindow: DayWindowCalculator,
) {
    suspend operator fun invoke(now: Long, until: Long?) {
        val todayStart = dayWindow.startOfDay(now)
        val days = mutableSetOf(todayStart)
        if (until != null) {
            val untilStart = dayWindow.startOfDay(until)
            if (untilStart > todayStart) days += untilStart
        }
        meetingDays.mark(days)
        meetingDays.prune(dayWindow.startOfDay(now - PRUNE_DAYS * DAY_MILLIS))
    }

    private companion object {
        const val PRUNE_DAYS = 400L
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
