package com.example.caminata20.ui.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.WalkLiveState
import com.example.caminata20.WalkTrackingService
import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Pantalla de caminata: refleja el estado en vivo del servicio en primer plano. */
@HiltViewModel
class WalkViewModel @Inject constructor() : ViewModel() {
    val state: StateFlow<WalkLiveState> = WalkTrackingService.live
}

data class WalkHistoryUiState(
    val sessions: List<WalkSession> = emptyList(),
    val selected: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
    val message: String? = null,
)

/** Historial de caminatas con selección múltiple y borrado confirmado. */
@HiltViewModel
class WalkHistoryViewModel @Inject constructor(
    private val repo: WalkRepository,
) : ViewModel() {
    private val ui = MutableStateFlow(WalkHistoryUiState())

    val state: StateFlow<WalkHistoryUiState> = combine(repo.sessions, ui) { sessions, u ->
        u.copy(sessions = sessions, selected = u.selected.intersect(sessions.map { it.id }.toSet()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalkHistoryUiState())

    fun toggle(id: String) = ui.update { it.copy(selected = if (id in it.selected) it.selected - id else it.selected + id) }

    fun requestDelete() = ui.update {
        if (it.selected.isEmpty()) it.copy(message = "No seleccionaste ninguno") else it.copy(confirmDelete = true)
    }

    fun dismissDelete() = ui.update { it.copy(confirmDelete = false) }
    fun consumeMessage() = ui.update { it.copy(message = null) }

    fun confirmDelete() {
        val ids = state.value.selected
        viewModelScope.launch {
            repo.delete(ids)
            ui.update { it.copy(selected = emptySet(), confirmDelete = false, message = "Eliminado(s) ${ids.size}") }
        }
    }
}

