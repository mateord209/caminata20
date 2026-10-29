package com.example.caminata20.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caminata20.ui.ai.AiExercisesScreen
import com.example.caminata20.ui.ai.AiHistoryScreen
import com.example.caminata20.ui.alerts.AlertsScreen
import com.example.caminata20.ui.auth.ForgotPasswordScreen
import com.example.caminata20.ui.auth.LoginScreen
import com.example.caminata20.ui.auth.RegisterScreen
import com.example.caminata20.ui.components.BottomNavBar
import com.example.caminata20.ui.components.BreakeeTab
import com.example.caminata20.ui.guide.GuideScreen
import com.example.caminata20.ui.home.HomeScreen
import com.example.caminata20.ui.myday.MyDayScreen
import com.example.caminata20.ui.onboarding.OnboardingScreen
import com.example.caminata20.ui.pausetype.PauseTypeScreen
import com.example.caminata20.ui.profile.ProfileScreen
import com.example.caminata20.ui.reminder.ReminderScreen
import com.example.caminata20.ui.splash.SplashDestination
import com.example.caminata20.ui.splash.SplashScreen
import com.example.caminata20.ui.stats.StatsScreen
import com.example.caminata20.ui.walk.WalkHistoryScreen
import com.example.caminata20.ui.walk.WalkScreen

/** Rutas de la app (una sola Activity). */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "registro"
    const val FORGOT = "recuperar"
    const val PAUSE_TYPE = "tipo_pausa"
    const val PROFILE = "perfil"
    const val WALK = "caminata"
    const val WALK_HISTORY = "caminata_historial"
    const val AI = "ia"
    const val AI_HISTORY = "ia_historial"
    const val REMINDER = "recordatorio"
    const val GUIDE = "guia/{exerciseId}"
    fun guide(exerciseId: String) = "guia/$exerciseId"
}

/** Navega a la home vaciando la pila (tras login/registro). */
private fun NavHostController.goHome() = navigate(BreakeeTab.INICIO.route) { popUpTo(graph.id) { inclusive = true } }

private fun NavHostController.goLogin() = navigate(Routes.LOGIN) { popUpTo(graph.id) { inclusive = true } }

/**
 * Grafo único de navegación. [deepLink] llega desde notificaciones (guía de ejercicio, caminata).
 * [onAuthenticated] dispara tareas post-login (migración de datos legados).
 */
@Composable
fun BreakeeApp(deepLink: String? = null, isSignedIn: () -> Boolean, onAuthenticated: () -> Unit = {}) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = BreakeeTab.entries.any { it.route == current }

    LaunchedEffect(deepLink) {
        if (deepLink != null && isSignedIn()) {
            nav.goHome()
            nav.navigate(deepLink)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) BottomNavBar(currentRoute = current, onSelect = { tab ->
                nav.navigate(tab.route) {
                    popUpTo(BreakeeTab.INICIO.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.SPLASH, modifier = Modifier.padding(padding)) {
            composable(Routes.SPLASH) {
                SplashScreen(onDestination = { dest ->
                    when (dest) {
                        SplashDestination.ONBOARDING -> nav.navigate(Routes.ONBOARDING) { popUpTo(nav.graph.id) { inclusive = true } }
                        SplashDestination.LOGIN -> nav.goLogin()
                        SplashDestination.HOME -> { onAuthenticated(); nav.goHome() }
                    }
                })
            }
            composable(Routes.ONBOARDING) { OnboardingScreen(onFinished = { nav.goLogin() }) }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { onAuthenticated(); nav.goHome() },
                    onRegister = { nav.navigate(Routes.REGISTER) },
                    onForgot = { nav.navigate(Routes.FORGOT) },
                )
            }
            composable(Routes.REGISTER) { RegisterScreen(onRegistered = { onAuthenticated(); nav.goHome() }, onBack = { nav.popBackStack() }) }
            composable(Routes.FORGOT) { ForgotPasswordScreen(onBack = { nav.popBackStack() }) }

            composable(BreakeeTab.INICIO.route) { HomeScreen(onNavigate = { nav.navigate(it) }) }
            composable(BreakeeTab.ALERTAS.route) { AlertsScreen(onConfigure = { nav.navigate(Routes.PAUSE_TYPE) }) }
            composable(BreakeeTab.MI_DIA.route) {
                MyDayScreen(onOpenProfile = { nav.navigate(Routes.PROFILE) }, onStartExercise = { nav.navigate(Routes.guide(it)) })
            }
            // TODO(HU-04): estadísticas e historial completo (otro integrante).
            composable(BreakeeTab.ESTADISTICAS.route) { StatsScreen(title = "Estadísticas") }

            composable(Routes.GUIDE) { entry ->
                GuideScreen(exerciseId = entry.arguments?.getString("exerciseId"), onBack = { nav.popBackStack() })
            }
            composable(Routes.PROFILE) { ProfileScreen(onBack = { nav.popBackStack() }, onSignedOut = { nav.goLogin() }) }
            composable(Routes.PAUSE_TYPE) { PauseTypeScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.WALK) { WalkScreen(onBack = { nav.popBackStack() }, onFinished = { nav.popBackStack() }) }
            composable(Routes.WALK_HISTORY) { WalkHistoryScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.AI) { AiExercisesScreen(onBack = { nav.popBackStack() }, onFinished = { nav.popBackStack() }) }
            composable(Routes.AI_HISTORY) { AiHistoryScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.REMINDER) { ReminderScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
