package com.example.caminata20.data

import com.example.caminata20.data.repository.AuthErrorMapper
import com.example.caminata20.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {
    private val mapper = AuthErrorMapper()

    @Test
    fun `mapea todos los codigos conocidos de Firebase`() {
        val casos = mapOf(
            AuthErrorMapper.ERROR_INVALID_EMAIL to AuthError.InvalidEmail,
            AuthErrorMapper.ERROR_WRONG_PASSWORD to AuthError.WrongPassword,
            AuthErrorMapper.ERROR_INVALID_CREDENTIAL to AuthError.WrongPassword,
            AuthErrorMapper.ERROR_USER_NOT_FOUND to AuthError.UserNotFound,
            AuthErrorMapper.ERROR_NETWORK_REQUEST_FAILED to AuthError.Network,
            AuthErrorMapper.ERROR_TOO_MANY_REQUESTS to AuthError.TooManyRequests,
            AuthErrorMapper.ERROR_EMAIL_ALREADY_IN_USE to AuthError.EmailInUse,
            AuthErrorMapper.ERROR_WEAK_PASSWORD to AuthError.WeakPassword,
            AuthErrorMapper.ERROR_USER_DISABLED to AuthError.UserDisabled,
        )
        casos.forEach { (codigo, esperado) ->
            assertEquals("Código $codigo", esperado, mapper.mapCode(codigo))
        }
    }

    @Test
    fun `codigo nulo o desconocido usa el fallback`() {
        assertEquals(AuthError.Unknown("Fallback"), mapper.mapCode(null, "Fallback"))
        assertEquals(AuthError.Unknown("Fallback"), mapper.mapCode("ERROR_OTRO", "Fallback"))
    }

    @Test
    fun `codigo desconocido sin fallback usa el mensaje generico`() {
        assertEquals(AuthError.Unknown(), mapper.mapCode("ERROR_OTRO"))
        assertEquals(AuthError.DEFAULT_MESSAGE, mapper.mapCode("ERROR_OTRO").userMessage)
    }

    @Test
    fun `throwable que no es de Firebase usa el fallback generico`() {
        val error = mapper.map(RuntimeException("boom"))
        assertEquals(AuthError.Unknown(), error)
        assertEquals(AuthError.DEFAULT_MESSAGE, error.userMessage)
    }
}
