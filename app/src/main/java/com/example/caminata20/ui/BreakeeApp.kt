package com.example.caminata20.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caminata20.ui.components.BottomNavBar
import com.example.caminata20.ui.components.BreakeeTab
import com.example.caminata20.ui.home.HomeScreen
import com.example.caminata20.ui.stats.StatsScreen
import com.example.caminata20.ui.pausetype.PauseTypeScreen
import com.example.caminata20.ui.myday.MyDayScreen
import com.example.caminata20.ui.profile.ProfileScreen
import com.example.caminata20.ui.guide.GuideScreen

@Composable
fun BreakeeApp(startRoute: String? = null) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = BreakeeTab.entries.any { it.route == current }

    LaunchedEffect(startRoute) {
        if (startRoute != null) nav.navigate(startRoute)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) BottomNavBar(currentRoute = current, onSelect = { tab ->
                nav.navigate(tab.route) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        },
    ) { padding ->
        NavHost(nav, startDestination = BreakeeTab.INICIO.route, modifier = Modifier.padding(padding)) {
            composable(BreakeeTab.INICIO.route) { HomeScreen(onNavigate = { nav.navigate(it) }) }
            composable(BreakeeTab.ALERTAS.route) { StatsScreen(title = "Alertas") }
            composable(BreakeeTab.MI_DIA.route) { MyDayScreen(onOpenProfile = { nav.navigate(Routes.PROFILE) }, onStartExercise = { nav.navigate(Routes.guide(it)) }) }
            composable(BreakeeTab.ESTADISTICAS.route) { StatsScreen(title = "Estadísticas") }
            composable(Routes.GUIDE) { entry ->
                GuideScreen(exerciseId = entry.arguments?.getString("exerciseId"), onBack = { nav.popBackStack() })
            }
            composable(Routes.PROFILE) { ProfileScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.PAUSE_TYPE) { PauseTypeScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

/** Rutas fuera de la barra inferior. */
object Routes {
    const val PAUSE_TYPE = "tipo_pausa"
    const val PROFILE = "perfil"
    const val GUIDE = "guia/{exerciseId}"
    fun guide(exerciseId: String) = "guia/$exerciseId"
}
