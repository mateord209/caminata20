package com.example.caminata20.domain

import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.time.DayWindowCalculator
import com.example.caminata20.domain.usecase.CalculateStreakUseCase
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.domain.usecase.ObserveStreakUseCase
import com.example.caminata20.domain.usecase.StreakDayEvaluator
import com.example.caminata20.fakes.FakeMeetingDayRepository
import com.example.caminata20.fakes.FakePauseHistoryRepository
import com.example.caminata20.fakes.FakePreferencesRepository
import com.example.caminata20.fakes.FakeProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** HU-17: la racha se arma combinando perfil, historial, horario y días de reunión. */
class ObserveStreakUseCaseTest {
    private val todos = setOf(
        Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
    )
    private val dayWindow = DayWindowCalculator(TimeZone.getDefault())
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

    private fun useCase(
        goal: Int = 3,
        activeDays: Set<Int> = todos,
        records: List<PauseRecord> = emptyList(),
        meetingDays: Set<Long> = emptySet(),
    ): ObserveStreakUseCase {
        val profile = FakeProfileRepository(Profile("Ana", "ana@x.com", goal))
        val history = FakePauseHistoryRepository().apply { items.value = records }
        val prefs = FakePreferencesRepository().apply {
            scheduleState.value = ReminderScheduleConfig(activeDays = activeDays)
        }
        val meetings = FakeMeetingDayRepository(meetingDays)
        val calculate = CalculateStreakUseCase(dayWindow, StreakDayEvaluator())
        return ObserveStreakUseCase(profile, history, prefs, meetings, calculate, dayWindow, Clock { ahora })
    }

    @Test
    fun `hoy en curso queda en riesgo y conserva la racha`() = runTest {
        val result = useCase(records = pausas(0, 1) + pausas(1, 3)).invoke().first()
        assertEquals(1, result.current)
        assertEquals(1, result.todayCompleted)
        assertEquals(3, result.todayGoal)
        assertTrue(result.atRisk)
    }

    @Test
    fun `hoy completo suma y deja de estar en riesgo`() = runTest {
        val result = useCase(records = pausas(0, 3) + pausas(1, 3)).invoke().first()
        assertEquals(2, result.current)
        assertEquals(3, result.todayCompleted)
        assertEquals(3, result.todayGoal)
        assertFalse(result.atRisk)
    }

    @Test
    fun `un dia inactivo no tiene meta ni riesgo`() = runTest {
        val result = useCase(
            activeDays = todos - dayWindow.dayOfWeek(ahora),
            records = pausas(1, 3),
        ).invoke().first()
        assertEquals(1, result.current)
        assertEquals(0, result.todayGoal)
        assertFalse(result.atRisk)
    }

    @Test
    fun `un dia de reunion no queda en riesgo`() = runTest {
        val result = useCase(
            records = pausas(1, 3),
            meetingDays = setOf(dayWindow.startOfDay(ahora)),
        ).invoke().first()
        assertEquals(1, result.current)
        assertEquals(0, result.todayCompleted)
        assertEquals(3, result.todayGoal)
        assertFalse(result.atRisk)
    }
}
