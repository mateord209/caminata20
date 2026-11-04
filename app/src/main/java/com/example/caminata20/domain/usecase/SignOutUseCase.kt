package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.ReminderScheduler
import javax.inject.Inject

/**
 * HU-10: cierra la sesión, cancela las alarmas del usuario y limpia el estado local.
 * Cancelar antes de limpiar evita que una notificación programada despierte la app
 * después del logout.
 */
class SignOutUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val local: LocalStateRepository,
    private val pauseScheduler: PauseScheduler,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke() {
        auth.signOut()
        pauseScheduler.cancel()
        pauseScheduler.cancelHydration()
        reminderScheduler.cancel()
        local.clearUserScopedState()
    }
}
