package com.example.caminata20.domain

import com.example.caminata20.domain.usecase.SignOutUseCase
import com.example.caminata20.fakes.FakeAuthRepository
import com.example.caminata20.fakes.FakeLocalState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SignOutUseCaseTest {

    @Test
    fun `cierra la sesion y limpia el estado local del usuario`() = runTest {
        val auth = FakeAuthRepository()
        val local = FakeLocalState().apply { setOnboardingDone() }

        SignOutUseCase(auth, local)()

        assertTrue(auth.signedOut)
        assertTrue(local.userStateCleared)
        assertTrue(local.onboarding.value)
    }
}
