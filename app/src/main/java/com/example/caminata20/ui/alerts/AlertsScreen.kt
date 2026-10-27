package com.example.caminata20.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import com.example.caminata20.data.system.AlarmPauseScheduler
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.theme.BreakeeThemeExt

@Composable
fun AlertsScreen(onConfigure: () -> Unit, vm: AlertsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val status = BreakeeThemeExt.status
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Alertas", actions = {
            IconButton(onClick = onConfigure) { Icon(Icons.Filled.Tune, contentDescription = "Configurar pausa") }
        })
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.showStreakWarning) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = status.warningContainer, contentColor = status.warning),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(PauseActionsUseCase.streakWarningText(state.consecutiveSnoozes), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            BreakeeCard {
                Text("Breakee · ahora", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("¡Hora de tu pausa activa!", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${state.type.emoji} ${state.type.title}: ${state.exercise?.name ?: ""}. ${state.exercise?.instructions ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton("Completar", onClick = { vm.complete() }, icon = Icons.Filled.Check, modifier = Modifier.weight(1f))
                    SecondaryButton("Posponer 5 min", onClick = { vm.snooze() }, icon = Icons.Filled.Snooze, modifier = Modifier.weight(1f))
                }
                state.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Text("Próximas alertas", style = MaterialTheme.typography.titleLarge)
            // TODO(HU-01/HU-02): listar las alertas programadas por el temporizador de intervalos real.
            BreakeeCard {
                Text("Cada ${AlarmPauseScheduler.DEFAULT_INTERVAL_MIN} min", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Intervalo provisional. La configuración de intervalos y horario llega con HU-01/HU-02.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
