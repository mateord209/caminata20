package com.example.caminata20.domain

import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.usecase.WalkStatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class WalkStatsCalculatorTest {
    private fun ts(y: Int, m: Int, d: Int) = Calendar.getInstance().apply { set(y, m - 1, d, 10, 0, 0) }.timeInMillis

    @Test
    fun `suma totales y solo cuenta en la semana las caminatas desde el lunes`() {
        val now = ts(2026, 10, 28) // miércoles
        val sessions = listOf(
            WalkSession(ts(2026, 10, 26), 1000, 0.8),  // lunes, esta semana
            WalkSession(ts(2026, 10, 27), 500, 0.4),   // martes, esta semana
            WalkSession(ts(2026, 10, 20), 2000, 1.5),  // semana anterior
        )
        val s = WalkStatsCalculator.compute(sessions, now)
        assertEquals(3500, s.totalSteps)
        assertEquals(2.7, s.totalKm, 1e-9)
        assertEquals(1.2, s.weekKm, 1e-9)
    }
}
