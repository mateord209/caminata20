package com.example.caminata20.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val goal: Int = Profile.DEFAULT_GOAL,
    val nameError: String? = null,
    val saved: Boolean = false,
    val signedOut: Boolean = false,
) {
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}

/** HU-12: Mi Perfil. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val auth: AuthRepository,
) : ViewModel() {
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
        _state.update { it.copy(goal = v.coerceIn(Profile.GOAL_RANGE), saved = false) }
        viewModelScope.launch { repo.saveGoal(_state.value.goal) }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            repo.saveName(s.name)
            repo.saveGoal(s.goal)
            _state.update { it.copy(saved = true) }
        }
    }

    fun signOut() {
        auth.signOut()
        _state.update { it.copy(signedOut = true) }
    }
}
