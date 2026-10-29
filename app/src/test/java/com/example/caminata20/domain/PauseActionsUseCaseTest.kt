package com.example.caminata20.domain

import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import com.example.caminata20.fakes.FakeLocalState
import com.example.caminata20.fakes.FakeNotifications
import com.example.caminata20.fakes.FakePauseHistoryRepository
import com.example.caminata20.fakes.FakePauseScheduler
import com.example.caminata20.fakes.FakePreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseActionsUseCaseTest {
    private val prefs = FakePreferencesRepository()
    private val history = FakePauseHistoryRepository()
    private val local = FakeLocalState()
    private val scheduler = FakePauseScheduler()
    private val notifications = FakeNotifications()
    private val useCase = PauseActionsUseCase(prefs, history, local, scheduler, notifications, Clock { 1_000L })

    @Test
    fun `completar registra la pausa con fecha, reinicia contador y temporizador`() = runTest {
        prefs.savePauseSettings(PauseType.HIDRATACION, 2)
        local.setLastExercise("hid_01")
        local.incrementSnoozes()

        useCase.complete()

        val r = history.items.value.single()
        assertEquals(1_000L, r.timestamp)
        assertEquals(PauseStatus.COMPLETADA, r.status)
        assertEquals(PauseType.HIDRATACION, r.type)
        assertEquals("Vaso de agua", r.exerciseName)
        assertEquals(0, local.snoozes.value)
        assertEquals(1, scheduler.restarts)
        assertEquals(1, notifications.dismissed)
    }

    @Test
    fun `posponer reprograma en 5 minutos y advierte despues de 3 seguidas`() = runTest {
        repeat(3) { assertFalse(useCase.snooze()) }
        assertTrue(useCase.snooze())

        assertEquals(listOf(5, 5, 5, 5), scheduler.scheduled)
        assertEquals(listOf(4), notifications.warnings)
        assertEquals(4, history.items.value.count { it.status == PauseStatus.POSPUESTA })
    }

    @Test
    fun `completar despues de posponer reinicia la racha de posposiciones`() = runTest {
        repeat(3) { useCase.snooze() }
        useCase.complete()
        assertFalse(useCase.snooze())
        assertEquals(1, local.snoozes.value)
    }
}
