package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import javax.inject.Inject

/** HU-10: cierra la sesión y limpia el estado local asociado al usuario. */
class SignOutUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val local: LocalStateRepository,
) {
    suspend operator fun invoke() {
        auth.signOut()
        local.clearUserScopedState()
    }
}
