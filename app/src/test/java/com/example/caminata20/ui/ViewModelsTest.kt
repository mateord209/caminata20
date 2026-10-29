package com.example.caminata20.ui

import com.example.caminata20.MainDispatcherRule
import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.WalkSession
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.domain.usecase.FraseMotivacionalUseCase
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import com.example.caminata20.domain.usecase.PauseScheduleSync
import com.example.caminata20.fakes.FakeAiRoutineRepository
import com.example.caminata20.fakes.FakeAuthRepository
import com.example.caminata20.fakes.FakeGenerator
import com.example.caminata20.fakes.FakeLocalState
import com.example.caminata20.fakes.FakeNotifications
import com.example.caminata20.fakes.FakePauseHistoryRepository
import com.example.caminata20.fakes.FakePauseScheduler
import com.example.caminata20.fakes.FakePreferencesRepository
import com.example.caminata20.fakes.FakeProfileRepository
import com.example.caminata20.fakes.FakeReminderScheduler
import com.example.caminata20.fakes.FakeWalkRepository
import com.example.caminata20.ui.ai.AiExercisesViewModel
import com.example.caminata20.ui.alerts.AlertsViewModel
import com.example.caminata20.ui.auth.ForgotPasswordViewModel
import com.example.caminata20.ui.auth.LoginViewModel
import com.example.caminata20.ui.auth.RegisterViewModel
import com.example.caminata20.ui.home.HomeViewModel
import com.example.caminata20.ui.myday.MyDayViewModel
import com.example.caminata20.ui.onboarding.OnboardingViewModel
import com.example.caminata20.ui.pausetype.PauseTypeViewModel
import com.example.caminata20.ui.profile.ProfileViewModel
import com.example.caminata20.ui.reminder.ReminderViewModel
import com.example.caminata20.ui.splash.SplashDestination
import com.example.caminata20.ui.splash.SplashViewModel
import com.example.caminata20.ui.walk.WalkHistoryViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import com.example.caminata20.ui.components.icon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    /** Mantiene suscrito un StateFlow con WhileSubscribed durante el test. */
    private fun <T> TestScope.keep(flow: StateFlow<T>) = backgroundScope.launch(main.dispatcher) { flow.collect {} }

    // ---------- Auth ----------
    @Test
    fun `login valida campos vacios`() = runTest {
        val vm = LoginViewModel(FakeAuthRepository())
        vm.signIn()
        assertEquals("Completá todos los campos", vm.state.value.message)
        assertFalse(vm.state.value.loggedIn)
    }

    @Test
    fun `login exitoso recorta espacios y marca loggedIn`() = runTest {
        val auth = FakeAuthRepository()
        val vm = LoginViewModel(auth)
        vm.onEmail("  ana@x.com "); vm.onPassword("123456")
        vm.signIn()
        assertEquals("ana@x.com", auth.lastEmail)
        assertTrue(vm.state.value.loggedIn)
    }

    @Test
    fun `login fallido muestra el mensaje del repositorio`() = runTest {
        val vm = LoginViewModel(FakeAuthRepository(Outcome.Failure("Correo o contraseña incorrectos")))
        vm.onEmail("a@x.com"); vm.onPassword("bad")
        vm.signIn()
        assertEquals("Correo o contraseña incorrectos", vm.state.value.message)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `google sin token muestra error`() = runTest {
        val vm = LoginViewModel(FakeAuthRepository())
        vm.signInWithGoogle(null)
        assertTrue(vm.state.value.message!!.contains("Google"))
    }

    @Test
    fun `registro aplica las validaciones originales`() = runTest {
        val vm = RegisterViewModel(FakeAuthRepository())
        vm.onName("Ana123") // filtrado: solo letras
        assertEquals("", vm.state.value.name)
        vm.onName("Ana Pérez"); vm.onEmail("a@x.com"); vm.onPassword("123"); vm.onConfirm("123")
        vm.register()
        assertEquals("La contraseña debe tener al menos 6 caracteres", vm.state.value.message)
        vm.onPassword("123456"); vm.onConfirm("1234567")
        vm.register()
        assertEquals("Las contraseñas no coinciden", vm.state.value.message)
        vm.onConfirm("123456")
        vm.register()
        assertTrue(vm.state.value.registered)
    }

    @Test
    fun `recuperar contrasena exige correo`() = runTest {
        val vm = ForgotPasswordViewModel(FakeAuthRepository())
        vm.send()
        assertEquals("Ingresá tu correo", vm.state.value.message)
        vm.onEmail("a@x.com"); vm.send()
        assertTrue(vm.state.value.sent)
    }

    // ---------- Splash / Onboarding ----------
    @Test
    fun `splash envia a onboarding, login o inicio`() = runTest {
        val local = FakeLocalState(); val auth = FakeAuthRepository()
        val vm1 = SplashViewModel(local, auth)
        advanceTimeBy(SplashViewModel.SPLASH_MS + 1)
        assertEquals(SplashDestination.ONBOARDING, vm1.state.value.destination)

        local.setOnboardingDone()
        val vm2 = SplashViewModel(local, auth)
        advanceTimeBy(SplashViewModel.SPLASH_MS + 1)
        assertEquals(SplashDestination.LOGIN, vm2.state.value.destination)

        auth.user.value = AuthUser("u", "Ana", "a@x.com")
        val vm3 = SplashViewModel(local, auth)
        advanceTimeBy(SplashViewModel.SPLASH_MS + 1)
        assertEquals(SplashDestination.HOME, vm3.state.value.destination)
    }

    @Test
    fun `onboarding guarda la bandera al terminar`() = runTest {
        val local = FakeLocalState()
        val vm = OnboardingViewModel(local)
        vm.next(); vm.next()
        assertTrue(vm.state.value.isLast)
        vm.next()
        assertTrue(local.onboarding.value)
        assertTrue(vm.state.value.finished)
    }

    // ---------- HU-08 / HU-12 ----------
    @Test
    fun `tipo de pausa se guarda en preferencias`() = runTest {
        val prefs = FakePreferencesRepository()
        val vm = PauseTypeViewModel(prefs)
        vm.select(PauseType.DESCANSO_VISUAL); vm.setDuration(1); vm.save()
        assertEquals(PauseType.DESCANSO_VISUAL, prefs.settings.value.type)
        assertEquals(1, prefs.settings.value.durationMin)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun `perfil valida nombre, limita la meta y cierra sesion`() = runTest {
        val repo = FakeProfileRepository(); val auth = FakeAuthRepository()
        val vm = ProfileViewModel(repo, auth)
        vm.onNameChange("  "); vm.save()
        assertEquals("El nombre no puede estar vacío", vm.state.value.nameError)
        vm.onGoalChange(40)
        assertEquals(15, vm.state.value.goal)
        assertEquals(15, repo.state.value.dailyGoal)
        vm.signOut()
        assertTrue(auth.signedOut)
        assertTrue(vm.state.value.signedOut)
    }

    @Test
    fun `mi dia recalcula el progreso al cambiar la meta o completar`() = runTest {
        val profile = FakeProfileRepository(); val history = FakePauseHistoryRepository()
        val vm = MyDayViewModel(profile, history, FakePreferencesRepository(), FakeLocalState())
        keep(vm.state)
        repeat(2) { history.add(PauseRecord(it.toLong(), PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA)) }
        history.add(PauseRecord(9, PauseType.ESTIRAMIENTO, PauseStatus.POSPUESTA))
        assertEquals(2, vm.state.value.completed)
        assertEquals(25, vm.state.value.percent)
        profile.saveGoal(4)
        assertEquals(50, vm.state.value.percent)
        assertEquals(4, vm.state.value.totalMinutes)
    }

    // ---------- HU-03 ----------
    @Test
    fun `alertas muestran advertencia tras mas de 3 posposiciones`() = runTest {
        val prefs = FakePreferencesRepository(); val local = FakeLocalState()
        val scheduler = FakePauseScheduler()
        val sync = PauseScheduleSync(prefs, local, scheduler, FakeReminderScheduler())
        val useCase = PauseActionsUseCase(prefs, FakePauseHistoryRepository(), local, scheduler, sync, FakeNotifications(), Clock { 0 })
        val vm = AlertsViewModel(prefs, local, useCase)
        keep(vm.state)
        repeat(3) { vm.snooze() }
        assertFalse(vm.state.value.showStreakWarning)
        vm.snooze()
        assertTrue(vm.state.value.showStreakWarning)
        vm.complete()
        assertFalse(vm.state.value.showStreakWarning)
    }

    // ---------- Caminata / Home ----------
    @Test
    fun `inicio calcula totales desde las caminatas`() = runTest {
        val walks = FakeWalkRepository()
        val local = FakeLocalState()
        val vm = HomeViewModel(FakeProfileRepository(), walks, FakePreferencesRepository(), local, FraseMotivacionalUseCase(local))
        keep(vm.state)
        walks.add(WalkSession(System.currentTimeMillis(), 1000, 0.75))
        walks.add(WalkSession(System.currentTimeMillis(), 500, 0.25))
        assertEquals(1500, vm.state.value.stats.totalSteps)
        assertEquals(1.0, vm.state.value.stats.totalKm, 1e-9)
        assertEquals("Ana", vm.state.value.userName)
    }

    @Test
    fun `HU-08 inicio refleja al instante el cambio de tipo de pausa`() = runTest {
        val prefs = FakePreferencesRepository()
        val local = FakeLocalState()
        val home = HomeViewModel(FakeProfileRepository(), FakeWalkRepository(), prefs, local, FraseMotivacionalUseCase(local))
        keep(home.state)
        assertEquals(PauseType.ESTIRAMIENTO, home.state.value.pauseSettings.type)

        // Se cambia desde la pantalla de configuración (otro ViewModel, misma fuente de datos)
        val config = PauseTypeViewModel(prefs)
        config.select(PauseType.HIDRATACION); config.setDuration(3); config.save()

        assertEquals(PauseType.HIDRATACION, home.state.value.pauseSettings.type)
        assertEquals(3, home.state.value.pauseSettings.durationMin)
        assertEquals(Icons.Filled.WaterDrop, home.state.value.pauseSettings.type.icon)
        assertEquals("Hidratación", home.state.value.pauseSettings.type.title)

        prefs.savePauseSettings(PauseType.DESCANSO_VISUAL, 1)
        assertEquals(Icons.Filled.Visibility, home.state.value.pauseSettings.type.icon)
    }

    @Test
    fun `HU-08 cada tipo de pausa tiene un icono distinto`() {
        val icons = PauseType.entries.map { it.icon }
        assertEquals(PauseType.entries.size, icons.toSet().size)
    }

    @Test
    fun `HU-08 alertas y mi dia siguen el tipo de pausa actual`() = runTest {
        val prefs = FakePreferencesRepository(); val local = FakeLocalState()
        val scheduler = FakePauseScheduler()
        val sync = PauseScheduleSync(prefs, local, scheduler, FakeReminderScheduler())
        val useCase = PauseActionsUseCase(prefs, FakePauseHistoryRepository(), local, scheduler, sync, FakeNotifications(), Clock { 0 })
        val alerts = AlertsViewModel(prefs, local, useCase)
        val myDay = MyDayViewModel(FakeProfileRepository(), FakePauseHistoryRepository(), prefs, local)
        keep(alerts.state); keep(myDay.state)
        prefs.savePauseSettings(PauseType.RESPIRACION, 2)
        assertEquals(PauseType.RESPIRACION, alerts.state.value.type)
        assertEquals(PauseType.RESPIRACION, myDay.state.value.suggested!!.type)
    }

    @Test
    fun `historial de caminatas pide confirmacion y borra lo seleccionado`() = runTest {
        val walks = FakeWalkRepository()
        walks.add(WalkSession(1, 10, 0.1)); walks.add(WalkSession(2, 20, 0.2))
        val vm = WalkHistoryViewModel(walks)
        keep(vm.state)
        vm.requestDelete()
        assertEquals("No seleccionaste ninguno", vm.state.value.message)
        vm.toggle("w0"); vm.requestDelete()
        assertTrue(vm.state.value.confirmDelete)
        vm.confirmDelete()
        assertEquals(listOf("w1"), walks.items.value.map { it.id })
        assertTrue(vm.state.value.selected.isEmpty())
    }

    // ---------- IA ----------
    @Test
    fun `rutina IA avanza y guarda al finalizar`() = runTest {
        val exs = listOf(AiExercise("A", "cuello", 1, 5, "x"), AiExercise("B", "brazos", 2, 8, "y"))
        val routines = FakeAiRoutineRepository()
        val vm = AiExercisesViewModel(FakeGenerator(Outcome.Success(exs)), routines, Clock { 42 })
        vm.generate()
        assertEquals("A", vm.state.value.current!!.name)
        vm.next()
        assertTrue(vm.state.value.isLast)
        vm.next()
        assertTrue(vm.state.value.finished)
        assertEquals(42L, routines.items.value.single().timestamp)
    }

    @Test
    fun `error de IA se muestra y no guarda`() = runTest {
        val routines = FakeAiRoutineRepository()
        val vm = AiExercisesViewModel(FakeGenerator(Outcome.Failure("Error al generar la rutina: x")), routines, Clock { 0 })
        vm.generate()
        assertEquals("Error al generar la rutina: x", vm.state.value.message)
        assertNull(vm.state.value.current)
        assertTrue(routines.items.value.isEmpty())
    }

    // ---------- Recordatorio ----------
    @Test
    fun `recordatorio programa, persiste y cancela`() = runTest {
        val prefs = FakePreferencesRepository(); val scheduler = FakeReminderScheduler()
        val vm = ReminderViewModel(prefs, scheduler)
        keep(vm.state)
        vm.set(7, 30)
        assertEquals("07:30", scheduler.scheduled!!.label)
        assertEquals("07:30", vm.state.value.active!!.label)
        vm.cancel()
        assertTrue(scheduler.cancelled)
        assertNull(vm.state.value.active)
    }
}
