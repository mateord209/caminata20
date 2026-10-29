package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.model.WalkStats
import java.util.Calendar

/** Totales de caminata (antes eran contadores en SharedPreferences, ahora se derivan del historial). */
object WalkStatsCalculator {
    fun compute(sessions: List<WalkSession>, now: Long = System.currentTimeMillis()): WalkStats {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis > now) add(Calendar.WEEK_OF_YEAR, -1)
        }
        val weekStart = cal.timeInMillis
        return WalkStats(
            totalSteps = sessions.sumOf { it.steps },
            totalKm = sessions.sumOf { it.km },
            weekKm = sessions.filter { it.timestamp >= weekStart }.sumOf { it.km },
        )
    }
}
