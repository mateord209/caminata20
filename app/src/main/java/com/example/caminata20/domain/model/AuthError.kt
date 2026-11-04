package com.example.caminata20.domain.model

/**
 * Errores de autenticación con un mensaje listo para mostrar al usuario.
 * El mapeo desde Firebase vive en `data`; el dominio solo conoce estos casos.
 */
sealed interface AuthError {
    val userMessage: String

    data object InvalidEmail : AuthError {
        override val userMessage = "El correo ingresado no es válido"
    }

    data object WrongPassword : AuthError {
        override val userMessage = "Correo o contraseña incorrectos"
    }

    data object UserNotFound : AuthError {
        override val userMessage = "No existe una cuenta con ese correo"
    }

    data object Network : AuthError {
        override val userMessage = "No pudimos conectar. Revisá tu conexión e intentá de nuevo"
    }

    data object TooManyRequests : AuthError {
        override val userMessage = "Demasiados intentos. Esperá unos minutos e intentá de nuevo"
    }

    data object EmailInUse : AuthError {
        override val userMessage = "Ya existe una cuenta con ese correo"
    }

    data object WeakPassword : AuthError {
        override val userMessage = "La contraseña es muy débil"
    }

    data object UserDisabled : AuthError {
        override val userMessage = "Esta cuenta está deshabilitada"
    }

    data class Unknown(override val userMessage: String = DEFAULT_MESSAGE) : AuthError

    companion object {
        const val DEFAULT_MESSAGE = "Ocurrió un error inesperado. Intentá de nuevo"
    }
}
