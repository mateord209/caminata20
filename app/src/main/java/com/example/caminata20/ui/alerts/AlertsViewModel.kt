package com.example.caminata20.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.Exercise
import com.example.caminata20.data.ExerciseCatalog
import com.example.caminata20.data.PauseType
import com.example.caminata20.data.SettingsRepository
import com.example.caminata20.data.SnoozeRepository
import com.example.caminata20.notifications.PauseActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val type: PauseType = PauseType.ESTIRAMIENTO,
    val exercise: Exercise? = null,
    val consecutiveSnoozes: Int = 0,
    val message: String? = null,
) {
    val showStreakWarning get() = consecutiveSnoozes > SnoozeRepository.STREAK_WARNING_AFTER
}

/** HU-03: pantalla Alertas con Completar / Posponer y advertencia de racha. */
class AlertsViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = SettingsRepository(app)
    private val snoozes = SnoozeRepository(app)
    private val actions = PauseActions(app)
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<AlertsUiState> = combine(
        settings.pauseSettings, settings.lastExerciseId, snoozes.consecutive, message,
    ) { s, lastId, count, msg ->
        AlertsUiState(
            type = s.type,
            exercise = ExerciseCatalog.byId(lastId)?.takeIf { it.type == s.type } ?: ExerciseCatalog.byType(s.type).first(),
            consecutiveSnoozes = count,
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())

    fun complete() = viewModelScope.launch {
        actions.complete()
        message.value = "Pausa registrada. El temporizador se reinició."
    }

    fun snooze() = viewModelScope.launch {
        actions.snooze()
        message.value = "Te avisaremos de nuevo en ${SnoozeRepository.SNOOZE_MINUTES} minutos."
    }
}
