package com.example.caminata20.fakes

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.repository.AiExerciseGenerator
import com.example.caminata20.domain.repository.AiRoutineRepository
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseHistoryRepository
import com.example.caminata20.domain.repository.PauseNotificationGateway
import com.example.caminata20.domain.repository.PauseScheduler
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.repository.ReminderScheduler
import com.example.caminata20.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAuthRepository(var result: Outcome<AuthUser> = Outcome.Success(AuthUser("u1", "Ana", "ana@x.com"))) : AuthRepository {
    val user = MutableStateFlow<AuthUser?>(null)
    var lastEmail: String? = null
    var signedOut = false
    var resetResult: Outcome<Unit> = Outcome.Success(Unit)
    override val currentUser: Flow<AuthUser?> = user
    override fun currentUserOrNull() = user.value
    override suspend fun signInWithEmail(email: String, password: String) = result.also { lastEmail = email; if (it is Outcome.Success) user.value = it.value }
    override suspend fun signInWithGoogle(idToken: String) = result.also { if (it is Outcome.Success) user.value = it.value }
    override suspend fun register(name: String, email: String, password: String) = result
    override suspend fun sendPasswordReset(email: String) = resetResult
    override suspend fun updateDisplayName(name: String) {}
    override fun signOut() { signedOut = true; user.value = null }
}

class FakeProfileRepository(initial: Profile = Profile("Ana", "ana@x.com", 8)) : ProfileRepository {
    val state = MutableStateFlow(initial)
    override val profile: Flow<Profile> = state
    override suspend fun saveName(name: String) = state.update { it.copy(name = name) }
    override suspend fun saveGoal(goal: Int) = state.update { it.copy(dailyGoal = goal) }
}

class FakePreferencesRepository : PreferencesRepository {
    val settings = MutableStateFlow(PauseSettings())
    val reminderState = MutableStateFlow<ReminderTime?>(null)
    override val pauseSettings: Flow<PauseSettings> = settings
    override suspend fun current() = settings.value
    override suspend fun savePauseSettings(type: PauseType, durationMin: Int) { settings.value = PauseSettings(type, durationMin) }
    override val reminder: Flow<ReminderTime?> = reminderState
    override suspend fun saveReminder(time: ReminderTime?) { reminderState.value = time }
}

class FakePauseHistoryRepository : PauseHistoryRepository {
    val items = MutableStateFlow<List<PauseRecord>>(emptyList())
    override val records: Flow<List<PauseRecord>> = items
    override val today: Flow<List<PauseRecord>> = items
    override suspend fun add(record: PauseRecord) = items.update { it + record }
}

class FakeWalkRepository : WalkRepository {
    val items = MutableStateFlow<List<WalkSession>>(emptyList())
    override val sessions: Flow<List<WalkSession>> = items
    override suspend fun add(session: WalkSession) = items.update { it + session.copy(id = "w${it.size}") }
    override suspend fun delete(ids: Collection<String>) = items.update { l -> l.filterNot { it.id in ids } }
}

class FakeAiRoutineRepository : AiRoutineRepository {
    val items = MutableStateFlow<List<AiRoutine>>(emptyList())
    override val routines: Flow<List<AiRoutine>> = items
    override suspend fun add(routine: AiRoutine) = items.update { it + routine.copy(id = "r${it.size}") }
    override suspend fun delete(ids: Collection<String>) = items.update { l -> l.filterNot { it.id in ids } }
}

class FakeGenerator(var result: Outcome<List<AiExercise>>) : AiExerciseGenerator {
    override suspend fun generate() = result
}

class FakeLocalState : LocalStateRepository {
    val onboarding = MutableStateFlow(false)
    val snoozes = MutableStateFlow(0)
    val lastExercise = MutableStateFlow<String?>(null)
    private val migrated = mutableSetOf<String>()
    private var reminder: ReminderTime? = null
    override val onboardingDone: Flow<Boolean> = onboarding
    override suspend fun setOnboardingDone() { onboarding.value = true }
    override val consecutiveSnoozes: Flow<Int> = snoozes
    override suspend fun incrementSnoozes(): Int { snoozes.value += 1; return snoozes.value }
    override suspend fun resetSnoozes() { snoozes.value = 0 }
    override val lastExerciseId: Flow<String?> = lastExercise
    override suspend fun setLastExercise(id: String) { lastExercise.value = id }
    override suspend fun isMigrated(uid: String) = uid in migrated
    override suspend fun setMigrated(uid: String) { migrated += uid }
    override suspend fun cachedReminder() = reminder
    override suspend fun cacheReminder(time: ReminderTime?) { reminder = time }
}

class FakePauseScheduler : PauseScheduler {
    var restarts = 0
    val scheduled = mutableListOf<Int>()
    override fun restart() { restarts++ }
    override fun scheduleIn(minutes: Int) { scheduled += minutes }
}

class FakeReminderScheduler : ReminderScheduler {
    var scheduled: ReminderTime? = null
    var cancelled = false
    override fun schedule(time: ReminderTime) { scheduled = time }
    override fun cancel() { cancelled = true }
}

class FakeNotifications : PauseNotificationGateway {
    var dismissed = 0
    val warnings = mutableListOf<Int>()
    override fun dismissPause() { dismissed++ }
    override fun showStreakWarning(consecutive: Int) { warnings += consecutive }
}
