package com.example.caminata20.ui.myday

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.Exercise
import com.example.caminata20.data.ExerciseCatalog
import com.example.caminata20.data.PauseHistoryRepository
import com.example.caminata20.data.PauseRecord
import com.example.caminata20.data.PauseStatus
import com.example.caminata20.data.ProfileRepository
import com.example.caminata20.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MyDayUiState(
    val name: String = "",
    val completed: Int = 0,
    val goal: Int = ProfileRepository.DEFAULT_GOAL,
    val totalMinutes: Int = 0,
    val todayRecords: List<PauseRecord> = emptyList(),
    val suggested: Exercise? = null,
) {
    val progress: Float get() = if (goal == 0) 0f else completed.toFloat() / goal
    val percent: Int get() = (progress * 100).toInt().coerceAtMost(100)
}

/** HU-12: Dashboard Mi Día. Combina perfil (meta) + historial de hoy → progreso al instante. */
class MyDayViewModel(app: Application) : AndroidViewModel(app) {
    private val profileRepo = ProfileRepository(app)
    private val historyRepo = PauseHistoryRepository(app)
    private val settingsRepo = SettingsRepository(app)

    val state: StateFlow<MyDayUiState> = combine(
        profileRepo.profile, historyRepo.today, settingsRepo.pauseSettings, settingsRepo.lastExerciseId,
    ) { profile, today, settings, lastId ->
        val done = today.count { it.status == PauseStatus.COMPLETADA }
        MyDayUiState(
            name = profile.name,
            completed = done,
            goal = profile.dailyGoal,
            totalMinutes = done * settings.durationMin,
            todayRecords = today.sortedByDescending { it.timestamp },
            suggested = ExerciseCatalog.byId(lastId)?.takeIf { it.type == settings.type } ?: ExerciseCatalog.byType(settings.type).first(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyDayUiState())
}
