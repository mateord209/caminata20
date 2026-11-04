package com.example.caminata20.domain

import com.example.caminata20.domain.model.GoalPeriod
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.time.DayWindowCalculator
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.domain.usecase.SaveGoalUseCase
import com.example.caminata20.fakes.FakeGoalHistoryRepository
import com.example.caminata20.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** HU-17 (Adenda C2): guardar la meta registra desde qué día rige. */
class SaveGoalUseCaseTest {
    private val dayWindow = DayWindowCalculator(TimeZone.getDefault())
    private val ahora: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 3, 15, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun useCase(
        profile: FakeProfileRepository,
        goals: FakeGoalHistoryRepository,
    ) = SaveGoalUseCase(profile, goals, dayWindow, Clock { ahora })

    @Test
    fun `guarda la meta y registra el cambio desde hoy`() = runTest {
        val profile = FakeProfileRepository(Profile("Ana", "ana@x.com", 8))
        val goals = FakeGoalHistoryRepository()

        useCase(profile, goals)(8, 10)

        assertEquals(10, profile.state.value.dailyGoal)
        assertEquals(
            listOf(GoalPeriod(0L, 8), GoalPeriod(dayWindow.startOfDay(ahora), 10)),
            goals.items.value,
        )
    }

    @Test
    fun `no registra si la meta no cambio`() = runTest {
        val profile = FakeProfileRepository(Profile("Ana", "ana@x.com", 8))
        val goals = FakeGoalHistoryRepository()

        useCase(profile, goals)(8, 8)

        assertEquals(8, profile.state.value.dailyGoal)
        assertTrue(goals.changes.isEmpty())
    }

    @Test
    fun `acota la meta al rango permitido antes de registrar`() = runTest {
        val profile = FakeProfileRepository(Profile("Ana", "ana@x.com", 8))
        val goals = FakeGoalHistoryRepository()

        useCase(profile, goals)(8, 40)

        assertEquals(15, profile.state.value.dailyGoal)
        assertEquals(15, goals.changes.single().third)
    }
}
