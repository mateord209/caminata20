package com.example.caminata20.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingPage(val emoji: String, val title: String, val body: String)

data class OnboardingUiState(val page: Int = 0, val finished: Boolean = false) {
    val isLast get() = page == PAGES.lastIndex
}

val PAGES = listOf(
    OnboardingPage("🌿", "Pausas que te cuidan", "Breakee te recuerda moverte, estirarte y descansar la vista durante tu jornada."),
    OnboardingPage("🔔", "Alertas a tu ritmo", "Elige tu tipo de pausa, horario e intervalo. Completa o pospón con un toque."),
    OnboardingPage("📊", "Mira tu progreso", "Fija una meta diaria y sigue tu racha en Mi Día y Estadísticas."),
)

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = OnboardingRepository(app)
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onPageChanged(page: Int) = _state.update { it.copy(page = page) }

    fun next() {
        if (_state.value.isLast) finish() else _state.update { it.copy(page = it.page + 1) }
    }

    /** Omitir o Comenzar: guarda la bandera y termina. */
    fun finish() {
        viewModelScope.launch {
            repo.markDone()
            _state.update { it.copy(finished = true) }
        }
    }
}
