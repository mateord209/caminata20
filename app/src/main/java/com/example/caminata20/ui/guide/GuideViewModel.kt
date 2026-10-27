package com.example.caminata20.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.repository.PauseHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GuideUiState(
    val exercise: Exercise? = null,
    val remainingSec: Int = 0,
    val running: Boolean = false,
    val completed: Boolean = false,
) {
    /** Paso actual según el tiempo transcurrido (pasos de igual duración). */
    val currentStep: Int
        get() {
            val ex = exercise ?: return 0
            if (completed) return ex.steps.size
            val elapsed = ex.durationSec - remainingSec
            val perStep = (ex.durationSec / ex.steps.size).coerceAtLeast(1)
            return (elapsed / perStep).coerceIn(0, ex.steps.lastIndex)
        }
    val clock: String get() = "%02d:%02d".format(remainingSec / 60, remainingSec % 60)
}

/** HU-16: pantalla guía con temporizador y pasos. */
@HiltViewModel
class GuideViewModel @Inject constructor(
    private val history: PauseHistoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(GuideUiState())
    val state: StateFlow<GuideUiState> = _state.asStateFlow()
    private var ticker: Job? = null

    fun load(id: String?) {
        if (_state.value.exercise != null) return
        val ex = ExerciseCatalog.byId(id) ?: ExerciseCatalog.all.first()
        _state.value = GuideUiState(ex, ex.durationSec)
        start()
    }

    fun toggle() = if (_state.value.running) pause() else start()

    private fun start() {
        if (_state.value.completed) return
        _state.update { it.copy(running = true) }
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive && _state.value.remainingSec > 0) {
                delay(1_000)
                _state.update { it.copy(remainingSec = it.remainingSec - 1) }
            }
            _state.update { it.copy(running = false) }
        }
    }

    private fun pause() {
        ticker?.cancel()
        _state.update { it.copy(running = false) }
    }

    fun complete() {
        val ex = _state.value.exercise ?: return
        if (_state.value.completed) return
        ticker?.cancel()
        _state.update { it.copy(running = false, completed = true, remainingSec = 0) }
        viewModelScope.launch {
            history.add(PauseRecord(System.currentTimeMillis(), ex.type, PauseStatus.COMPLETADA, ex.name))
        }
    }
}
