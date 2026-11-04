package com.example.caminata20.domain

import com.example.caminata20.domain.time.DayWindowCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** HU-17: límites de día calculados con una zona horaria inyectada. */
class DayWindowCalculatorTest {
    private val zona: TimeZone = TimeZone.getTimeZone("America/Bogota")
    private val calculator = DayWindowCalculator(zona)

    private fun fecha(dia: Int, hora: Int, minuto: Int, segundo: Int = 0, millis: Int = 0): Long =
        Calendar.getInstance(zona).apply {
            clear()
            set(2026, Calendar.NOVEMBER, dia, hora, minuto, segundo)
            set(Calendar.MILLISECOND, millis)
        }.timeInMillis

    @Test
    fun `inicio del dia`() {
        assertEquals(fecha(3, 0, 0), calculator.startOfDay(fecha(3, 15, 30, 45, 123)))
    }

    @Test
    fun `fin del dia coincide con el inicio del siguiente`() {
        val momento = fecha(3, 15, 0)
        assertEquals(fecha(4, 0, 0), calculator.endOfDay(momento))
        assertEquals(fecha(4, 0, 0), calculator.nextDayStart(momento))
    }

    @Test
    fun `inicio del dia anterior`() {
        assertEquals(fecha(2, 0, 0), calculator.previousDayStart(fecha(3, 8, 0)))
    }

    @Test
    fun `dia de la semana`() {
        assertEquals(Calendar.TUESDAY, calculator.dayOfWeek(fecha(3, 12, 0)))
    }
}
