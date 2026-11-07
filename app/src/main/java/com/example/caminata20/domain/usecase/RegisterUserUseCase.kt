package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * HU-10: crea la cuenta y, si el registro fue exitoso, asegura el documento `users/{uid}`.
 * Un fallo al crear el perfil no revierte el registro: el perfil se reintenta en el próximo guardado.
 */
class RegisterUserUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val profile: ProfileRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String): Outcome<AuthUser> {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        return when (val result = auth.register(cleanName, cleanEmail, password)) {
            is Outcome.Success -> {
                try {
                    profile.ensureProfile(cleanName, cleanEmail)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // El registro ya se completó; el perfil se reintenta en el próximo guardado.
                }
                result
            }
            is Outcome.Failure -> result
        }
    }
}
