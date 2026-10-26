package com.example.caminata20.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val goal: Int = ProfileRepository.DEFAULT_GOAL,
    val nameError: String? = null,
    val saved: Boolean = false,
) {
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProfileRepository(app)
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val p = repo.profile.first()
            _state.update { it.copy(name = p.name, email = p.email, goal = p.dailyGoal) }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null, saved = false) }

    /** La meta se guarda al instante para que Mi Día actualice la barra sin esperar. */
    fun onGoalChange(v: Int) {
        _state.update { it.copy(goal = v.coerceIn(ProfileRepository.GOAL_RANGE), saved = false) }
        viewModelScope.launch { repo.saveGoal(_state.value.goal) }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            repo.save(s.name, s.goal)
            _state.update { it.copy(saved = true) }
        }
    }
}
