package com.example.caminata20.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(val active: ReminderTime? = null, val message: String? = null, val done: Boolean = false)

/** Recordatorio diario (antes ReminderActivity). */
@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {
    private val ui = MutableStateFlow(ReminderUiState())

    val state: StateFlow<ReminderUiState> = combine(prefs.reminder, ui) { r, u -> u.copy(active = r) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderUiState())

    fun set(hour: Int, minute: Int) {
        val t = ReminderTime(hour, minute)
        scheduler.schedule(t)
        viewModelScope.launch {
            prefs.saveReminder(t)
            ui.update { it.copy(message = "Recordatorio activado", done = true) }
        }
    }

    fun cancel() {
        scheduler.cancel()
        viewModelScope.launch {
            prefs.saveReminder(null)
            ui.update { it.copy(message = "Recordatorio cancelado") }
        }
    }

    fun consumeMessage() = ui.update { it.copy(message = null) }
}
