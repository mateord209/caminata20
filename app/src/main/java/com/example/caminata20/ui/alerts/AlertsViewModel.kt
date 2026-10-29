package com.example.caminata20.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val type: PauseType = PauseType.ESTIRAMIENTO,
    val exercise: Exercise? = null,
    val consecutiveSnoozes: Int = 0,
    val schedule: ReminderScheduleConfig = ReminderScheduleConfig(),
    val meetingActive: Boolean = false,
    val message: String? = null,
) {
    val showStreakWarning get() = consecutiveSnoozes > PauseActionsUseCase.STREAK_WARNING_AFTER
}

/** HU-03: pantalla Alertas con Completar / Posponer y advertencia de racha. */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    prefs: PreferencesRepository,
    local: LocalStateRepository,
    private val actions: PauseActionsUseCase,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<AlertsUiState> = combine(
        combine(prefs.pauseSettings, prefs.reminderSchedule) { s, schedule -> s to schedule },
        local.lastExerciseId,
        local.consecutiveSnoozes,
        local.meetingUntil,
        message,
    ) { settingsPair, lastId, count, meetingUntil, msg ->
        val (s, schedule) = settingsPair
        AlertsUiState(
            type = s.type,
            exercise = ExerciseCatalog.byId(lastId)?.takeIf { it.type == s.type } ?: ExerciseCatalog.byType(s.type).first(),
            consecutiveSnoozes = count,
            schedule = schedule,
            meetingActive = meetingUntil != null && meetingUntil > System.currentTimeMillis(),
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertsUiState())

    fun complete() = viewModelScope.launch {
        actions.complete()
        message.value = "Pausa registrada. El temporizador se reinició."
    }

    fun snooze() = viewModelScope.launch {
        actions.snooze()
        message.value = "Te avisaremos de nuevo en ${PauseActionsUseCase.SNOOZE_MINUTES} minutos."
    }
}
