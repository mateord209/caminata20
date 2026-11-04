package com.example.caminata20.domain.time

import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calcula los límites de un día calendario con una zona horaria inyectada (HU-17).
 * Evita sumar 86_400_000L fijo para no fallar en cambios de horario.
 */
@Singleton
class DayWindowCalculator @Inject constructor(
    private val timeZone: TimeZone,
) {
    fun startOfDay(ms: Long): Long = calendarAt(ms).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfDay(ms: Long): Long = nextDayStart(ms)

    fun nextDayStart(ms: Long): Long = calendarAt(ms).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    fun previousDayStart(ms: Long): Long = calendarAt(ms).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis

    fun dayOfWeek(ms: Long): Int = calendarAt(ms).get(Calendar.DAY_OF_WEEK)

    private fun calendarAt(ms: Long): Calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = ms
    }
}
