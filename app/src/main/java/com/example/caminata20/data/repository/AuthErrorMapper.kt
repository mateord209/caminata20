package com.example.caminata20.data.repository

import com.example.caminata20.domain.model.AuthError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Único punto que conoce los códigos de Firebase Auth (HU-10).
 * Devuelve errores de dominio con mensajes en español.
 */
@Singleton
class AuthErrorMapper @Inject constructor() {

    fun mapCode(code: String?, fallback: String = AuthError.DEFAULT_MESSAGE): AuthError = when (code) {
        ERROR_INVALID_EMAIL -> AuthError.InvalidEmail
        ERROR_WRONG_PASSWORD -> AuthError.WrongPassword
        ERROR_INVALID_CREDENTIAL -> AuthError.WrongPassword
        ERROR_USER_NOT_FOUND -> AuthError.UserNotFound
        ERROR_NETWORK_REQUEST_FAILED -> AuthError.Network
        ERROR_TOO_MANY_REQUESTS -> AuthError.TooManyRequests
        ERROR_EMAIL_ALREADY_IN_USE -> AuthError.EmailInUse
        ERROR_WEAK_PASSWORD -> AuthError.WeakPassword
        ERROR_USER_DISABLED -> AuthError.UserDisabled
        else -> AuthError.Unknown(fallback)
    }

    fun map(throwable: Throwable): AuthError {
        if (throwable is FirebaseNetworkException || throwable.cause is FirebaseNetworkException) {
            return AuthError.Network
        }
        val code = (throwable as? FirebaseAuthException)?.errorCode
            ?: (throwable.cause as? FirebaseAuthException)?.errorCode
        return mapCode(code)
    }

    companion object {
        const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val ERROR_INVALID_CREDENTIAL = "ERROR_INVALID_CREDENTIAL"
        const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val ERROR_NETWORK_REQUEST_FAILED = "ERROR_NETWORK_REQUEST_FAILED"
        const val ERROR_TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS"
        const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
        const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        const val ERROR_USER_DISABLED = "ERROR_USER_DISABLED"
    }
}
