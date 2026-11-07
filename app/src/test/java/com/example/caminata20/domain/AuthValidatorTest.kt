package com.example.caminata20.domain

import com.example.caminata20.domain.validation.AuthFieldError
import com.example.caminata20.domain.validation.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {
    private val validator = AuthValidator()

    @Test
    fun `login valido no tiene errores`() {
        val errors = validator.validateLogin("ana@x.com", "123456")
        assertFalse(errors.hasErrors)
        assertNull(errors.email)
        assertNull(errors.password)
    }

    @Test
    fun `login vacio exige correo y contrasena`() {
        val errors = validator.validateLogin("", "")
        assertEquals(AuthFieldError.EMAIL_REQUIRED, errors.email)
        assertEquals(AuthFieldError.PASSWORD_REQUIRED, errors.password)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun `correo sin arroba es invalido`() {
        val errors = validator.validateLogin("anax.com", "123456")
        assertEquals(AuthFieldError.EMAIL_INVALID, errors.email)
    }

    @Test
    fun `correo sin dominio o sin tld es invalido`() {
        assertEquals(AuthFieldError.EMAIL_INVALID, validator.validateLogin("ana@", "123456").email)
        assertEquals(AuthFieldError.EMAIL_INVALID, validator.validateLogin("ana@x", "123456").email)
        assertEquals(AuthFieldError.EMAIL_INVALID, validator.validateLogin("ana@x.c", "123456").email)
    }

    @Test
    fun `correo con espacios internos es invalido`() {
        assertEquals(AuthFieldError.EMAIL_INVALID, validator.validateLogin("ana perez@x.com", "123456").email)
    }

    @Test
    fun `contrasena menor a seis caracteres es invalida`() {
        val errors = validator.validateLogin("ana@x.com", "12345")
        assertEquals(AuthFieldError.PASSWORD_TOO_SHORT, errors.password)
    }

    @Test
    fun `registro exige nombre`() {
        val errors = validator.validateRegister("   ", "ana@x.com", "123456", "123456")
        assertEquals(AuthFieldError.NAME_REQUIRED, errors.name)
    }

    @Test
    fun `nombre con numeros es invalido`() {
        val errors = validator.validateRegister("Ana123", "ana@x.com", "123456", "123456")
        assertEquals(AuthFieldError.NAME_INVALID, errors.name)
    }

    @Test
    fun `nombre con tildes y ene es valido`() {
        val errors = validator.validateRegister("José Ñúñez", "jose@x.com", "123456", "123456")
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `confirmacion distinta muestra error`() {
        val errors = validator.validateRegister("Ana", "ana@x.com", "123456", "654321")
        assertEquals(AuthFieldError.CONFIRM_MISMATCH, errors.confirm)
        assertNull(errors.password)
    }

    @Test
    fun `confirmacion vacia muestra error`() {
        val errors = validator.validateRegister("Ana", "ana@x.com", "123456", "")
        assertEquals(AuthFieldError.CONFIRM_REQUIRED, errors.confirm)
    }

    @Test
    fun `registro valido no tiene errores`() {
        val errors = validator.validateRegister("Ana Pérez", "ana@x.com", "123456", "123456")
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `contrasena con espacios se conserva y es valida`() {
        val errors = validator.validateRegister("Ana", "ana@x.com", " 12345 ", " 12345 ")
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `recuperacion valida solo el correo`() {
        val invalido = validator.validateForgot("ana@")
        assertEquals(AuthFieldError.EMAIL_INVALID, invalido.email)
        assertTrue(invalido.hasErrors)

        val valido = validator.validateForgot("ana@x.com")
        assertFalse(valido.hasErrors)
    }
}
