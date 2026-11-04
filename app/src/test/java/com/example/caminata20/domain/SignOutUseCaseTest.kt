package com.example.caminata20.domain

import com.example.caminata20.domain.usecase.SignOutUseCase
import com.example.caminata20.fakes.FakeAuthRepository
import com.example.caminata20.fakes.FakeLocalState
import com.example.caminata20.fakes.FakePauseScheduler
import com.example.caminata20.fakes.FakeReminderScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SignOutUseCaseTest {

    @Test
    fun `cierra la sesion, cancela alarmas y limpia el estado local del usuario`() = runTest {
        val auth = FakeAuthRepository()
        val local = FakeLocalState().apply { setOnboardingDone() }
        val pause = FakePauseScheduler()
        val reminder = FakeReminderScheduler()

        SignOutUseCase(auth, local, pause, reminder)()

        assertTrue(auth.signedOut)
        assertTrue(pause.cancelled)
        assertTrue(pause.hydrationCancelled)
        assertTrue(reminder.cancelled)
        assertTrue(local.userStateCleared)
        assertTrue(local.onboarding.value)
    }
}
