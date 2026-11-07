package com.example.caminata20.domain.validation

import javax.inject.Inject

/** Errores de validación de un campo de autenticación, con mensaje en español. */
enum class AuthFieldError(val message: String) {
    NAME_REQUIRED("Ingresá tu nombre"),
    NAME_INVALID("El nombre solo puede contener letras y espacios"),
    EMAIL_REQUIRED("Ingresá tu correo"),
    EMAIL_INVALID("Ingresá un correo válido"),
    PASSWORD_REQUIRED("Ingresá tu contraseña"),
    PASSWORD_TOO_SHORT("La contraseña debe tener al menos 6 caracteres"),
    CONFIRM_REQUIRED("Repetí tu contraseña"),
    CONFIRM_MISMATCH("Las contraseñas no coinciden"),
}

/** Errores por campo de un formulario de autenticación. */
data class AuthFormErrors(
    val name: AuthFieldError? = null,
    val email: AuthFieldError? = null,
    val password: AuthFieldError? = null,
    val confirm: AuthFieldError? = null,
) {
    val hasErrors: Boolean
        get() = name != null || email != null || password != null || confirm != null
}

/**
 * Validación pura de los formularios de autenticación (HU-10).
 * No depende de Android ni de Firebase.
 */
class AuthValidator @Inject constructor() {

    fun validateLogin(email: String, password: String): AuthFormErrors = AuthFormErrors(
        email = validateEmail(email),
        password = validatePassword(password),
    )

    fun validateRegister(name: String, email: String, password: String, confirm: String): AuthFormErrors =
        AuthFormErrors(
            name = validateName(name),
            email = validateEmail(email),
            password = validatePassword(password),
            confirm = validateConfirm(password, confirm),
        )

    fun validateForgot(email: String): AuthFormErrors = AuthFormErrors(email = validateEmail(email))

    private fun validateName(name: String): AuthFieldError? {
        val value = name.trim()
        return when {
            value.isEmpty() -> AuthFieldError.NAME_REQUIRED
            !NAME_REGEX.matches(value) -> AuthFieldError.NAME_INVALID
            else -> null
        }
    }

    private fun validateEmail(email: String): AuthFieldError? {
        val value = email.trim()
        return when {
            value.isEmpty() -> AuthFieldError.EMAIL_REQUIRED
            value.any(Char::isWhitespace) -> AuthFieldError.EMAIL_INVALID
            !EMAIL_REGEX.matches(value) -> AuthFieldError.EMAIL_INVALID
            else -> null
        }
    }

    private fun validatePassword(password: String): AuthFieldError? = when {
        password.isEmpty() -> AuthFieldError.PASSWORD_REQUIRED
        password.length < MIN_PASSWORD_LENGTH -> AuthFieldError.PASSWORD_TOO_SHORT
        else -> null
    }

    private fun validateConfirm(password: String, confirm: String): AuthFieldError? = when {
        confirm.isEmpty() -> AuthFieldError.CONFIRM_REQUIRED
        confirm != password -> AuthFieldError.CONFIRM_MISMATCH
        else -> null
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6

        /** Solo letras, tildes, ñ y espacios (misma regla de la pantalla anterior). */
        val NAME_REGEX = Regex("[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]*")

        /** Correo con usuario, dominio y TLD alfabético de al menos 2 letras. */
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
