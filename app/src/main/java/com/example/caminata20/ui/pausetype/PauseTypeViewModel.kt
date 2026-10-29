package com.example.caminata20.ui.pausetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PauseTypeUiState(
    val selected: PauseType = PauseType.ESTIRAMIENTO,
    val durationMin: Int = 2,
    val saved: Boolean = false,
)

/** HU-08. */
@HiltViewModel
class PauseTypeViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PauseTypeUiState())
    val state: StateFlow<PauseTypeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val s = prefs.current()
            _state.update { it.copy(selected = s.type, durationMin = s.durationMin) }
        }
    }

    fun select(type: PauseType) = _state.update { it.copy(selected = type, saved = false) }
    fun setDuration(min: Int) = _state.update { it.copy(durationMin = min, saved = false) }

    fun save() {
        viewModelScope.launch {
            prefs.savePauseSettings(_state.value.selected, _state.value.durationMin)
            _state.update { it.copy(saved = true) }
        }
    }
}
