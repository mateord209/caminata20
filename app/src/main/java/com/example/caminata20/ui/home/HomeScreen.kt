package com.example.caminata20.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.ui.Routes
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PauseTypeIcon
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.TopBar

private data class HomeAction(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

private val ACTIONS = listOf(
    HomeAction("Iniciar caminata", "Pasos y distancia con GPS", Icons.AutoMirrored.Filled.DirectionsWalk, Routes.WALK),
    HomeAction("Historial de caminatas", "Tus recorridos guardados", Icons.Filled.History, Routes.WALK_HISTORY),
    HomeAction("Ejercicios con IA", "Rutina de estiramientos generada", Icons.Filled.AutoAwesome, Routes.AI),
    HomeAction("Historial de rutinas IA", "Rutinas completadas", Icons.AutoMirrored.Filled.ListAlt, Routes.AI_HISTORY),
    HomeAction("Recordatorio diario", "Aviso a una hora fija", Icons.Filled.Alarm, Routes.REMINDER),
    HomeAction("Ajustes", "Intervalos, apariencia, perfiles e hidratación", Icons.Filled.Settings, Routes.SETTINGS),
    HomeAction("Permisos", "Notificaciones y alarmas exactas", Icons.Filled.Alarm, Routes.PERMISSIONS),
)

/** Pestaña Inicio (reemplaza a la MainActivity XML). */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Inicio", actions = {
            IconButton(onClick = { onNavigate(Routes.PROFILE) }) { Icon(Icons.Filled.Person, contentDescription = "Mi Perfil") }
        })
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Hola, ${state.userName}", style = MaterialTheme.typography.titleLarge)
            if (state.motivationalPhrase.isNotBlank()) {
                BreakeeCard {
                    Text("Tip del día", style = MaterialTheme.typography.labelMedium)
                    Text(state.motivationalPhrase, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state.meetingActive) {
                BreakeeCard(highlighted = true) {
                    Text("Modo reunión activo", style = MaterialTheme.typography.titleMedium)
                    Text("Las alertas se reanudarán al terminar el silencio.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("${state.stats.totalSteps}", "Pasos totales", Icons.AutoMirrored.Filled.DirectionsWalk, Modifier.weight(1f))
                StatCard("%.2f km".format(state.stats.totalKm), "Distancia", Icons.Filled.Route, Modifier.weight(1f))
                StatCard("%.2f km".format(state.stats.weekKm), "Esta semana", Icons.Filled.DateRange, Modifier.weight(1f))
            }
            // HU-08: ícono y etiqueta salen del estado observado → cambian al instante
            val pause = state.pauseSettings
            BreakeeCard(highlighted = true, onClick = { onNavigate(Routes.PAUSE_TYPE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PauseTypeIcon(pause.type, modifier = Modifier.padding(end = 16.dp))
                    Column {
                        Text("Tipo de pausa", style = MaterialTheme.typography.labelMedium)
                        Text(pause.type.title, style = MaterialTheme.typography.titleMedium)
                        Text("${pause.durationMin} min · toca para cambiar", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            ACTIONS.forEach { a ->
                BreakeeCard(onClick = { onNavigate(a.route) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(a.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        Column {
                            Text(a.title, style = MaterialTheme.typography.titleMedium)
                            Text(a.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
