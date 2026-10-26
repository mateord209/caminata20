package com.example.caminata20.ui.pausetype

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.PauseType
import com.example.caminata20.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PauseTypeUiState(
    val selected: PauseType = PauseType.ESTIRAMIENTO,
    val durationMin: Int = 2,
    val saved: Boolean = false,
)

class PauseTypeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    private val _state = MutableStateFlow(PauseTypeUiState())
    val state: StateFlow<PauseTypeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val s = repo.current()
            _state.update { it.copy(selected = s.type, durationMin = s.durationMin) }
        }
    }

    fun select(type: PauseType) = _state.update { it.copy(selected = type, saved = false) }
    fun setDuration(min: Int) = _state.update { it.copy(durationMin = min, saved = false) }

    fun save() {
        viewModelScope.launch {
            repo.savePauseSettings(_state.value.selected, _state.value.durationMin)
            _state.update { it.copy(saved = true) }
        }
    }
}
