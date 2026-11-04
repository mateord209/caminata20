package com.example.caminata20.domain

import com.example.caminata20.domain.usecase.DayOutcome
import com.example.caminata20.domain.usecase.StreakDayEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

/** HU-17: reglas de clasificación de un día para la racha. */
class StreakDayEvaluatorTest {
    private val evaluator = StreakDayEvaluator()

    @Test
    fun `un dia inactivo es neutro aunque no tenga pausas`() {
        assertEquals(DayOutcome.NEUTRAL, evaluator.evaluate(0, 8, isActive = false, isMeeting = false, isToday = false))
    }

    @Test
    fun `un dia activo con la meta cumplida cuenta`() {
        assertEquals(DayOutcome.COUNTED, evaluator.evaluate(8, 8, isActive = true, isMeeting = false, isToday = false))
    }

    @Test
    fun `el dia en curso sin meta es neutro`() {
        assertEquals(DayOutcome.NEUTRAL, evaluator.evaluate(3, 8, isActive = true, isMeeting = false, isToday = true))
    }

    @Test
    fun `un dia de reunion sin meta es neutro`() {
        assertEquals(DayOutcome.NEUTRAL, evaluator.evaluate(0, 8, isActive = true, isMeeting = true, isToday = false))
    }

    @Test
    fun `un dia pasado activo sin meta rompe la racha`() {
        assertEquals(DayOutcome.BROKEN, evaluator.evaluate(7, 8, isActive = true, isMeeting = false, isToday = false))
    }

    @Test
    fun `un dia de reunion con la meta cumplida cuenta`() {
        assertEquals(DayOutcome.COUNTED, evaluator.evaluate(8, 8, isActive = true, isMeeting = true, isToday = false))
    }
}
