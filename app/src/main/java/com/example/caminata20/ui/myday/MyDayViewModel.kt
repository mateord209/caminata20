package com.example.caminata20.ui.myday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MyDayUiState(
    val name: String = "",
    val completed: Int = 0,
    val goal: Int = Profile.DEFAULT_GOAL,
    val totalMinutes: Int = 0,
    val todayRecords: List<PauseRecord> = emptyList(),
    val suggested: Exercise? = null,
) {
    val progress: Float get() = if (goal == 0) 0f else completed.toFloat() / goal
    val percent: Int get() = (progress * 100).toInt().coerceAtMost(100)
}

/** HU-12: Mi Día. Combina perfil (meta) + historial de hoy → progreso al instante. */
@HiltViewModel
class MyDayViewModel @Inject constructor(
    profile: ProfileRepository,
    history: PauseHistoryRepository,
    prefs: PreferencesRepository,
    local: LocalStateRepository,
) : ViewModel() {
    val state: StateFlow<MyDayUiState> = combine(
        profile.profile, history.today, prefs.pauseSettings, local.lastExerciseId,
    ) { p, today, settings, lastId ->
        val done = today.count { it.status == PauseStatus.COMPLETADA }
        MyDayUiState(
            name = p.name,
            completed = done,
            goal = p.dailyGoal,
            totalMinutes = done * settings.durationMin,
            todayRecords = today.sortedByDescending { it.timestamp },
            suggested = ExerciseCatalog.byId(lastId)?.takeIf { it.type == settings.type } ?: ExerciseCatalog.byType(settings.type).first(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyDayUiState())
}
