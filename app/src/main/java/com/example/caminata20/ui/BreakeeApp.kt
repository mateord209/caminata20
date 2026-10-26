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
            composable(BreakeeTab.MI_DIA.route) { StatsScreen(title = "Mi Día") }
            composable(BreakeeTab.ESTADISTICAS.route) { StatsScreen(title = "Estadísticas") }
            composable(Routes.PAUSE_TYPE) { PauseTypeScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

/** Rutas fuera de la barra inferior. */
object Routes {
    const val PAUSE_TYPE = "tipo_pausa"
}
