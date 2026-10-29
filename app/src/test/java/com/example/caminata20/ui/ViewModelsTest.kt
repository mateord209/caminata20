package com.example.caminata20.ui

import com.example.caminata20.MainDispatcherRule
import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AparienciaApp
import com.example.caminata20.domain.model.AuthUser
import com.example.caminata20.domain.model.EscalaTexto
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.TemaApp
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
import com.example.caminata20.ui.myday.HistoryRange
import com.example.caminata20.ui.myday.MyDayViewModel
import com.example.caminata20.ui.onboarding.OnboardingViewModel
import com.example.caminata20.ui.pausetype.PauseTypeViewModel
import com.example.caminata20.ui.profile.ProfileViewModel
import com.example.caminata20.ui.reminder.ReminderViewModel
import com.example.caminata20.ui.settings.SettingsViewModel
import com.example.caminata20.ui.splash.SplashDestination
import com.example.caminata20.ui.splash.SplashViewModel
import com.example.caminata20.ui.stats.StatsViewModel
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

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
        val vm = ProfileViewModel(repo, auth, FakeLocalState())
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

    // ---------- HU-04 / HU-09 (Mi Día) ----------
    @Test
    fun `HU-04 la vista semanal solo lista completadas de los ultimos 7 dias`() = runTest {
        // FakePauseHistoryRepository.today devuelve todos los ítems, así que sembramos
        // timestamps reales para validar el filtro de 7 días sobre `records`.
        val history = FakePauseHistoryRepository()
        val vm = MyDayViewModel(FakeProfileRepository(), history, FakePreferencesRepository(), FakeLocalState())
        keep(vm.state)
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        history.add(PauseRecord(now, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 2 * day, PauseType.HIDRATACION, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 5 * day, PauseType.RESPIRACION, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 8 * day, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 2 * day, PauseType.DESCANSO_VISUAL, PauseStatus.POSPUESTA))

        vm.setRange(HistoryRange.SEMANA)

        assertEquals(HistoryRange.SEMANA, vm.state.value.range)
        assertEquals(3, vm.state.value.visibleRecords.size)
        assertTrue(vm.state.value.visibleRecords.all { it.status == PauseStatus.COMPLETADA })
        assertFalse(vm.state.value.visibleRecords.any { it.type == PauseType.DESCANSO_VISUAL })
    }

    @Test
    fun `HU-04 cambiar de rango conserva el historial completado del dia`() = runTest {
        val history = FakePauseHistoryRepository()
        val vm = MyDayViewModel(FakeProfileRepository(), history, FakePreferencesRepository(), FakeLocalState())
        keep(vm.state)
        val now = System.currentTimeMillis()
        history.add(PauseRecord(now, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now + 1, PauseType.HIDRATACION, PauseStatus.POSPUESTA))

        assertEquals(1, vm.state.value.todayRecords.size)
        assertTrue(vm.state.value.todayRecords.all { it.status == PauseStatus.COMPLETADA })

        vm.setRange(HistoryRange.SEMANA)
        vm.setRange(HistoryRange.DIA)

        assertEquals(HistoryRange.DIA, vm.state.value.range)
        assertEquals(1, vm.state.value.visibleRecords.size)
        assertEquals(1, vm.state.value.todayRecords.size)
    }

    @Test
    fun `HU-09 el filtro por categoria convive con el rango de mi dia`() = runTest {
        val history = FakePauseHistoryRepository()
        val vm = MyDayViewModel(FakeProfileRepository(), history, FakePreferencesRepository(), FakeLocalState())
        keep(vm.state)
        val now = System.currentTimeMillis()
        history.add(PauseRecord(now - 3_000, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 2_000, PauseType.HIDRATACION, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 1_000, PauseType.HIDRATACION, PauseStatus.POSPUESTA))

        assertEquals(2, vm.state.value.visibleRecords.size)

        vm.onFilterType(PauseType.HIDRATACION)
        assertEquals(PauseType.HIDRATACION, vm.state.value.typeFilter)
        assertEquals(1, vm.state.value.visibleRecords.size)
        assertEquals(PauseStatus.COMPLETADA, vm.state.value.visibleRecords.single().status)

        vm.setRange(HistoryRange.SEMANA)
        assertEquals(1, vm.state.value.visibleRecords.size)

        vm.onFilterType(null)
        assertEquals(2, vm.state.value.visibleRecords.size)
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

    // ---------- HU-09 / HU-18 (Estadísticas) ----------
    @Test
    fun `HU-09 el historial de estadisticas ordena y filtra por categoria`() = runTest {
        val history = FakePauseHistoryRepository()
        val vm = StatsViewModel(FakeProfileRepository(), history, FakePreferencesRepository())
        keep(vm.state)
        val now = System.currentTimeMillis()
        history.add(PauseRecord(now - 3_000, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 2_000, PauseType.HIDRATACION, PauseStatus.COMPLETADA))
        history.add(PauseRecord(now - 1_000, PauseType.HIDRATACION, PauseStatus.POSPUESTA))

        assertEquals(
            listOf(PauseType.HIDRATACION, PauseType.ESTIRAMIENTO),
            vm.state.value.history.map { it.type },
        )

        vm.onFilterType(PauseType.HIDRATACION)
        assertEquals(1, vm.state.value.history.size)
        assertEquals(PauseType.HIDRATACION, vm.state.value.history.single().type)

        vm.onFilterType(null)
        assertEquals(2, vm.state.value.history.size)
    }

    @Test
    fun `HU-18 estadisticas calculan total y porcentaje semanal agregado`() = runTest {
        val prefs = FakePreferencesRepository()
        prefs.scheduleState.value = ReminderScheduleConfig(
            activeDays = setOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
            ),
        )
        val history = FakePauseHistoryRepository()
        val vm = StatsViewModel(FakeProfileRepository(), history, prefs)
        keep(vm.state)
        val now = System.currentTimeMillis()
        repeat(40) { history.add(PauseRecord(now, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA)) }

        assertEquals(40, vm.state.value.weeklyCompleted)
        assertEquals(56, vm.state.value.weeklyExpected)
        assertEquals(71, vm.state.value.weeklyPercent)
        assertTrue(vm.state.value.shareText.contains("40 de 56 pausas"))
        assertTrue(vm.state.value.shareText.contains("71%"))
    }

    // ---------- HU-13 / HU-19 (Apariencia) ----------
    @Test
    fun `HU-13 cambiar tema persiste de inmediato en local`() = runTest {
        val prefs = FakePreferencesRepository(); val local = FakeLocalState()
        val sync = PauseScheduleSync(prefs, local, FakePauseScheduler(), FakeReminderScheduler())
        val vm = SettingsViewModel(prefs, local, sync)
        advanceUntilIdle()

        vm.setTheme(TemaApp.OSCURO)
        advanceUntilIdle()

        assertEquals(TemaApp.OSCURO, vm.state.value.apariencia.tema)
        assertEquals(TemaApp.OSCURO, local.aparienciaState.value.tema)
        assertEquals(TemaApp.OSCURO, prefs.aparienciaState.value.tema)
        assertFalse(vm.state.value.saved) // no se llamó save()
    }

    @Test
    fun `HU-13 el perfil activa y desactiva el modo oscuro`() = runTest {
        val local = FakeLocalState()
        val vm = ProfileViewModel(FakeProfileRepository(), FakeAuthRepository(), local)
        advanceUntilIdle()

        vm.setDarkMode(true)
        advanceUntilIdle()
        assertEquals(TemaApp.OSCURO, vm.state.value.apariencia.tema)
        assertEquals(TemaApp.OSCURO, local.aparienciaState.value.tema)

        vm.setDarkMode(false)
        advanceUntilIdle()
        assertEquals(TemaApp.CLARO, vm.state.value.apariencia.tema)
        assertEquals(TemaApp.CLARO, local.aparienciaState.value.tema)
    }

    @Test
    fun `HU-13 interruptor de modo oscuro activa y desactiva el tema`() = runTest {
        val prefs = FakePreferencesRepository(); val local = FakeLocalState()
        val sync = PauseScheduleSync(prefs, local, FakePauseScheduler(), FakeReminderScheduler())
        val vm = SettingsViewModel(prefs, local, sync)
        advanceUntilIdle()

        vm.setDarkMode(true)
        advanceUntilIdle()
        assertEquals(TemaApp.OSCURO, vm.state.value.apariencia.tema)
        assertEquals(TemaApp.OSCURO, local.aparienciaState.value.tema)

        vm.setDarkMode(false)
        advanceUntilIdle()
        assertEquals(TemaApp.CLARO, vm.state.value.apariencia.tema)
        assertEquals(TemaApp.CLARO, local.aparienciaState.value.tema)
    }

    @Test
    fun `HU-13 cambiar escala de texto persiste de inmediato en local`() = runTest {
        val prefs = FakePreferencesRepository(); val local = FakeLocalState()
        val sync = PauseScheduleSync(prefs, local, FakePauseScheduler(), FakeReminderScheduler())
        val vm = SettingsViewModel(prefs, local, sync)
        advanceUntilIdle()

        vm.setTextScale(EscalaTexto.EXTRA_GRANDE)
        advanceUntilIdle()

        assertEquals(EscalaTexto.EXTRA_GRANDE, vm.state.value.apariencia.escalaTexto)
        assertEquals(EscalaTexto.EXTRA_GRANDE, local.aparienciaState.value.escalaTexto)
        assertFalse(vm.state.value.saved)
    }

    @Test
    fun `HU-13 carga la apariencia local al iniciar`() = runTest {
        val prefs = FakePreferencesRepository()
        val local = FakeLocalState().apply {
            aparienciaState.value = AparienciaApp(TemaApp.OSCURO, EscalaTexto.GRANDE)
        }
        val sync = PauseScheduleSync(prefs, local, FakePauseScheduler(), FakeReminderScheduler())
        val vm = SettingsViewModel(prefs, local, sync)
        advanceUntilIdle()

        assertEquals(AparienciaApp(TemaApp.OSCURO, EscalaTexto.GRANDE), vm.state.value.apariencia)
    }

    @Test
    fun `HU-13 adopta la apariencia remota solo si local esta en defaults`() = runTest {
        val prefs = FakePreferencesRepository().apply {
            aparienciaState.value = AparienciaApp(TemaApp.OSCURO, EscalaTexto.GRANDE)
        }
        val local = FakeLocalState()
        val sync = PauseScheduleSync(prefs, local, FakePauseScheduler(), FakeReminderScheduler())
        val vm = SettingsViewModel(prefs, local, sync)
        advanceUntilIdle()

        assertEquals(TemaApp.OSCURO, vm.state.value.apariencia.tema)
        assertEquals(EscalaTexto.GRANDE, vm.state.value.apariencia.escalaTexto)
        assertEquals(TemaApp.OSCURO, local.aparienciaState.value.tema)
        assertEquals(EscalaTexto.GRANDE, local.aparienciaState.value.escalaTexto)
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
