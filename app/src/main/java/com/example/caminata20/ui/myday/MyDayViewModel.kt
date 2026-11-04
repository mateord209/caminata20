package com.example.caminata20.ui.myday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.repository.GoalHistoryRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.MeetingDayRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.usecase.CalculateStreakUseCase
import com.example.caminata20.domain.usecase.SuggestedExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlinx.coroutines.launch
import javax.inject.Inject

/** HU-04: rango del historial (día actual o últimos 7 días). */
enum class HistoryRange { DIA, SEMANA }

data class MyDayUiState(
    val name: String = "",
    val completed: Int = 0,
    val goal: Int = Profile.DEFAULT_GOAL,
    val totalMinutes: Int = 0,
    val streak: Int = 0,
    /** Pausas COMPLETADA de hoy, en orden cronológico ascendente (CA-03). */
    val todayRecords: List<PauseRecord> = emptyList(),
    /** Pausas COMPLETADA de los últimos 7 días (hoy incluido), de más reciente a más antigua. */
    val weekRecords: List<PauseRecord> = emptyList(),
    val range: HistoryRange = HistoryRange.DIA,
    /** HU-09: filtro por categoría; null = todas. */
    val typeFilter: PauseType? = null,
    val suggested: Exercise? = null,
) {
    val progress: Float get() = if (goal == 0) 0f else completed.toFloat() / goal
    val percent: Int get() = (progress * 100).toInt().coerceAtMost(100)

    /** Lista visible según el rango y el filtro de categoría. */
    val visibleRecords: List<PauseRecord>
        get() = (if (range == HistoryRange.DIA) todayRecords else weekRecords)
            .filter { typeFilter == null || it.type == typeFilter }
}

/** HU-04, HU-09, HU-12, HU-17: Mi Día con progreso, racha e historial. */
@HiltViewModel
class MyDayViewModel @Inject constructor(
    profile: ProfileRepository,
    history: PauseHistoryRepository,
    private val prefs: PreferencesRepository,
    private val local: LocalStateRepository,
    meetingDays: MeetingDayRepository,
    goalHistory: GoalHistoryRepository,
    private val calculateStreak: CalculateStreakUseCase,
) : ViewModel() {
    init {
        // HU-16: al abrir o al cambiar el tipo de pausa, garantiza un ejercicio válido (aleatorio) y persistido
        viewModelScope.launch {
            combine(prefs.pauseSettings, local.lastExerciseId) { s, id -> s.type to id }
                .collect { (type, id) -> // se lee el tipo actual (no el emitido) para no competir con otra pantalla con un valor viejo
                    if (SuggestedExercise.current(id, type) == null) SuggestedExercise.ensure(local, prefs.current().type) }
        }
    }

    /** HU-16: botón "Otro ejercicio". */
    fun shuffle() = viewModelScope.launch { SuggestedExercise.shuffle(local, prefs.current().type) }

    private val range = MutableStateFlow(HistoryRange.DIA)
    private val typeFilter = MutableStateFlow<PauseType?>(null)
    private val historyFilters = combine(typeFilter, range) { type, selectedRange -> type to selectedRange }

    val state: StateFlow<MyDayUiState> = combine(
        combine(profile.profile, history.records, history.today) { p, all, today ->
            Triple(p, all, today)
        },
        prefs.pauseSettings,
        prefs.reminderSchedule,
        combine(local.lastExerciseId, meetingDays.days(), goalHistory.periods()) { id, days, periods ->
            Triple(id, days, periods)
        },
        historyFilters,
    ) { triple, settings, schedule, lastAndMeeting, filters ->
        val (lastId, meeting, goalPeriods) = lastAndMeeting
        val (p, all, today) = triple
        val (filter, selectedRange) = filters
        val completedToday = today
            .filter { it.status == PauseStatus.COMPLETADA }
            .sortedBy { it.timestamp }
        val weekStart = startOfToday() - 6L * DAY_MILLIS
        val weekRecords = all
            .filter { it.status == PauseStatus.COMPLETADA && it.timestamp >= weekStart }
            .sortedByDescending { it.timestamp }
        MyDayUiState(
            name = p.name,
            completed = completedToday.size,
            goal = p.dailyGoal,
            totalMinutes = completedToday.size * settings.durationMin,
            streak = calculateStreak(all, p.dailyGoal, goalPeriods, schedule.activeDays, meeting, System.currentTimeMillis()),
            todayRecords = completedToday,
            weekRecords = weekRecords,
            range = selectedRange,
            typeFilter = filter,
            suggested = SuggestedExercise.current(lastId, settings.type),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyDayUiState())

    /** HU-04: conmuta entre la vista del día y la de los últimos 7 días. */
    fun setRange(newRange: HistoryRange) {
        range.value = newRange
    }

    /** HU-09: filtra el historial por categoría; null muestra todas. */
    fun onFilterType(type: PauseType?) {
        typeFilter.value = type
    }

    private fun startOfToday(now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
