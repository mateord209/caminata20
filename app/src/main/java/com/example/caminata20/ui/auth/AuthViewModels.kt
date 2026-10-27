package com.example.caminata20.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val loggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v, message = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun signIn() {
        val s = _state.value
        val email = s.email.trim()
        val pass = s.password.trim()
        if (email.isEmpty() || pass.isEmpty()) {
            _state.update { it.copy(message = "Completá todos los campos") }
            return
        }
        run { auth.signInWithEmail(email, pass) }
    }

    fun signInWithGoogle(idToken: String?) {
        if (idToken == null) {
            _state.update { it.copy(message = "Google Sign-In cancelado o falló") }
            return
        }
        run { auth.signInWithGoogle(idToken) }
    }

    fun onGoogleError(code: Int) = _state.update { it.copy(message = "Google Sign-In cancelado o falló (código $code)") }

    private fun run(block: suspend () -> Outcome<*>) {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            when (val r = block()) {
                is Outcome.Success -> _state.update { it.copy(loading = false, loggedIn = true) }
                is Outcome.Failure -> _state.update { it.copy(loading = false, message = r.message) }
            }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val registered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    /** Solo letras, tildes, ñ y espacios (misma regla que la pantalla XML anterior). */
    fun onName(v: String) { if (NAME_REGEX.matches(v)) _state.update { it.copy(name = v, message = null) } }
    fun onEmail(v: String) = _state.update { it.copy(email = v, message = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, message = null) }
    fun onConfirm(v: String) = _state.update { it.copy(confirm = v, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun register() {
        val s = _state.value
        val name = s.name.trim(); val email = s.email.trim(); val pass = s.password.trim(); val confirm = s.confirm.trim()
        val error = when {
            name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() -> "Completá todos los campos"
            pass.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            pass != confirm -> "Las contraseñas no coinciden"
            else -> null
        }
        if (error != null) { _state.update { it.copy(message = error) }; return }
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (val r = auth.register(name, email, pass)) {
                is Outcome.Success -> _state.update { it.copy(loading = false, registered = true, message = "Cuenta creada con éxito") }
                is Outcome.Failure -> _state.update { it.copy(loading = false, message = r.message) }
            }
        }
    }

    companion object {
        val NAME_REGEX = Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")
    }
}

data class ForgotUiState(val email: String = "", val loading: Boolean = false, val message: String? = null, val sent: Boolean = false)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(ForgotUiState())
    val state: StateFlow<ForgotUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun send() {
        val email = _state.value.email.trim()
        if (email.isEmpty()) { _state.update { it.copy(message = "Ingresá tu correo") }; return }
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (val r = auth.sendPasswordReset(email)) {
                is Outcome.Success -> _state.update {
                    it.copy(loading = false, sent = true, message = "Te enviamos un correo para restablecer tu contraseña")
                }
                is Outcome.Failure -> _state.update { it.copy(loading = false, message = r.message) }
            }
        }
    }
}
