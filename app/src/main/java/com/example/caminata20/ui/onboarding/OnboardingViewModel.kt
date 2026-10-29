package com.example.caminata20.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.repository.LocalStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingPage(val emoji: String, val title: String, val body: String)

data class OnboardingUiState(val page: Int = 0, val finished: Boolean = false) {
    val isLast get() = page == PAGES.lastIndex
}

val PAGES = listOf(
    OnboardingPage("🌿", "Pausas que te cuidan", "Breakee te recuerda moverte, estirarte y descansar la vista durante tu jornada."),
    OnboardingPage("🔔", "Alertas a tu ritmo", "Elige tu tipo de pausa, horario e intervalo. Completa o pospón con un toque."),
    OnboardingPage("📊", "Mira tu progreso", "Fija una meta diaria y sigue tu racha en Mi Día y Estadísticas."),
)

/** HU-20. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val local: LocalStateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onPageChanged(page: Int) = _state.update { it.copy(page = page) }

    fun next() {
        if (_state.value.isLast) finish() else _state.update { it.copy(page = it.page + 1) }
    }

    /** Omitir o Comenzar: guarda la bandera y termina. */
    fun finish() {
        viewModelScope.launch {
            local.setOnboardingDone()
            _state.update { it.copy(finished = true) }
        }
    }
}
