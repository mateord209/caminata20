package com.example.caminata20.domain

import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.usecase.RegisterUserUseCase
import com.example.caminata20.fakes.FakeAuthRepository
import com.example.caminata20.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RegisterUserUseCaseTest {

    @Test
    fun `registro exitoso crea el perfil con los datos normalizados`() = runTest {
        val auth = FakeAuthRepository()
        val profile = FakeProfileRepository()
        val result = RegisterUserUseCase(auth, profile)("  Ana Pérez ", " ana@x.com ", "123456")

        assertTrue(result is Outcome.Success)
        assertEquals("Ana Pérez" to "ana@x.com", profile.ensuredProfile)
    }

    @Test
    fun `si falla auth no intenta crear el perfil`() = runTest {
        val auth = FakeAuthRepository(Outcome.Failure("Ya existe una cuenta con ese correo"))
        val profile = FakeProfileRepository()
        val result = RegisterUserUseCase(auth, profile)("Ana", "ana@x.com", "123456")

        assertEquals("Ya existe una cuenta con ese correo", (result as Outcome.Failure).message)
        assertNull(profile.ensuredProfile)
    }

    @Test
    fun `si falla el perfil el registro no se revierte`() = runTest {
        val auth = FakeAuthRepository(Outcome.Success(AuthUser("u1", "Ana", "ana@x.com")))
        val profile = FakeProfileRepository().apply { ensureFailure = IOException("sin red") }
        val result = RegisterUserUseCase(auth, profile)("Ana", "ana@x.com", "123456")

        assertTrue(result is Outcome.Success)
        assertEquals("u1", (result as Outcome.Success).value.uid)
        assertEquals("Ana" to "ana@x.com", profile.ensuredProfile)
    }
}
