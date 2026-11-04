package com.example.caminata20.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.usecase.SaveGoalUseCase
import com.example.caminata20.domain.usecase.SignOutUseCase
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
    private val saveGoalUseCase: SaveGoalUseCase,
    private val signOutUseCase: SignOutUseCase,
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

    /** La meta se guarda al instante y queda registrada para los días ya cerrados (HU-17). */
    fun onGoalChange(v: Int) {
        val old = _state.value.goal
        val goal = v.coerceIn(Profile.GOAL_RANGE)
        _state.update { it.copy(goal = goal, saved = false) }
        viewModelScope.launch { saveGoalUseCase(old, goal) }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            // La meta ya se persiste (y se registra en el historial) al cambiarla.
            repo.saveName(s.name)
            _state.update { it.copy(saved = true) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                signOutUseCase()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Aunque falle la limpieza local, la sesión ya se cerró y hay que volver al login.
            }
            _state.update { it.copy(signedOut = true) }
        }
    }
}
