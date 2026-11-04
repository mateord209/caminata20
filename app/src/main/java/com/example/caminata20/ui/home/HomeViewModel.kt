package com.example.caminata20.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.StreakResult
import com.example.caminata20.domain.model.WalkStats
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.WalkRepository
import com.example.caminata20.domain.usecase.GetPhraseUseCase
import com.example.caminata20.domain.usecase.ObserveStreakUseCase
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
    val pauseSettings: PauseSettings = PauseSettings(),
    val motivationalPhrase: String = "",
    val meetingActive: Boolean = false,
    val intervalMin: Int = 60,
    val streak: StreakResult = StreakResult(),
)

/** Inicio: saludo, racha, caminatas y frase motivacional (HU-07, HU-08, HU-17). */
@HiltViewModel
class HomeViewModel @Inject constructor(
    profile: ProfileRepository,
    walks: WalkRepository,
    prefs: PreferencesRepository,
    local: LocalStateRepository,
    streakUseCase: ObserveStreakUseCase,
    phraseUseCase: GetPhraseUseCase,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        combine(profile.profile, walks.sessions, prefs.pauseSettings) { p, sessions, settings ->
            Triple(p, sessions, settings)
        },
        prefs.reminderSchedule,
        local.meetingUntil,
        streakUseCase(),
        phraseUseCase.current(),
    ) { triple, schedule, meetingUntil, streak, phrase ->
        val (p, sessions, settings) = triple
        HomeUiState(
            userName = p.name.ifBlank { p.email.ifBlank { "Invitado" } },
            stats = WalkStatsCalculator.compute(sessions),
            pauseSettings = settings,
            motivationalPhrase = phrase.text,
            meetingActive = meetingUntil != null && meetingUntil > System.currentTimeMillis(),
            intervalMin = schedule.intervalMin,
            streak = streak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
