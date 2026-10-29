package com.example.caminata20.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.domain.model.AparienciaApp
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.EscalaTexto
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.ScheduleProfile
import com.example.caminata20.domain.model.ScheduleProfilesState
import com.example.caminata20.domain.model.TemaApp
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.usecase.PauseScheduleSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val intervalMin: Int = ReminderScheduleConfig.DEFAULT_INTERVAL,
    val activeDays: Set<Int> = ReminderScheduleConfig.DIAS_LABORALES,
    val profiles: ScheduleProfilesState = ScheduleProfilesState(),
    val hidratacion: ConfigHidratacion = ConfigHidratacion(),
    val apariencia: AparienciaApp = AparienciaApp(),
    val meetingActive: Boolean = false,
    val message: String? = null,
    val saved: Boolean = false,
)

/** HU-01, HU-06, HU-14, HU-15, HU-19: ajustes de intervalos, perfiles y apariencia. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val local: LocalStateRepository,
    private val scheduleSync: PauseScheduleSync,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { cargarDesdePreferencias() }
    }

    private suspend fun cargarDesdePreferencias() {
        val schedule = prefs.currentSchedule()
        val meeting = local.meetingUntil.first()
        // HU-13: la copia local es la fuente principal (funciona sin sesión).
        val localApariencia = local.apariencia.first()
        val remotaApariencia = runCatching { prefs.apariencia.first() }.getOrDefault(AparienciaApp())
        val apariencia = if (localApariencia == AparienciaApp() && remotaApariencia != AparienciaApp()) {
            // Migración suave: solo si local está en defaults se adopta lo guardado en la nube.
            runCatching { local.saveApariencia(remotaApariencia) }
            remotaApariencia
        } else {
            localApariencia
        }
        _state.update {
            it.copy(
                intervalMin = schedule.intervalMin,
                activeDays = schedule.activeDays,
                profiles = prefs.scheduleProfiles.first(),
                hidratacion = prefs.hidratacion.first(),
                apariencia = apariencia,
                meetingActive = meeting != null && meeting > System.currentTimeMillis(),
            )
        }
    }

    fun toggleDay(day: Int) = _state.update {
        val days = it.activeDays.toMutableSet()
        if (day in days) days.remove(day) else days.add(day)
        it.copy(activeDays = days, saved = false)
    }

    fun setInterval(minutes: Int) = _state.update {
        it.copy(intervalMin = minutes.coerceIn(ReminderScheduleConfig.MIN_INTERVAL, 240), saved = false)
    }

    fun setProfileName(index: Int, name: String) = _state.update { ui ->
        val profiles = if (index == 1) ui.profiles.copy(perfil2 = ui.profiles.perfil2.copy(nombre = name))
        else ui.profiles.copy(perfil1 = ui.profiles.perfil1.copy(nombre = name))
        ui.copy(profiles = profiles, saved = false)
    }

    fun saveCurrentAsProfile(index: Int) = _state.update { ui ->
        val perfil = ScheduleProfile(
            if (index == 1) ui.profiles.perfil2.nombre else ui.profiles.perfil1.nombre,
            ui.intervalMin,
            ui.activeDays,
        )
        val profiles = if (index == 1) ui.profiles.copy(perfil2 = perfil) else ui.profiles.copy(perfil1 = perfil)
        ui.copy(profiles = profiles, saved = false)
    }

    fun activateProfile(index: Int) = viewModelScope.launch {
        prefs.activateScheduleProfile(index)
        scheduleSync.syncPauseTimers()
        cargarDesdePreferencias()
        _state.update { it.copy(message = "Perfil activado", saved = true) }
    }

    fun setHydration(active: Boolean) = _state.update { it.copy(hidratacion = it.hidratacion.copy(activo = active), saved = false) }

    fun setHydrationInterval(minutes: Int) = _state.update {
        it.copy(hidratacion = it.hidratacion.copy(intervaloMin = minutes), saved = false)
    }

    fun setTheme(tema: TemaApp) {
        _state.update { it.copy(apariencia = it.apariencia.copy(tema = tema), saved = false) }
        persistApariencia()
    }

    /** HU-13: interruptor "Modo oscuro" (ON = oscuro, OFF = claro). */
    fun setDarkMode(enabled: Boolean) = setTheme(if (enabled) TemaApp.OSCURO else TemaApp.CLARO)

    fun setTextScale(escala: EscalaTexto) {
        _state.update { it.copy(apariencia = it.apariencia.copy(escalaTexto = escala), saved = false) }
        persistApariencia()
    }

    /** HU-13: el cambio se aplica al instante; local es obligatorio, Firestore best-effort. */
    private fun persistApariencia() {
        val apariencia = _state.value.apariencia
        viewModelScope.launch {
            local.saveApariencia(apariencia)
            runCatching { prefs.saveApariencia(apariencia) }
        }
    }

    fun enableMeeting(minutes: Int) = viewModelScope.launch {
        local.setMeetingUntil(System.currentTimeMillis() + minutes * 60_000L)
        scheduleSync.syncPauseTimers()
        _state.update { it.copy(message = "Modo reunión activado", meetingActive = true) }
    }

    fun disableMeeting() = viewModelScope.launch {
        local.setMeetingUntil(null)
        scheduleSync.syncPauseTimers()
        _state.update { it.copy(message = "Modo reunión desactivado", meetingActive = false) }
    }

    fun save() = viewModelScope.launch {
        val ui = _state.value
        prefs.saveReminderSchedule(ReminderScheduleConfig(ui.intervalMin, ui.activeDays))
        prefs.saveScheduleProfiles(ui.profiles)
        prefs.saveHidratacion(ui.hidratacion)
        local.saveApariencia(ui.apariencia)
        runCatching { prefs.saveApariencia(ui.apariencia) }
        scheduleSync.syncAll()
        _state.update { it.copy(message = "Configuración guardada", saved = true) }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
