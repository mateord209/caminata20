package com.example.caminata20.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { ONBOARDING, LOGIN, HOME }

data class SplashUiState(val destination: SplashDestination? = null)

/** Decide el destino inicial: onboarding (HU-20, solo la primera vez) → login → inicio. */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val local: LocalStateRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_MS)
            val dest = when {
                !local.onboardingDone.first() -> SplashDestination.ONBOARDING
                auth.currentUserOrNull() == null -> SplashDestination.LOGIN
                else -> SplashDestination.HOME
            }
            _state.value = SplashUiState(dest)
        }
    }

    companion object {
        const val SPLASH_MS = 1_200L
    }
}
