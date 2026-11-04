package com.example.caminata20.domain

import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.time.DayWindowCalculator
import com.example.caminata20.domain.usecase.CalculateStreakUseCase
import com.example.caminata20.domain.usecase.StreakDayEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** HU-17: la racha respeta la meta diaria, el día en curso, el horario y el Modo Reunión. */
class CalculateStreakUseCaseTest {
    private val todos = (1..7).toSet()
    private val dayWindow = DayWindowCalculator(TimeZone.getDefault())
    private val useCase = CalculateStreakUseCase(dayWindow, StreakDayEvaluator())
    private val ahora: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 3, 15, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun inicioDia(offset: Int): Long {
        var day = dayWindow.startOfDay(ahora)
        repeat(offset) { day = dayWindow.previousDayStart(day) }
        return day
    }

    private fun pausas(offset: Int, n: Int): List<PauseRecord> = (0 until n).map {
        PauseRecord(
            inicioDia(offset) + 9 * 3_600_000L + it * 60_000L,
            PauseType.ESTIRAMIENTO,
            PauseStatus.COMPLETADA,
            "",
        )
    }

    @Test
    fun `hoy incompleto no rompe la racha`() {
        val records = pausas(0, 1) + pausas(1, 3) + pausas(2, 3)
        assertEquals(2, useCase(records, 3, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `hoy completo suma a la racha`() {
        val records = pausas(0, 3) + pausas(1, 3)
        assertEquals(2, useCase(records, 3, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `ayer incompleto rompe la racha`() {
        val records = pausas(1, 3) + pausas(2, 1) + pausas(3, 3)
        assertEquals(1, useCase(records, 3, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `un dia inactivo es neutro`() {
        val diasActivos = todos - dayWindow.dayOfWeek(inicioDia(1))
        val records = pausas(1, 0) + pausas(2, 3)
        assertEquals(1, useCase(records, 3, emptyList(), diasActivos, emptySet(), ahora))
    }

    @Test
    fun `un dia de reunion es neutro`() {
        val records = pausas(1, 0) + pausas(2, 3)
        assertEquals(1, useCase(records, 3, emptyList(), todos, setOf(inicioDia(1)), ahora))
    }

    @Test
    fun `un dia de reunion con meta cumplida suma`() {
        val records = pausas(1, 3) + pausas(2, 3)
        assertEquals(2, useCase(records, 3, emptyList(), todos, setOf(inicioDia(1)), ahora))
    }

    @Test
    fun `la meta configurada cambia la racha`() {
        val records = pausas(1, 5) + pausas(2, 5)
        assertEquals(2, useCase(records, 5, emptyList(), todos, emptySet(), ahora))
        assertEquals(0, useCase(records, 6, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `sin registros la racha es cero`() {
        assertEquals(0, useCase(emptyList(), 8, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `una racha larga se cuenta completa`() {
        val records = (1..30).flatMap { pausas(it, 3) }
        assertEquals(30, useCase(records, 3, emptyList(), todos, emptySet(), ahora))
    }

    @Test
    fun `hoy con reunion no rompe la racha`() {
        val records = pausas(1, 3)
        assertEquals(1, useCase(records, 3, emptyList(), todos, setOf(inicioDia(0)), ahora))
    }

    @Test
    fun `solo las pausas completadas cuentan`() {
        val records = pausas(1, 2) + listOf(
            PauseRecord(inicioDia(1) + 12 * 3_600_000L, PauseType.ESTIRAMIENTO, PauseStatus.POSPUESTA, ""),
        )
        assertEquals(0, useCase(records, 3, emptyList(), todos, emptySet(), ahora))
    }

    // ---------- Adenda C2: meta por día ----------

    @Test
    fun `C2 subir la meta hoy no reevalua el dia de ayer`() {
        // Ayer se cumplió la meta 3; hoy la meta subió a 4 y todavía van 3 pausas.
        val records = pausas(1, 3) + pausas(0, 3)
        val periods = listOf(
            GoalPeriod(inicioDia(2), 3),
            GoalPeriod(inicioDia(1), 3),
            GoalPeriod(inicioDia(0), 4),
        )
        assertEquals(1, useCase(records, 4, periods, todos, emptySet(), ahora))
    }

    @Test
    fun `C2 sin cambio de meta la racha sigue contando`() {
        val records = pausas(1, 3) + pausas(0, 3)
        val periods = listOf(GoalPeriod(inicioDia(1), 3))
        assertEquals(2, useCase(records, 3, periods, todos, emptySet(), ahora))
    }
}
