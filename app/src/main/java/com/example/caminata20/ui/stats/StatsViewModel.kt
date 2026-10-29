package com.example.caminata20.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.usecase.DiaCumplimiento
import com.example.caminata20.domain.usecase.EstadisticasSemanalesUseCase
import com.example.caminata20.domain.usecase.RachaDiariaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val weekly: List<DiaCumplimiento> = emptyList(),
    val streak: Int = 0,
    val history: List<PauseRecord> = emptyList(),
    val shareText: String = "",
)

/** HU-04, HU-05, HU-17, HU-18: historial y estadísticas semanales. */
@HiltViewModel
class StatsViewModel @Inject constructor(
    profile: ProfileRepository,
    history: PauseHistoryRepository,
    prefs: PreferencesRepository,
) : ViewModel() {
    val state: StateFlow<StatsUiState> = combine(
        profile.profile,
        history.records,
        prefs.reminderSchedule,
    ) { perfil, registros, schedule ->
        val weekly = EstadisticasSemanalesUseCase.calcular(registros, perfil.dailyGoal, schedule.activeDays)
        val streak = RachaDiariaUseCase.calcular(registros, perfil.dailyGoal, schedule.activeDays)
        val completadas = registros.filter { it.status == PauseStatus.COMPLETADA }.take(30)
        StatsUiState(
            weekly = weekly,
            streak = streak,
            history = completadas,
            shareText = buildShareText(weekly, streak, perfil.dailyGoal),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    private fun buildShareText(weekly: List<DiaCumplimiento>, streak: Int, meta: Int): String {
        val lineas = weekly.joinToString("\n") { "${it.etiqueta}: ${it.completadas}/${it.esperadas} (${it.porcentaje}%)" }
        return "Mi semana con Breakee 🌿\nRacha: $streak días\nMeta diaria: $meta pausas\n\n$lineas"
    }
}
