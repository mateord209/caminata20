package com.example.caminata20.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AiExerciseGenerator
import com.example.caminata20.domain.repository.AiRoutineRepository
import com.example.caminata20.domain.usecase.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiUiState(
    val loading: Boolean = false,
    val exercises: List<AiExercise> = emptyList(),
    val index: Int = 0,
    val message: String? = null,
    val finished: Boolean = false,
) {
    val current: AiExercise? get() = exercises.getOrNull(index)
    val isLast: Boolean get() = index == exercises.lastIndex
}

/** Rutina de estiramientos generada con Groq. */
@HiltViewModel
class AiExercisesViewModel @Inject constructor(
    private val generator: AiExerciseGenerator,
    private val routines: AiRoutineRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    fun generate() {
        _state.update { it.copy(loading = true, exercises = emptyList(), index = 0, message = null) }
        viewModelScope.launch {
            when (val r = generator.generate()) {
                is Outcome.Success -> _state.update { it.copy(loading = false, exercises = r.value) }
                is Outcome.Failure -> _state.update { it.copy(loading = false, message = r.message) }
            }
        }
    }

    fun next() {
        val s = _state.value
        if (s.exercises.isEmpty()) return
        if (!s.isLast) { _state.update { it.copy(index = it.index + 1) }; return }
        viewModelScope.launch {
            routines.add(AiRoutine(clock.now(), s.exercises))
            _state.update { it.copy(finished = true, message = "¡Rutina completa! Bien hecho 💪") }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    companion object {
        fun emojiForCategory(category: String) = when (category.lowercase()) {
            "cuello" -> "🙆"; "hombros" -> "🤸"; "espalda" -> "🧘"; "piernas" -> "🦵"
            "brazos" -> "💪"; "muñecas" -> "🤲"; else -> "🤾"
        }
    }
}

data class AiHistoryUiState(
    val routines: List<AiRoutine> = emptyList(),
    val selected: Set<String> = emptySet(),
    val confirmDelete: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class AiHistoryViewModel @Inject constructor(
    private val repo: AiRoutineRepository,
) : ViewModel() {
    private val ui = MutableStateFlow(AiHistoryUiState())

    val state: StateFlow<AiHistoryUiState> = combine(repo.routines, ui) { list, u ->
        u.copy(routines = list, selected = u.selected.intersect(list.map { it.id }.toSet()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiHistoryUiState())

    fun toggle(id: String) = ui.update { it.copy(selected = if (id in it.selected) it.selected - id else it.selected + id) }
    fun requestDelete() = ui.update {
        if (it.selected.isEmpty()) it.copy(message = "No seleccionaste ninguna") else it.copy(confirmDelete = true)
    }
    fun dismissDelete() = ui.update { it.copy(confirmDelete = false) }
    fun consumeMessage() = ui.update { it.copy(message = null) }

    fun confirmDelete() {
        val ids = state.value.selected
        viewModelScope.launch {
            repo.delete(ids)
            ui.update { it.copy(selected = emptySet(), confirmDelete = false, message = "Eliminada(s) ${ids.size}") }
        }
    }
}
