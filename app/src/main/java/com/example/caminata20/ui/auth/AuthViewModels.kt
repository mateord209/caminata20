package com.example.caminata20.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.usecase.RegisterUserUseCase
import com.example.caminata20.domain.validation.AuthValidator
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
    val emailError: String? = null,
    val passwordError: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val validator: AuthValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v, emailError = null, message = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, passwordError = null, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun signIn() {
        if (_state.value.loading) return
        val s = _state.value
        val email = s.email.trim()
        val pass = s.password
        val errors = validator.validateLogin(email, pass)
        if (errors.hasErrors) {
            _state.update {
                it.copy(
                    emailError = errors.email?.message,
                    passwordError = errors.password?.message,
                    message = null,
                )
            }
            return
        }
        _state.update {
            it.copy(emailError = null, passwordError = null, message = null)
        }
        run { auth.signInWithEmail(email, pass) }
    }

    fun signInWithGoogle(idToken: String?) {
        if (_state.value.loading) return
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
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val confirmError: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUser: RegisterUserUseCase,
    private val validator: AuthValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    /** Solo letras, tildes, ñ y espacios (misma regla que la pantalla XML anterior). */
    fun onName(v: String) {
        if (AuthValidator.NAME_REGEX.matches(v)) {
            _state.update { it.copy(name = v, nameError = null, message = null) }
        }
    }

    fun onEmail(v: String) = _state.update { it.copy(email = v, emailError = null, message = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, passwordError = null, message = null) }
    fun onConfirm(v: String) = _state.update { it.copy(confirm = v, confirmError = null, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun register() {
        if (_state.value.loading) return
        val s = _state.value
        val name = s.name.trim()
        val email = s.email.trim()
        val pass = s.password
        val confirm = s.confirm
        val errors = validator.validateRegister(name, email, pass, confirm)
        if (errors.hasErrors) {
            _state.update {
                it.copy(
                    nameError = errors.name?.message,
                    emailError = errors.email?.message,
                    passwordError = errors.password?.message,
                    confirmError = errors.confirm?.message,
                    message = null,
                )
            }
            return
        }
        _state.update {
            it.copy(
                loading = true,
                message = null,
                nameError = null,
                emailError = null,
                passwordError = null,
                confirmError = null,
            )
        }
        viewModelScope.launch {
            when (val r = registerUser(name, email, pass)) {
                is Outcome.Success -> _state.update {
                    it.copy(loading = false, registered = true, message = "Cuenta creada con éxito")
                }
                is Outcome.Failure -> _state.update { it.copy(loading = false, message = r.message) }
            }
        }
    }
}

data class ForgotUiState(
    val email: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val sent: Boolean = false,
    val emailError: String? = null,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val validator: AuthValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotUiState())
    val state: StateFlow<ForgotUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v, emailError = null, message = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun send() {
        if (_state.value.loading) return
        val email = _state.value.email.trim()
        val errors = validator.validateForgot(email)
        if (errors.hasErrors) {
            _state.update { it.copy(emailError = errors.email?.message, message = null) }
            return
        }
        _state.update { it.copy(loading = true, emailError = null, message = null) }
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
