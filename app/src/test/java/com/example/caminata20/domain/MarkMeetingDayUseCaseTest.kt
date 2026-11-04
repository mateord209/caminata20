package com.example.caminata20.domain

import com.example.caminata20.domain.time.DayWindowCalculator
import com.example.caminata20.domain.usecase.MarkMeetingDayUseCase
import com.example.caminata20.fakes.FakeMeetingDayRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** HU-17: marcado y poda de los días con Modo Reunión. */
class MarkMeetingDayUseCaseTest {
    private val dayWindow = DayWindowCalculator(TimeZone.getDefault())
    private val ahora: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 3, 15, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun hora(dia: Int, hora: Int, minuto: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = ahora
            set(Calendar.DAY_OF_MONTH, dia)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `marca el dia actual y poda los antiguos`() = runTest {
        val viejo = dayWindow.startOfDay(ahora - 500L * 24 * 60 * 60 * 1000)
        val repo = FakeMeetingDayRepository(setOf(viejo))
        val useCase = MarkMeetingDayUseCase(repo, dayWindow)

        useCase(ahora, null)

        assertEquals(setOf(dayWindow.startOfDay(ahora)), repo.items.value)
        assertEquals(dayWindow.startOfDay(ahora - 400L * 24 * 60 * 60 * 1000), repo.prunedBefore)
    }

    @Test
    fun `si la reunion cruza medianoche marca los dos dias`() = runTest {
        val repo = FakeMeetingDayRepository()
        val useCase = MarkMeetingDayUseCase(repo, dayWindow)
        val inicio = hora(3, 23, 30)
        val fin = hora(4, 1, 0)

        useCase(inicio, fin)

        assertEquals(setOf(dayWindow.startOfDay(inicio), dayWindow.startOfDay(fin)), repo.items.value)
    }

    @Test
    fun `si la reunion termina el mismo dia solo marca hoy`() = runTest {
        val repo = FakeMeetingDayRepository()
        val useCase = MarkMeetingDayUseCase(repo, dayWindow)
        val inicio = hora(3, 9, 0)
        val fin = hora(3, 10, 0)

        useCase(inicio, fin)

        assertEquals(setOf(dayWindow.startOfDay(inicio)), repo.items.value)
    }

    @Test
    fun `la poda conserva los dias recientes`() = runTest {
        val reciente = dayWindow.startOfDay(ahora - 10L * 24 * 60 * 60 * 1000)
        val viejo = dayWindow.startOfDay(ahora - 500L * 24 * 60 * 60 * 1000)
        val repo = FakeMeetingDayRepository(setOf(reciente, viejo))
        val useCase = MarkMeetingDayUseCase(repo, dayWindow)

        useCase(ahora, null)

        assertTrue(reciente in repo.items.value)
        assertTrue(viejo !in repo.items.value)
    }
}
