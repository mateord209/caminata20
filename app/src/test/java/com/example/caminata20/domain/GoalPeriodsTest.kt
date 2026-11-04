package com.example.caminata20.domain

import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.model.GoalPeriods
import org.junit.Assert.assertEquals
import org.junit.Test

/** HU-17 (Adenda C2): la meta de cada día sale de su historial. */
class GoalPeriodsTest {
    @Test
    fun `sin historial usa la meta actual`() {
        assertEquals(8, GoalPeriods.goalFor(emptyList(), 1_000L, 8))
    }

    @Test
    fun `usa el ultimo periodo que empezo en o antes del dia`() {
        val periods = listOf(GoalPeriod(0L, 8), GoalPeriod(1_000L, 5), GoalPeriod(2_000L, 6))
        assertEquals(8, GoalPeriods.goalFor(periods, 999L, 9))
        assertEquals(5, GoalPeriods.goalFor(periods, 1_500L, 9))
        assertEquals(6, GoalPeriods.goalFor(periods, 2_000L, 9))
        assertEquals(6, GoalPeriods.goalFor(periods, 3_000L, 9))
    }

    @Test
    fun `un cambio de meta solo aplica desde su dia`() {
        val cambio = 1_700_000_000_000L
        val periods = listOf(GoalPeriod(0L, 3), GoalPeriod(cambio, 4))
        assertEquals(3, GoalPeriods.goalFor(periods, cambio - 1, 4))
        assertEquals(4, GoalPeriods.goalFor(periods, cambio, 4))
    }

    @Test
    fun `no depende del orden de la lista`() {
        val periods = listOf(GoalPeriod(2_000L, 6), GoalPeriod(0L, 8), GoalPeriod(1_000L, 5))
        assertEquals(5, GoalPeriods.goalFor(periods, 1_500L, 9))
    }
}
