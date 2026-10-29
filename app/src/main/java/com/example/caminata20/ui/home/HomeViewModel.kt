package com.example.caminata20.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.WalkStats
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.WalkRepository
import com.example.caminata20.domain.usecase.WalkStatsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val stats: WalkStats = WalkStats(),
    /** HU-08: tipo de pausa actual, observado del Flow de preferencias (antes no se observaba). */
    val pauseSettings: PauseSettings = PauseSettings(),
)

/** Inicio (antes MainActivity XML): saludo y totales de caminata. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    profile: ProfileRepository,
    walks: WalkRepository,
    prefs: PreferencesRepository,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = combine(profile.profile, walks.sessions, prefs.pauseSettings) { p, sessions, settings ->
        HomeUiState(
            userName = p.name.ifBlank { p.email.ifBlank { "Invitado" } },
            stats = WalkStatsCalculator.compute(sessions),
            pauseSettings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
