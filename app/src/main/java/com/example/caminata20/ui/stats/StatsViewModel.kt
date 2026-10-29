package com.example.caminata20.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.usecase.DiaCumplimiento
import com.example.caminata20.domain.usecase.EstadisticasSemanalesUseCase
import com.example.caminata20.domain.usecase.RachaDiariaUseCase
import com.example.caminata20.domain.usecase.WeeklyShareSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val weekly: List<DiaCumplimiento> = emptyList(),
    val streak: Int = 0,
    /** Historial COMPLETADA ordenado por timestamp descendente y filtrado por [typeFilter]. */
    val history: List<PauseRecord> = emptyList(),
    val typeFilter: PauseType? = null,
    val shareText: String = "",
    val weeklyCompleted: Int = 0,
    val weeklyExpected: Int = 0,
    val weeklyPercent: Int = 0,
)

/** HU-04, HU-05, HU-09, HU-17, HU-18: historial y estadísticas semanales. */
@HiltViewModel
class StatsViewModel @Inject constructor(
    profile: ProfileRepository,
    history: PauseHistoryRepository,
    prefs: PreferencesRepository,
) : ViewModel() {
    private val typeFilter = MutableStateFlow<PauseType?>(null)

    val state: StateFlow<StatsUiState> = combine(
        profile.profile,
        history.records,
        prefs.reminderSchedule,
        typeFilter,
    ) { perfil, registros, schedule, filter ->
        val weekly = EstadisticasSemanalesUseCase.calcular(registros, perfil.dailyGoal, schedule.activeDays)
        val streak = RachaDiariaUseCase.calcular(registros, perfil.dailyGoal, schedule.activeDays)
        val allHistory = registros
            .filter { it.status == PauseStatus.COMPLETADA }
            .sortedByDescending { it.timestamp }
            .take(30)
        val visibles = allHistory.filter { filter == null || it.type == filter }
        val weeklyCompleted = weekly.sumOf { it.completadas }
        val weeklyExpected = weekly.sumOf { it.esperadas }
        val weeklyPercent =
            if (weeklyExpected == 0) 0 else (weeklyCompleted * 100 / weeklyExpected).coerceIn(0, 100)
        StatsUiState(
            weekly = weekly,
            streak = streak,
            history = visibles,
            typeFilter = filter,
            shareText = WeeklyShareSummary.generar(weekly, streak, perfil.dailyGoal),
            weeklyCompleted = weeklyCompleted,
            weeklyExpected = weeklyExpected,
            weeklyPercent = weeklyPercent,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    /** HU-09: filtra el historial por categoría; null muestra todas. */
    fun onFilterType(type: PauseType?) {
        typeFilter.value = type
    }
}
